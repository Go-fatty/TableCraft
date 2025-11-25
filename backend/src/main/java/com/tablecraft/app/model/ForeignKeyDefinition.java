package com.tablecraft.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 外部キー定義を表すモデルクラス
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForeignKeyDefinition {
    private String name;
    private String column;
    private String referencedTable;
    private String referencedColumn;
    private String onDelete; // CASCADE, RESTRICT, SET NULL
    private String onUpdate; // CASCADE, RESTRICT, SET NULL

    public ForeignKeyDefinition() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getReferencedTable() {
        return referencedTable;
    }

    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }

    public void setReferencedColumn(String referencedColumn) {
        this.referencedColumn = referencedColumn;
    }

    public String getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
    }

    public String getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(String onUpdate) {
        this.onUpdate = onUpdate;
    }

    @Override
    public String toString() {
        return "ForeignKeyDefinition{" +
                "name='" + name + '\'' +
                ", column='" + column + '\'' +
                ", referencedTable='" + referencedTable + '\'' +
                ", referencedColumn='" + referencedColumn + '\'' +
                ", onDelete='" + onDelete + '\'' +
                ", onUpdate='" + onUpdate + '\'' +
                '}';
    }
}