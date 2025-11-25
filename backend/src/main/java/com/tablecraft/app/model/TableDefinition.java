package com.tablecraft.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 個別テーブルの定義を表すモデルクラス
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableDefinition {
    private String name;
    private String displayName;
    private String description;
    private TableMetadata metadata;
    private PrimaryKeyDefinition primaryKey;
    private List<ColumnDefinition> columns;
    private List<ForeignKeyDefinition> foreignKeys;

    public TableDefinition() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public TableMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TableMetadata metadata) {
        this.metadata = metadata;
    }

    public PrimaryKeyDefinition getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKeyDefinition primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    /**
     * カラムをMapとして取得（backward compatibility用）
     */
    public Map<String, ColumnDefinition> getColumnsAsMap() {
        if (columns == null) {
            return new java.util.HashMap<>();
        }
        return columns.stream()
                .collect(Collectors.toMap(ColumnDefinition::getName, col -> col));
    }

    public List<ForeignKeyDefinition> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<ForeignKeyDefinition> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    @Override
    public String toString() {
        return "TableDefinition{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", metadata=" + metadata +
                ", primaryKey=" + primaryKey +
                ", columns=" + columns +
                ", foreignKeys=" + foreignKeys +
                '}';
    }
}