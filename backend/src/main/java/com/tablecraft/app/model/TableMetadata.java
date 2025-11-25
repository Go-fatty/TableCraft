package com.tablecraft.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * テーブルメタデータを表すモデルクラス
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableMetadata {
    private String category;
    private String icon;
    private String color;
    private Integer sortOrder;
    private boolean isSystemTable;
    private String createdBy;
    private String lastModifiedBy;

    public TableMetadata() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isSystemTable() {
        return isSystemTable;
    }

    public void setSystemTable(boolean systemTable) {
        isSystemTable = systemTable;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public String toString() {
        return "TableMetadata{" +
                "category='" + category + '\'' +
                ", icon='" + icon + '\'' +
                ", color='" + color + '\'' +
                ", sortOrder=" + sortOrder +
                ", isSystemTable=" + isSystemTable +
                ", createdBy='" + createdBy + '\'' +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                '}';
    }
}