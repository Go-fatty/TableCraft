package com.tablecraft.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * カラム定義を表すモデルクラス
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ColumnDefinition {
    private String name;
    private String displayName;
    private String type;
    private Integer size;
    private Integer scale;
    private boolean nullable;
    private String defaultValue;
    private boolean autoIncrement;
    private String comment;

    public ColumnDefinition() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * sizeのエイリアスメソッド（下位互換性のため）
     */
    public Integer getLength() {
        return getSize();
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "ColumnDefinition{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", scale=" + scale +
                ", nullable=" + nullable +
                ", defaultValue='" + defaultValue + '\'' +
                ", autoIncrement=" + autoIncrement +
                ", comment='" + comment + '\'' +
                '}';
    }
}