package com.tablecraft.app.admin.dto;

import java.util.List;

/**
 * DTO for table schema information from INFORMATION_SCHEMA
 */
public class TableSchemaInfo {
    private String tableName;
    private String tableSchema;
    private String tableComment;
    private List<ColumnSchemaInfo> columns;

    public TableSchemaInfo() {
    }

    public TableSchemaInfo(String tableName, String tableSchema, String tableComment, List<ColumnSchemaInfo> columns) {
        this.tableName = tableName;
        this.tableSchema = tableSchema;
        this.tableComment = tableComment;
        this.columns = columns;
    }

    // Getters and Setters
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public List<ColumnSchemaInfo> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnSchemaInfo> columns) {
        this.columns = columns;
    }

    /**
     * Column schema information
     */
    public static class ColumnSchemaInfo {
        private String columnName;
        private String dataType;
        private String columnType;
        private Boolean isNullable;
        private String columnKey;
        private String columnDefault;
        private String extra;
        private String columnComment;
        private Integer characterMaximumLength;
        private Integer numericPrecision;
        private Integer numericScale;

        public ColumnSchemaInfo() {
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

        public Boolean getIsNullable() {
            return isNullable;
        }

        public void setIsNullable(Boolean isNullable) {
            this.isNullable = isNullable;
        }

        public String getColumnKey() {
            return columnKey;
        }

        public void setColumnKey(String columnKey) {
            this.columnKey = columnKey;
        }

        public String getColumnDefault() {
            return columnDefault;
        }

        public void setColumnDefault(String columnDefault) {
            this.columnDefault = columnDefault;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }

        public String getColumnComment() {
            return columnComment;
        }

        public void setColumnComment(String columnComment) {
            this.columnComment = columnComment;
        }

        public Integer getCharacterMaximumLength() {
            return characterMaximumLength;
        }

        public void setCharacterMaximumLength(Integer characterMaximumLength) {
            this.characterMaximumLength = characterMaximumLength;
        }

        public Integer getNumericPrecision() {
            return numericPrecision;
        }

        public void setNumericPrecision(Integer numericPrecision) {
            this.numericPrecision = numericPrecision;
        }

        public Integer getNumericScale() {
            return numericScale;
        }

        public void setNumericScale(Integer numericScale) {
            this.numericScale = numericScale;
        }
    }
}
