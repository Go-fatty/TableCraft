package com.tablecraft.app.admin.dto;

import com.tablecraft.app.admin.entity.TableUISettings;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Combined table definition response (schema + UI settings)
 */
public class TableDefinitionResponse {
    private Long id;
    private String tableName;
    private String displayName;
    private String description;
    private List<ColumnInfo> columns;
    
    // UI Settings
    private Boolean enableSearch;
    private Boolean enableSort;
    private Boolean enablePagination;
    private Integer pageSize;
    private Boolean allowCreate;
    private Boolean allowEdit;
    private Boolean allowDelete;
    private Boolean allowBulk;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TableDefinitionResponse() {
    }

    /**
     * Create response from schema and UI settings
     */
    public static TableDefinitionResponse from(TableSchemaInfo schema, TableUISettings settings) {
        TableDefinitionResponse response = new TableDefinitionResponse();
        
        // Schema info
        response.setTableName(schema.getTableName());
        
        // UI settings
        if (settings != null) {
            response.setId(settings.getId());
            response.setDisplayName(settings.getDisplayName());
            response.setDescription(settings.getDescription());
            response.setEnableSearch(settings.getEnableSearch());
            response.setEnableSort(settings.getEnableSort());
            response.setEnablePagination(settings.getEnablePagination());
            response.setPageSize(settings.getPageSize());
            response.setAllowCreate(settings.getAllowCreate());
            response.setAllowEdit(settings.getAllowEdit());
            response.setAllowDelete(settings.getAllowDelete());
            response.setAllowBulk(settings.getAllowBulk());
            response.setCreatedAt(settings.getCreatedAt());
            response.setUpdatedAt(settings.getUpdatedAt());
        } else {
            // Default values if no UI settings exist
            response.setDisplayName(schema.getTableName());
            response.setEnableSearch(true);
            response.setEnableSort(true);
            response.setEnablePagination(true);
            response.setPageSize(20);
            response.setAllowCreate(true);
            response.setAllowEdit(true);
            response.setAllowDelete(true);
            response.setAllowBulk(false);
        }
        
        // Convert columns
        if (schema.getColumns() != null) {
            response.setColumns(schema.getColumns().stream()
                .map(ColumnInfo::from)
                .collect(java.util.stream.Collectors.toList()));
        }
        
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnInfo> columns) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Column information for response
     */
    public static class ColumnInfo {
        private String columnName;
        private String dataType;
        private String columnType;
        private Boolean nullable;
        private Boolean primaryKey;
        private Boolean autoIncrement;
        private String defaultValue;
        private String comment;

        public static ColumnInfo from(TableSchemaInfo.ColumnSchemaInfo schemaColumn) {
            ColumnInfo info = new ColumnInfo();
            info.setColumnName(schemaColumn.getColumnName());
            info.setDataType(schemaColumn.getDataType());
            info.setColumnType(schemaColumn.getColumnType());
            info.setNullable(schemaColumn.getIsNullable());
            info.setPrimaryKey("PRI".equals(schemaColumn.getColumnKey()));
            info.setAutoIncrement(schemaColumn.getExtra() != null && 
                                  schemaColumn.getExtra().contains("auto_increment"));
            info.setDefaultValue(schemaColumn.getColumnDefault());
            info.setComment(schemaColumn.getColumnComment());
            return info;
        }

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

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
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
