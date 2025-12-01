package com.tablecraft.app.admin.service;

import com.tablecraft.app.admin.dto.TableDefinitionResponse;
import com.tablecraft.app.admin.dto.TableSchemaInfo;
import com.tablecraft.app.admin.entity.TableUISettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Integrated table management service
 * Combines INFORMATION_SCHEMA data with UI settings
 */
@Service
public class IntegratedTableService {

    @Value("${tablecraft.admin.system-tables:manual_table_definitions,parsed_table_definitions,table_ui_settings}")
    private String systemTablesConfig;

    @Autowired
    private TableSchemaService schemaService;

    @Autowired
    private TableUISettingsService uiSettingsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigGeneratorService configGeneratorService;

    /**
     * Get all tables with their UI settings
     * 
     * @return List of table definitions
     */
    public List<TableDefinitionResponse> getAllTables() {
        List<String> tableNames = schemaService.getAllTableNames();
        List<TableDefinitionResponse> responses = new ArrayList<>();

        for (String tableName : tableNames) {
            // Skip system tables
            if (isSystemTable(tableName)) {
                continue;
            }

            Optional<TableSchemaInfo> schema = schemaService.getTableSchema(tableName);
            if (schema.isPresent()) {
                Optional<TableUISettings> uiSettings = uiSettingsService.getSettingsByTableName(tableName);
                TableDefinitionResponse response = TableDefinitionResponse.from(
                        schema.get(),
                        uiSettings.orElse(null));
                responses.add(response);
            }
        }

        return responses;
    }

    /**
     * Get a specific table by name
     * 
     * @param tableName Table name
     * @return Table definition
     */
    public Optional<TableDefinitionResponse> getTableByName(String tableName) {
        Optional<TableSchemaInfo> schema = schemaService.getTableSchema(tableName);
        if (schema.isEmpty()) {
            return Optional.empty();
        }

        Optional<TableUISettings> uiSettings = uiSettingsService.getSettingsByTableName(tableName);
        TableDefinitionResponse response = TableDefinitionResponse.from(
                schema.get(),
                uiSettings.orElse(null));

        return Optional.of(response);
    }

    /**
     * Get a specific table by UI settings ID
     * 
     * @param id UI settings ID
     * @return Table definition
     */
    public Optional<TableDefinitionResponse> getTableById(Long id) {
        Optional<TableUISettings> uiSettings = uiSettingsService.getSettingsById(id);
        if (uiSettings.isEmpty()) {
            return Optional.empty();
        }

        String tableName = uiSettings.get().getTableName();
        Optional<TableSchemaInfo> schema = schemaService.getTableSchema(tableName);
        if (schema.isEmpty()) {
            return Optional.empty();
        }

        TableDefinitionResponse response = TableDefinitionResponse.from(
                schema.get(),
                uiSettings.get());

        return Optional.of(response);
    }

    /**
     * Create a new table with UI settings
     * 
     * @param request Table creation request
     * @return Created table definition
     */
    @Transactional
    public TableDefinitionResponse createTable(TableCreationRequest request) {
        try {
            System.out.println("[IntegratedTableService] Creating table: " + request.getTableName());

            // 1. Create actual MySQL table
            createActualTable(request);
            System.out.println("[IntegratedTableService] Table created successfully: " + request.getTableName());

            // 2. Create UI settings
            TableUISettings uiSettings = new TableUISettings();
            uiSettings.setTableName(request.getTableName());
            uiSettings.setDisplayName(request.getDisplayName());
            uiSettings.setDescription(request.getDescription());

            if (request.getEnableSearch() != null)
                uiSettings.setEnableSearch(request.getEnableSearch());
            if (request.getEnableSort() != null)
                uiSettings.setEnableSort(request.getEnableSort());
            if (request.getEnablePagination() != null)
                uiSettings.setEnablePagination(request.getEnablePagination());
            if (request.getPageSize() != null)
                uiSettings.setPageSize(request.getPageSize());
            if (request.getAllowCreate() != null)
                uiSettings.setAllowCreate(request.getAllowCreate());
            if (request.getAllowEdit() != null)
                uiSettings.setAllowEdit(request.getAllowEdit());
            if (request.getAllowDelete() != null)
                uiSettings.setAllowDelete(request.getAllowDelete());
            if (request.getAllowBulk() != null)
                uiSettings.setAllowBulk(request.getAllowBulk());

            TableUISettings savedSettings = uiSettingsService.saveSettings(uiSettings);
            System.out.println("[IntegratedTableService] UI settings saved: ID=" + savedSettings.getId());

            // 3. Regenerate table-config.json
            try {
                configGeneratorService.regenerateTableConfig();
                System.out.println("[IntegratedTableService] table-config.json regenerated");
            } catch (Exception e) {
                System.err.println(
                        "[IntegratedTableService] Warning: Failed to regenerate table-config.json: " + e.getMessage());
            }

            // 4. Get and return the created table
            Optional<TableSchemaInfo> schema = schemaService.getTableSchema(request.getTableName());
            if (schema.isEmpty()) {
                throw new RuntimeException("Failed to retrieve created table schema: " + request.getTableName());
            }

            return TableDefinitionResponse.from(schema.get(), savedSettings);
        } catch (Exception e) {
            System.err.println("[IntegratedTableService] Error creating table: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create table: " + e.getMessage(), e);
        }
    }

    /**
     * Update UI settings for a table
     * 
     * @param id      UI settings ID
     * @param request Update request
     * @return Updated table definition
     */
    @Transactional
    public TableDefinitionResponse updateTableUISettings(Long id, TableUISettingsUpdateRequest request) {
        TableUISettings settings = uiSettingsService.getSettingsById(id)
                .orElseThrow(() -> new IllegalArgumentException("UI settings not found: " + id));

        // Update UI settings (table structure cannot be changed here)
        if (request.getDisplayName() != null)
            settings.setDisplayName(request.getDisplayName());
        if (request.getDescription() != null)
            settings.setDescription(request.getDescription());
        if (request.getEnableSearch() != null)
            settings.setEnableSearch(request.getEnableSearch());
        if (request.getEnableSort() != null)
            settings.setEnableSort(request.getEnableSort());
        if (request.getEnablePagination() != null)
            settings.setEnablePagination(request.getEnablePagination());
        if (request.getPageSize() != null)
            settings.setPageSize(request.getPageSize());
        if (request.getAllowCreate() != null)
            settings.setAllowCreate(request.getAllowCreate());
        if (request.getAllowEdit() != null)
            settings.setAllowEdit(request.getAllowEdit());
        if (request.getAllowDelete() != null)
            settings.setAllowDelete(request.getAllowDelete());
        if (request.getAllowBulk() != null)
            settings.setAllowBulk(request.getAllowBulk());

        TableUISettings updated = uiSettingsService.saveSettings(settings);

        // Regenerate table-config.json
        try {
            configGeneratorService.regenerateTableConfig();
        } catch (Exception e) {
            System.err.println(
                    "[IntegratedTableService] Warning: Failed to regenerate table-config.json: " + e.getMessage());
        }

        Optional<TableSchemaInfo> schema = schemaService.getTableSchema(updated.getTableName());
        return TableDefinitionResponse.from(schema.get(), updated);
    }

    /**
     * Delete a table and its UI settings
     * 
     * @param id UI settings ID
     */
    @Transactional
    public void deleteTable(Long id) {
        TableUISettings settings = uiSettingsService.getSettingsById(id)
                .orElseThrow(() -> new IllegalArgumentException("UI settings not found: " + id));

        String tableName = settings.getTableName();

        // 1. Drop actual table
        dropTable(tableName);

        // 2. Delete UI settings
        uiSettingsService.deleteSettings(id);

        // 3. Regenerate table-config.json
        try {
            configGeneratorService.regenerateTableConfig();
        } catch (Exception e) {
            System.err.println(
                    "[IntegratedTableService] Warning: Failed to regenerate table-config.json: " + e.getMessage());
        }
    }

    /**
     * Create actual MySQL table
     */
    private void createActualTable(TableCreationRequest request) {
        StringBuilder sql = new StringBuilder("CREATE TABLE `")
                .append(request.getTableName())
                .append("` (");

        List<String> columnDefs = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();

        for (TableCreationRequest.ColumnDefinition column : request.getColumns()) {
            StringBuilder colDef = new StringBuilder("`")
                    .append(column.getColumnName())
                    .append("` ")
                    .append(column.getDataType());

            if (!column.getNullable()) {
                colDef.append(" NOT NULL");
            }

            if (column.getAutoIncrement()) {
                colDef.append(" AUTO_INCREMENT");
            }

            if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
                colDef.append(" DEFAULT ").append(column.getDefaultValue());
            }

            if (column.getComment() != null && !column.getComment().isEmpty()) {
                colDef.append(" COMMENT '").append(column.getComment()).append("'");
            }

            columnDefs.add(colDef.toString());

            if (column.getPrimaryKey()) {
                primaryKeys.add("`" + column.getColumnName() + "`");
            }
        }

        sql.append(String.join(", ", columnDefs));

        if (!primaryKeys.isEmpty()) {
            sql.append(", PRIMARY KEY (").append(String.join(", ", primaryKeys)).append(")");
        }

        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        jdbcTemplate.execute(sql.toString());
    }

    /**
     * Drop table
     */
    private void dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS `" + tableName + "`";
        jdbcTemplate.execute(sql);
    }

    /**
     * Check if table is a system table
     * System tables are defined in application.properties:
     * tablecraft.admin.system-tables
     */
    private boolean isSystemTable(String tableName) {
        if (systemTablesConfig == null || systemTablesConfig.trim().isEmpty()) {
            return false;
        }

        String[] systemTables = systemTablesConfig.split(",");
        for (String systemTable : systemTables) {
            if (tableName.equalsIgnoreCase(systemTable.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Table creation request DTO
     */
    public static class TableCreationRequest {
        private String tableName;
        private String displayName;
        private String description;
        private List<ColumnDefinition> columns;

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

        public List<ColumnDefinition> getColumns() {
            return columns;
        }

        public void setColumns(List<ColumnDefinition> columns) {
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

        public static class ColumnDefinition {
            private String columnName;
            private String dataType;
            private Boolean nullable = true;
            private Boolean primaryKey = false;
            private Boolean autoIncrement = false;
            private String defaultValue;
            private String comment;

            // Getters and Setters
            public String getColumnName() {
                return columnName;
            }

            public void setColumnName(String columnName) {
                this.columnName = columnName;
            }

            public String getDataType() {
                return dataType;
            }

            public void setDataType(String dataType) {
                this.dataType = dataType;
            }

            public Boolean getNullable() {
                return nullable;
            }

            public void setNullable(Boolean nullable) {
                this.nullable = nullable;
            }

            public Boolean getPrimaryKey() {
                return primaryKey;
            }

            public void setPrimaryKey(Boolean primaryKey) {
                this.primaryKey = primaryKey;
            }

            public Boolean getAutoIncrement() {
                return autoIncrement;
            }

            public void setAutoIncrement(Boolean autoIncrement) {
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
        }
    }

    /**
     * Table UI settings update request DTO
     */
    public static class TableUISettingsUpdateRequest {
        private String displayName;
        private String description;
        private Boolean enableSearch;
        private Boolean enableSort;
        private Boolean enablePagination;
        private Integer pageSize;
        private Boolean allowCreate;
        private Boolean allowEdit;
        private Boolean allowDelete;
        private Boolean allowBulk;

        // Getters and Setters
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
}
