package com.tablecraft.app.service;

import com.tablecraft.app.model.ColumnDefinition;
import com.tablecraft.app.model.TableConfig;
import com.tablecraft.app.model.TableDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON設定ファイルベースのテーブルサービス
 * 外部設定ファイルから読み込んだテーブル定義を使用してCRUD操作を提供
 */
@Service
public class ConfigBasedTableService {

    private final ExternalConfigService configService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ConfigBasedTableService(ExternalConfigService configService, JdbcTemplate jdbcTemplate) {
        this.configService = configService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * すべてのテーブル名を取得
     */
    public Set<String> getAllTableNames() {
        TableConfig config = configService.getTableConfig();
        return config != null && config.getTables() != null
                ? config.getTables().keySet()
                : new HashSet<>();
    }

    /**
     * 指定されたテーブルの定義を取得
     */
    public TableDefinition getTableDefinition(String tableName) {
        TableConfig config = configService.getTableConfig();
        if (config != null && config.getTables() != null) {
            return config.getTables().get(tableName);
        }
        return null;
    }

    /**
     * テーブルのカラム一覧を取得
     */
    public List<String> getTableColumns(String tableName) {
        TableDefinition tableDefinition = getTableDefinition(tableName);
        if (tableDefinition != null && tableDefinition.getColumns() != null) {
            return tableDefinition.getColumns().stream()
                    .map(ColumnDefinition::getName)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * テーブルの主キーカラム一覧を取得
     */
    public List<String> getPrimaryKeyColumns(String tableName) {
        TableDefinition tableDefinition = getTableDefinition(tableName);
        if (tableDefinition != null && tableDefinition.getPrimaryKey() != null) {
            return tableDefinition.getPrimaryKey().getColumns();
        }
        return new ArrayList<>();
    }

    /**
     * テーブルが存在するかチェック
     */
    public boolean tableExists(String tableName) {
        return getAllTableNames().contains(tableName);
    }

    /**
     * テーブルのレコード一覧を取得（ページネーション付き）
     */
    public List<Map<String, Object>> findAll(String tableName, int offset, int limit, String orderBy) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("テーブルが見つかりません: " + tableName);
        }

        List<String> columns = getTableColumns(tableName);
        if (columns.isEmpty()) {
            throw new IllegalStateException("テーブルのカラム情報が取得できません: " + tableName);
        }

        String columnsStr = String.join(", ", columns);
        String sql = String.format("SELECT %s FROM %s", columnsStr, tableName);

        // ORDER BY句の追加
        if (orderBy != null && !orderBy.trim().isEmpty()) {
            sql += " ORDER BY " + orderBy;
        } else {
            // デフォルトは主キーでソート
            List<String> primaryKeys = getPrimaryKeyColumns(tableName);
            if (!primaryKeys.isEmpty()) {
                sql += " ORDER BY " + String.join(", ", primaryKeys);
            }
        }

        // LIMIT句の追加
        sql += String.format(" LIMIT %d OFFSET %d", limit, offset);

        return jdbcTemplate.queryForList(sql);
    }

    /**
     * テーブルのレコード数を取得
     */
    public long count(String tableName) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("テーブルが見つかりません: " + tableName);
        }

        String sql = "SELECT COUNT(*) FROM " + tableName;
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    /**
     * 主キーによるレコード検索
     */
    public Map<String, Object> findById(String tableName, Map<String, Object> primaryKeyValues) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("テーブルが見つかりません: " + tableName);
        }

        List<String> primaryKeys = getPrimaryKeyColumns(tableName);
        if (primaryKeys.isEmpty()) {
            throw new IllegalStateException("主キーが定義されていません: " + tableName);
        }

        List<String> columns = getTableColumns(tableName);
        String columnsStr = String.join(", ", columns);

        // WHERE句の構築
        List<String> whereConditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (String pkColumn : primaryKeys) {
            if (!primaryKeyValues.containsKey(pkColumn)) {
                throw new IllegalArgumentException("主キー値が不足しています: " + pkColumn);
            }
            whereConditions.add(pkColumn + " = ?");
            parameters.add(primaryKeyValues.get(pkColumn));
        }

        String sql = String.format("SELECT %s FROM %s WHERE %s",
                columnsStr, tableName, String.join(" AND ", whereConditions));

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, parameters.toArray());
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 主キーによるレコード検索（リスト形式で返却）
     * autofill用エンドポイント向け
     */
    public List<Map<String, Object>> findByPrimaryKey(String tableName, Map<String, Object> primaryKeyValues) {
        Map<String, Object> result = findById(tableName, primaryKeyValues);
        if (result == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(result);
    }

    /**
     * レコードの挿入
     */
    public int insert(String tableName, Map<String, Object> data) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("テーブルが見つかりません: " + tableName);
        }

        validateDataTypes(tableName, data);

        List<String> columns = new ArrayList<>(data.keySet());
        List<String> placeholders = columns.stream().map(col -> "?").collect(Collectors.toList());
        List<Object> values = columns.stream().map(data::get).collect(Collectors.toList());

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                String.join(", ", columns),
                String.join(", ", placeholders));

        return jdbcTemplate.update(sql, values.toArray());
    }

    /**
     * レコードの更新
     */
    public int update(String tableName, Map<String, Object> data, Map<String, Object> primaryKeyValues) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("テーブルが見つかりません: " + tableName);
        }

        List<String> primaryKeys = getPrimaryKeyColumns(tableName);
        if (primaryKeys.isEmpty()) {
            throw new IllegalStateException("主キーが定義されていません: " + tableName);
        }

        validateDataTypes(tableName, data);

        // SET句の構築（主キー以外のカラム）
        List<String> setConditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!primaryKeys.contains(entry.getKey())) {
                setConditions.add(entry.getKey() + " = ?");
                parameters.add(entry.getValue());
            }
        }

        if (setConditions.isEmpty()) {
            throw new IllegalArgumentException("更新するカラムが指定されていません");
        }

        // WHERE句の構築
        List<String> whereConditions = new ArrayList<>();
        for (String pkColumn : primaryKeys) {
            if (!primaryKeyValues.containsKey(pkColumn)) {
                throw new IllegalArgumentException("主キー値が不足しています: " + pkColumn);
            }
            whereConditions.add(pkColumn + " = ?");
            parameters.add(primaryKeyValues.get(pkColumn));
        }

        String sql = String.format("UPDATE %s SET %s WHERE %s",
                tableName,
                String.join(", ", setConditions),
                String.join(" AND ", whereConditions));

        return jdbcTemplate.update(sql, parameters.toArray());
    }

    /**
     * レコードの削除
     */
    public int delete(String tableName, Map<String, Object> primaryKeyValues) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("テーブルが見つかりません: " + tableName);
        }

        List<String> primaryKeys = getPrimaryKeyColumns(tableName);
        if (primaryKeys.isEmpty()) {
            throw new IllegalStateException("主キーが定義されていません: " + tableName);
        }

        List<String> whereConditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (String pkColumn : primaryKeys) {
            if (!primaryKeyValues.containsKey(pkColumn)) {
                throw new IllegalArgumentException("主キー値が不足しています: " + pkColumn);
            }
            whereConditions.add(pkColumn + " = ?");
            parameters.add(primaryKeyValues.get(pkColumn));
        }

        String sql = String.format("DELETE FROM %s WHERE %s",
                tableName, String.join(" AND ", whereConditions));

        return jdbcTemplate.update(sql, parameters.toArray());
    }

    /**
     * データ型のバリデーション
     */
    private void validateDataTypes(String tableName, Map<String, Object> data) {
        TableDefinition tableDefinition = getTableDefinition(tableName);
        if (tableDefinition == null || tableDefinition.getColumns() == null) {
            return; // 定義がない場合はスキップ
        }

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String columnName = entry.getKey();
            Object value = entry.getValue();

            // カラム定義をリストから検索
            ColumnDefinition columnDef = tableDefinition.getColumns().stream()
                    .filter(col -> col.getName().equals(columnName))
                    .findFirst()
                    .orElse(null);

            if (columnDef != null && value != null) {
                validateColumnValue(tableName, columnName, columnDef, value);
            }
        }
    }

    /**
     * 個別カラムの値バリデーション
     */
    private void validateColumnValue(String tableName, String columnName, ColumnDefinition columnDef, Object value) {
        String dataType = columnDef.getType().toLowerCase();

        try {
            switch (dataType) {
                case "int":
                case "integer":
                case "bigint":
                    if (!(value instanceof Number)) {
                        Long.valueOf(value.toString());
                    }
                    break;
                case "decimal":
                case "float":
                case "double":
                    if (!(value instanceof Number)) {
                        Double.valueOf(value.toString());
                    }
                    break;
                case "varchar":
                case "text":
                case "char":
                    // 文字列型は基本的にそのまま
                    if (columnDef.getLength() != null && value.toString().length() > columnDef.getLength()) {
                        throw new IllegalArgumentException(
                                String.format("文字列が長すぎます: %s.%s (最大%d文字)", tableName, columnName,
                                        columnDef.getLength()));
                    }
                    break;
                case "datetime":
                case "timestamp":
                case "date":
                    // 日付型のバリデーション（必要に応じて実装）
                    break;
                default:
                    // その他の型は基本的にそのまま通す
                    break;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("データ型が不正です: %s.%s (期待: %s, 実際: %s)",
                            tableName, columnName, dataType, value.getClass().getSimpleName()));
        }
    }

    /**
     * テーブル設定の取得（デバッグ用）
     */
    public TableConfig getTableConfig() {
        return configService.getTableConfig();
    }

    /**
     * 設定の再読み込み
     */
    public void reloadConfig() {
        configService.reloadTableConfig();
    }

    /**
     * 設定情報の取得
     */
    public String getConfigInfo() {
        return configService.getConfigSummary();
    }
}