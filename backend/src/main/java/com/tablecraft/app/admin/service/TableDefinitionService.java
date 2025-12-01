package com.tablecraft.app.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tablecraft.app.admin.entity.ManualTableDefinition;
import com.tablecraft.app.admin.repository.ManualTableDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * テーブル定義管理サービス
 * 手動作成・編集・削除等を管理
 */
@Service
public class TableDefinitionService {

    @Autowired
    private ManualTableDefinitionRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfigGeneratorService configGeneratorService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 手動作成したテーブル定義を保存
     */
    @Transactional
    public ManualTableDefinition createManualTable(TableDefinitionRequest request) throws Exception {
        System.out.println("[TableDefinitionService] 手動テーブル作成: " + request.getTableName());

        // テーブル名の重複チェック
        if (repository.existsByTableName(request.getTableName())) {
            throw new IllegalArgumentException("Table already exists: " + request.getTableName());
        }

        ManualTableDefinition def = new ManualTableDefinition();
        def.setTableName(request.getTableName());
        def.setDisplayName(request.getDisplayName());

        // カラム定義をJSON文字列に変換
        String columnsJson = objectMapper.writeValueAsString(request.getColumns());
        def.setColumns(columnsJson);
        
        // UI設定
        if (request.getEnableSearch() != null) def.setEnableSearch(request.getEnableSearch());
        if (request.getEnableSort() != null) def.setEnableSort(request.getEnableSort());
        if (request.getEnablePagination() != null) def.setEnablePagination(request.getEnablePagination());
        if (request.getPageSize() != null) def.setPageSize(request.getPageSize());
        if (request.getAllowCreate() != null) def.setAllowCreate(request.getAllowCreate());
        if (request.getAllowEdit() != null) def.setAllowEdit(request.getAllowEdit());
        if (request.getAllowDelete() != null) def.setAllowDelete(request.getAllowDelete());
        if (request.getAllowBulk() != null) def.setAllowBulk(request.getAllowBulk());

        def.setCreatedAt(LocalDateTime.now());
        def.setUpdatedAt(LocalDateTime.now());

        ManualTableDefinition saved = repository.save(def);
        System.out.println("[TableDefinitionService] ✅ 保存完了: ID=" + saved.getId());

        // 実際のDBテーブルを作成
        try {
            createActualTable(request);
            System.out.println("[TableDefinitionService] ✅ DBテーブル作成完了: " + request.getTableName());
        } catch (Exception e) {
            System.err.println("[TableDefinitionService] ⚠️ DBテーブル作成失敗: " + e.getMessage());
            // 定義は保存されたので、エラーは警告のみ
        }

        // table-config.jsonを再生成
        try {
            regenerateTableConfig();
        } catch (Exception e) {
            System.err.println("[TableDefinitionService] ⚠️ table-config.json生成失敗: " + e.getMessage());
        }

        return saved;
    }

    /**
     * テーブル定義を更新
     */
    @Transactional
    public ManualTableDefinition updateTable(Long tableId, TableDefinitionRequest request) throws Exception {
        System.out.println("[TableDefinitionService] テーブル更新: ID=" + tableId);

        ManualTableDefinition def = repository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + tableId));

        // 既存のカラム定義と比較して構造変更があるか確認
        String oldColumnsJson = def.getColumns();
        String newColumnsJson = objectMapper.writeValueAsString(request.getColumns());
        boolean structureChanged = !oldColumnsJson.equals(newColumnsJson);

        // テーブル名は変更不可（既にDBに作成されている可能性があるため）
        // def.setTableName(request.getTableName());
        def.setDisplayName(request.getDisplayName());
        def.setColumns(newColumnsJson);
        
        // UI設定を更新
        if (request.getEnableSearch() != null) def.setEnableSearch(request.getEnableSearch());
        if (request.getEnableSort() != null) def.setEnableSort(request.getEnableSort());
        if (request.getEnablePagination() != null) def.setEnablePagination(request.getEnablePagination());
        if (request.getPageSize() != null) def.setPageSize(request.getPageSize());
        if (request.getAllowCreate() != null) def.setAllowCreate(request.getAllowCreate());
        if (request.getAllowEdit() != null) def.setAllowEdit(request.getAllowEdit());
        if (request.getAllowDelete() != null) def.setAllowDelete(request.getAllowDelete());
        if (request.getAllowBulk() != null) def.setAllowBulk(request.getAllowBulk());
        
        def.setUpdatedAt(LocalDateTime.now());

        ManualTableDefinition updated = repository.save(def);
        
        // 構造変更があった場合
        if (structureChanged) {
            System.out.println("[TableDefinitionService] カラム構造が変更されました。");
            
            // 1. データをクリア
            System.out.println("[TableDefinitionService] データをクリアします。");
            truncateTable(updated.getTableName());
            
            // 2. テーブルを削除して再作成
            System.out.println("[TableDefinitionService] テーブルを再作成します。");
            dropTable(updated.getTableName());
            createActualTable(request);
            
            // 3. table-config.jsonを再生成
            System.out.println("[TableDefinitionService] table-config.jsonを再生成します。");
            regenerateTableConfig();
        } else {
            // 構造変更がない場合でもtable-config.jsonは更新（表示名などの変更）
            regenerateTableConfig();
        }
        
        System.out.println("[TableDefinitionService] ✅ 更新完了: " + updated.getTableName());

        return updated;
    }

    /**
     * テーブル定義を削除
     */
    @Transactional
    public void deleteTable(Long tableId) throws Exception {
        System.out.println("[TableDefinitionService] テーブル削除: ID=" + tableId);

        ManualTableDefinition def = repository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + tableId));

        String tableName = def.getTableName();
        
        // 1. 実際のDBテーブルを削除
        dropTable(tableName);
        
        // 2. テーブル定義を削除
        repository.deleteById(tableId);
        
        // 3. table-config.jsonを再生成
        regenerateTableConfig();
        
        System.out.println("[TableDefinitionService] ✅ 削除完了: " + tableName);
    }

    /**
     * 全テーブル定義を取得
     */
    public List<ManualTableDefinition> listAllTables() {
        return repository.findAll();
    }

    /**
     * 特定のテーブル定義を取得
     */
    public Optional<ManualTableDefinition> getTable(Long tableId) {
        return repository.findById(tableId);
    }

    /**
     * テーブル定義リクエスト
     */
    public static class TableDefinitionRequest {
        private String tableName;
        private String schemaName;
        private String displayName;
        private String description;
        private List<ColumnRequest> columns;
        
        // UI設定
        private Boolean enableSearch;
        private Boolean enableSort;
        private Boolean enablePagination;
        private Integer pageSize;
        private Boolean allowCreate;
        private Boolean allowEdit;
        private Boolean allowDelete;
        private Boolean allowBulk;

        // Getters and Setters
        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<ColumnRequest> getColumns() {
            return columns;
        }

        public void setColumns(List<ColumnRequest> columns) {
            this.columns = columns;
        }

        public Boolean getEnableSearch() {
            return enableSearch;
        }

        public void setEnableSearch(Boolean enableSearch) {
            this.enableSearch = enableSearch;
        }

        public Boolean getEnableSort() {
            return enableSort;
        }

        public void setEnableSort(Boolean enableSort) {
            this.enableSort = enableSort;
        }

        public Boolean getEnablePagination() {
            return enablePagination;
        }

        public void setEnablePagination(Boolean enablePagination) {
            this.enablePagination = enablePagination;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }

        public Boolean getAllowCreate() {
            return allowCreate;
        }

        public void setAllowCreate(Boolean allowCreate) {
            this.allowCreate = allowCreate;
        }

        public Boolean getAllowEdit() {
            return allowEdit;
        }

        public void setAllowEdit(Boolean allowEdit) {
            this.allowEdit = allowEdit;
        }

        public Boolean getAllowDelete() {
            return allowDelete;
        }

        public void setAllowDelete(Boolean allowDelete) {
            this.allowDelete = allowDelete;
        }

        public Boolean getAllowBulk() {
            return allowBulk;
        }

        public void setAllowBulk(Boolean allowBulk) {
            this.allowBulk = allowBulk;
        }
    }

    /**
     * カラム定義リクエスト
     */
    public static class ColumnRequest {
        private String name;
        private String type;
        private boolean nullable = true;
        private boolean primary = false;
        private boolean autoIncrement = false;
        private String defaultValue;
        private String comment;
        private Boolean visible = true;  // 一覧表示設定
        private Boolean sortable = true;  // ソート可能設定
        private Boolean filterable = true;  // フィルタ可能設定

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public boolean isAutoIncrement() {
            return autoIncrement;
        }

        public void setAutoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Boolean getVisible() {
            return visible;
        }

        public void setVisible(Boolean visible) {
            this.visible = visible;
        }

        public Boolean getSortable() {
            return sortable;
        }

        public void setSortable(Boolean sortable) {
            this.sortable = sortable;
        }

        public Boolean getFilterable() {
            return filterable;
        }

        public void setFilterable(Boolean filterable) {
            this.filterable = filterable;
        }
    }

    /**
     * テーブルのデータをクリア（TRUNCATE）
     */
    private void truncateTable(String tableName) {
        try {
            System.out.println("[TableDefinitionService] TRUNCATE TABLE: " + tableName);
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
            System.out.println("[TableDefinitionService] ✅ データクリア完了: " + tableName);
        } catch (Exception e) {
            System.err.println("[TableDefinitionService] ⚠️ データクリア失敗: " + tableName);
            System.err.println("エラー: " + e.getMessage());
            // テーブルが存在しない場合などはエラーを無視（作成前の場合があるため）
        }
    }

    /**
     * テーブルを削除（DROP TABLE）
     */
    private void dropTable(String tableName) {
        try {
            System.out.println("[TableDefinitionService] DROP TABLE: " + tableName);
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
            System.out.println("[TableDefinitionService] ✅ テーブル削除完了: " + tableName);
        } catch (Exception e) {
            System.err.println("[TableDefinitionService] ⚠️ テーブル削除失敗: " + tableName);
            System.err.println("エラー: " + e.getMessage());
        }
    }

    /**
     * カラム型を正規化（VARCHARに長さがない場合はデフォルト255を追加）
     */
    private String normalizeColumnType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "VARCHAR(255)";
        }
        
        String upperType = type.trim().toUpperCase();
        
        // VARCHARに長さが指定されていない場合
        if (upperType.equals("VARCHAR")) {
            return "VARCHAR(255)";
        }
        
        // TEXTに長さが指定されている場合は除去
        if (upperType.startsWith("TEXT(")) {
            return "TEXT";
        }
        
        return type.trim();
    }

    /**
     * table-config.jsonを再生成
     */
    private void regenerateTableConfig() throws Exception {
        System.out.println("[TableDefinitionService] table-config.json再生成開始");
        
        List<ManualTableDefinition> allTables = repository.findAll();
        Map<String, Object> config = new LinkedHashMap<>();
        Map<String, Object> tablesMap = new LinkedHashMap<>();  // 配列からMapに変更
        
        for (ManualTableDefinition tableDef : allTables) {
            Map<String, Object> tableConfig = new LinkedHashMap<>();
            String tableId = tableDef.getTableName().toLowerCase();
            
            tableConfig.put("id", tableId);
            tableConfig.put("name", tableDef.getTableName());
            tableConfig.put("label", tableDef.getDisplayName() != null ? tableDef.getDisplayName() : tableDef.getTableName());
            tableConfig.put("icon", "Table");
            
            // カラム定義を解析
            List<Map<String, Object>> columns = objectMapper.readValue(tableDef.getColumns(), List.class);
            List<Map<String, Object>> configColumns = new ArrayList<>();
            List<String> listColumns = new ArrayList<>();
            
            for (Map<String, Object> col : columns) {
                Map<String, Object> columnConfig = new LinkedHashMap<>();
                String colName = (String) col.get("name");
                String colType = (String) col.get("type");
                String colComment = col.get("comment") != null ? (String) col.get("comment") : colName;
                Boolean colVisible = col.get("visible") != null ? (Boolean) col.get("visible") : true;
                Boolean colSortable = col.get("sortable") != null ? (Boolean) col.get("sortable") : true;
                Boolean colFilterable = col.get("filterable") != null ? (Boolean) col.get("filterable") : true;
                
                // 多言語対応のラベル
                Map<String, String> labelMap = new LinkedHashMap<>();
                labelMap.put("ja", colComment);
                labelMap.put("en", colComment);
                
                columnConfig.put("name", colName);
                columnConfig.put("label", labelMap);
                columnConfig.put("type", mapToFieldType(colType));
                columnConfig.put("required", !Boolean.TRUE.equals(col.get("nullable")));
                columnConfig.put("visible", colVisible);
                columnConfig.put("sortable", colSortable);
                columnConfig.put("filterable", colFilterable);
                
                configColumns.add(columnConfig);
                
                // visibleなカラムのみリスト表示用に追加（最初の5カラム）
                if (colVisible && listColumns.size() < 5) {
                    listColumns.add(colName);
                }
            }
            
            tableConfig.put("columns", configColumns);
            tableConfig.put("listColumns", listColumns);
            tableConfig.put("enableSearch", tableDef.getEnableSearch() != null ? tableDef.getEnableSearch() : true);
            tableConfig.put("enableSort", tableDef.getEnableSort() != null ? tableDef.getEnableSort() : true);
            tableConfig.put("enablePagination", tableDef.getEnablePagination() != null ? tableDef.getEnablePagination() : true);
            tableConfig.put("pageSize", tableDef.getPageSize() != null ? tableDef.getPageSize() : 20);
            tableConfig.put("allowCreate", tableDef.getAllowCreate() != null ? tableDef.getAllowCreate() : true);
            tableConfig.put("allowEdit", tableDef.getAllowEdit() != null ? tableDef.getAllowEdit() : true);
            tableConfig.put("allowDelete", tableDef.getAllowDelete() != null ? tableDef.getAllowDelete() : true);
            tableConfig.put("allowBulk", tableDef.getAllowBulk() != null ? tableDef.getAllowBulk() : false);
            
            tablesMap.put(tableId, tableConfig);  // MapにtableIdをキーとして格納
        }
        
        config.put("tables", tablesMap);  // Map形式で格納
        
        // ファイルに保存
        configGeneratorService.saveTableConfig(config);
        System.out.println("[TableDefinitionService] ✅ table-config.json再生成完了");
    }
    
    /**
     * 実際のDBテーブルを作成
     */
    private void createActualTable(TableDefinitionRequest request) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE IF NOT EXISTS ").append(request.getTableName()).append(" (");
        
        List<String> columnDefs = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        
        for (ColumnRequest col : request.getColumns()) {
            StringBuilder colDef = new StringBuilder();
            
            // 型を正規化（VARCHARに長さがない場合はVARCHAR(255)にする）
            String colType = normalizeColumnType(col.getType());
            colDef.append(col.getName()).append(" ").append(colType);
            
            if (!col.isNullable()) {
                colDef.append(" NOT NULL");
            }
            
            if (col.isAutoIncrement()) {
                colDef.append(" AUTO_INCREMENT");
            }
            
            if (col.getDefaultValue() != null && !col.getDefaultValue().isEmpty()) {
                if ("CURRENT_TIMESTAMP".equalsIgnoreCase(col.getDefaultValue())) {
                    colDef.append(" DEFAULT CURRENT_TIMESTAMP");
                } else {
                    colDef.append(" DEFAULT '").append(col.getDefaultValue()).append("'");
                }
            }
            
            if (col.getComment() != null && !col.getComment().isEmpty()) {
                colDef.append(" COMMENT '").append(col.getComment()).append("'");
            }
            
            columnDefs.add(colDef.toString());
            
            if (col.isPrimary()) {
                primaryKeys.add(col.getName());
            }
        }
        
        ddl.append(String.join(", ", columnDefs));
        
        if (!primaryKeys.isEmpty()) {
            ddl.append(", PRIMARY KEY (").append(String.join(", ", primaryKeys)).append(")");
        }
        
        ddl.append(")");
        
        System.out.println("[TableDefinitionService] DDL: " + ddl.toString());
        jdbcTemplate.execute(ddl.toString());
    }
    
    /**
     * SQLデータ型をフィールドタイプにマッピング
     */
    private String mapToFieldType(String sqlType) {
        String type = sqlType.toUpperCase();
        if (type.contains("INT") || type.contains("DECIMAL") || type.contains("NUMERIC")) {
            return "number";
        } else if (type.contains("DATE") || type.contains("TIME")) {
            return "date";
        } else if (type.contains("BOOL")) {
            return "boolean";
        } else if (type.contains("TEXT") && type.length() > 4) {
            return "textarea";
        } else {
            return "text";
        }
    }
}
