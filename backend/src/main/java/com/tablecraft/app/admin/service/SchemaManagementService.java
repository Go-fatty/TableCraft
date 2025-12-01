package com.tablecraft.app.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tablecraft.app.admin.entity.ParsedTableDefinition;
import com.tablecraft.app.admin.repository.ParsedTableDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DBスキーマ管理サービス
 * テーブル構造をDBに適用（CREATE TABLE、ALTER TABLE）
 */
@Service
public class SchemaManagementService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ParsedTableDefinitionRepository tableDefRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * テーブル構造をDBに適用
     * 
     * @param tableId ParsedTableDefinitionのID
     * @return 適用結果メッセージ
     */
    @Transactional
    public Map<String, Object> applyTableSchema(Long tableId) {
        Map<String, Object> result = new HashMap<>();

        try {
            ParsedTableDefinition tableDef = tableDefRepo.findById(tableId)
                    .orElseThrow(() -> new IllegalArgumentException("Table definition not found: " + tableId));

            String tableName = tableDef.getTableName();
            System.out.println("[SchemaManagementService] テーブルスキーマ適用開始: " + tableName);

            // 1. テーブル存在チェック
            boolean exists = checkTableExists(tableName);

            if (!exists) {
                // 2. 新規テーブル作成
                String createSql = generateCreateTableSql(tableDef);
                System.out.println("[SchemaManagementService] CREATE TABLE実行:\n" + createSql);
                jdbcTemplate.execute(createSql);

                result.put("action", "created");
                result.put("message", "テーブルを作成しました: " + tableName);
            } else {
                // 3. 既存テーブルが存在する場合はデータをクリアしてから更新
                System.out.println("[SchemaManagementService] 既存テーブル検出: " + tableName);

                // 3-1. データクリア
                String truncateSql = "TRUNCATE TABLE `" + tableName + "`";
                System.out.println("[SchemaManagementService] データクリア実行:\n" + truncateSql);
                jdbcTemplate.execute(truncateSql);

                // 3-2. テーブル差分適用
                List<String> alterSqls = generateAlterTableSqls(tableDef);

                if (alterSqls.isEmpty()) {
                    result.put("action", "data_cleared");
                    result.put("message", "既存データをクリアしました（スキーマ変更なし）: " + tableName);
                } else {
                    for (String sql : alterSqls) {
                        System.out.println("[SchemaManagementService] ALTER TABLE実行:\n" + sql);
                        jdbcTemplate.execute(sql);
                    }
                    result.put("action", "updated_and_cleared");
                    result.put("message", "既存データをクリアし、テーブルを更新しました: " + tableName);
                    result.put("alterCount", alterSqls.size());
                }
            }

            result.put("success", true);
            result.put("tableName", tableName);
            System.out.println("[SchemaManagementService] ✅ 適用完了: " + tableName);

        } catch (Exception e) {
            System.err.println("[SchemaManagementService] ❌ エラー: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 複数テーブルを一括適用
     */
    @Transactional
    public Map<String, Object> applyMultipleSchemas(List<Long> tableIds) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (Long tableId : tableIds) {
            Map<String, Object> tableResult = applyTableSchema(tableId);
            results.add(tableResult);

            if ((Boolean) tableResult.getOrDefault("success", false)) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        result.put("success", failureCount == 0);
        result.put("totalCount", tableIds.size());
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("results", results);

        return result;
    }

    /**
     * テーブルの存在チェック
     */
    private boolean checkTableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
        return count != null && count > 0;
    }

    /**
     * CREATE TABLE文を生成
     */
    private String generateCreateTableSql(ParsedTableDefinition tableDef) throws Exception {
        String tableName = tableDef.getTableName();

        // tableStructureをJSONから読み込み
        @SuppressWarnings("unchecked")
        Map<String, Object> structure = objectMapper.readValue(
                tableDef.getTableStructure(),
                new TypeReference<Map<String, Object>>() {
                });

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columns = (List<Map<String, Object>>) structure.get("columns");

        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("カラム定義が見つかりません: " + tableName);
        }

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE `").append(tableName).append("` (\n");

        // カラム定義
        List<String> columnDefs = new ArrayList<>();
        String primaryKey = null;

        for (Map<String, Object> col : columns) {
            String colName = (String) col.get("name");
            String colType = (String) col.get("type");
            Boolean nullable = (Boolean) col.getOrDefault("nullable", true);
            Boolean isPrimary = (Boolean) col.getOrDefault("primary", false);
            Object defaultValue = col.get("default");
            Boolean autoIncrement = (Boolean) col.getOrDefault("autoIncrement", false);

            StringBuilder colDef = new StringBuilder();
            colDef.append("  `").append(colName).append("` ").append(colType);

            if (!nullable) {
                colDef.append(" NOT NULL");
            }

            if (autoIncrement) {
                colDef.append(" AUTO_INCREMENT");
            }

            if (defaultValue != null && !autoIncrement) {
                if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultValue.toString())) {
                    colDef.append(" DEFAULT CURRENT_TIMESTAMP");
                } else if (defaultValue instanceof String) {
                    colDef.append(" DEFAULT '").append(defaultValue).append("'");
                } else {
                    colDef.append(" DEFAULT ").append(defaultValue);
                }
            }

            columnDefs.add(colDef.toString());

            if (isPrimary) {
                primaryKey = colName;
            }
        }

        sql.append(String.join(",\n", columnDefs));

        // PRIMARY KEY
        if (primaryKey != null) {
            sql.append(",\n  PRIMARY KEY (`").append(primaryKey).append("`)");
        }

        sql.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

        return sql.toString();
    }

    /**
     * ALTER TABLE文を生成（既存テーブルとの差分）
     */
    private List<String> generateAlterTableSqls(ParsedTableDefinition tableDef) throws Exception {
        List<String> alterSqls = new ArrayList<>();
        String tableName = tableDef.getTableName();

        // 既存カラム情報を取得
        Map<String, Map<String, Object>> existingColumns = getExistingColumns(tableName);

        // 新しいカラム定義を取得
        @SuppressWarnings("unchecked")
        Map<String, Object> structure = objectMapper.readValue(
                tableDef.getTableStructure(),
                new TypeReference<Map<String, Object>>() {
                });

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> newColumns = (List<Map<String, Object>>) structure.get("columns");

        if (newColumns == null) {
            return alterSqls;
        }

        // 新しいカラムのマップを作成
        Map<String, Map<String, Object>> newColumnsMap = newColumns.stream()
                .collect(Collectors.toMap(
                        col -> (String) col.get("name"),
                        col -> col));

        // 1. 新規カラムの追加
        for (Map<String, Object> newCol : newColumns) {
            String colName = (String) newCol.get("name");
            if (!existingColumns.containsKey(colName)) {
                String alterSql = generateAddColumnSql(tableName, newCol);
                alterSqls.add(alterSql);
            }
        }

        // 2. カラム変更の検出（型変更、NULL制約変更など）
        for (String colName : existingColumns.keySet()) {
            if (newColumnsMap.containsKey(colName)) {
                Map<String, Object> existingCol = existingColumns.get(colName);
                Map<String, Object> newCol = newColumnsMap.get(colName);

                if (isColumnChanged(existingCol, newCol)) {
                    String alterSql = generateModifyColumnSql(tableName, newCol);
                    alterSqls.add(alterSql);
                }
            }
        }

        // 3. 削除されたカラム（注意: データ損失の可能性）
        // for (String existingColName : existingColumns.keySet()) {
        // if (!newColumnsMap.containsKey(existingColName)) {
        // String alterSql = "ALTER TABLE `" + tableName + "` DROP COLUMN `" +
        // existingColName + "`;";
        // alterSqls.add(alterSql);
        // }
        // }
        // ↑ 安全のため削除は手動で行う想定（コメントアウト）

        return alterSqls;
    }

    /**
     * 既存カラム情報を取得
     */
    private Map<String, Map<String, Object>> getExistingColumns(String tableName) {
        String sql = "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA " +
                "FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tableName);

        Map<String, Map<String, Object>> columns = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String colName = (String) row.get("COLUMN_NAME");
            columns.put(colName, row);
        }

        return columns;
    }

    /**
     * カラム変更の検出
     */
    private boolean isColumnChanged(Map<String, Object> existingCol, Map<String, Object> newCol) {
        String existingType = ((String) existingCol.get("COLUMN_TYPE")).toUpperCase();
        String newType = ((String) newCol.get("type")).toUpperCase();

        String existingNullable = (String) existingCol.get("IS_NULLABLE");
        Boolean newNullable = (Boolean) newCol.getOrDefault("nullable", true);

        // 型の比較（簡易版）
        if (!existingType.startsWith(newType.split("\\(")[0])) {
            return true;
        }

        // NULL制約の比較
        boolean existingIsNullable = "YES".equalsIgnoreCase(existingNullable);
        if (existingIsNullable != newNullable) {
            return true;
        }

        return false;
    }

    /**
     * ADD COLUMN文の生成
     */
    private String generateAddColumnSql(String tableName, Map<String, Object> col) {
        String colName = (String) col.get("name");
        String colType = (String) col.get("type");
        Boolean nullable = (Boolean) col.getOrDefault("nullable", true);
        Object defaultValue = col.get("default");

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE `").append(tableName).append("` ADD COLUMN `")
                .append(colName).append("` ").append(colType);

        if (!nullable) {
            sql.append(" NOT NULL");
        }

        if (defaultValue != null) {
            if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultValue.toString())) {
                sql.append(" DEFAULT CURRENT_TIMESTAMP");
            } else if (defaultValue instanceof String) {
                sql.append(" DEFAULT '").append(defaultValue).append("'");
            } else {
                sql.append(" DEFAULT ").append(defaultValue);
            }
        }

        sql.append(";");
        return sql.toString();
    }

    /**
     * MODIFY COLUMN文の生成
     */
    private String generateModifyColumnSql(String tableName, Map<String, Object> col) {
        String colName = (String) col.get("name");
        String colType = (String) col.get("type");
        Boolean nullable = (Boolean) col.getOrDefault("nullable", true);
        Object defaultValue = col.get("default");

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE `").append(tableName).append("` MODIFY COLUMN `")
                .append(colName).append("` ").append(colType);

        if (!nullable) {
            sql.append(" NOT NULL");
        }

        if (defaultValue != null) {
            if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultValue.toString())) {
                sql.append(" DEFAULT CURRENT_TIMESTAMP");
            } else if (defaultValue instanceof String) {
                sql.append(" DEFAULT '").append(defaultValue).append("'");
            } else {
                sql.append(" DEFAULT ").append(defaultValue);
            }
        }

        sql.append(";");
        return sql.toString();
    }
}
