package com.tablecraft.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Arrays;

/**
 * プライマリキー定義を表すモデルクラス
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrimaryKeyDefinition {
    private String type; // "single" または "composite"
    private List<String> columns;

    @JsonProperty("column")
    private String singleColumn; // JSON設定の"column"フィールドをサポート

    public PrimaryKeyDefinition() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getColumns() {
        // columnsが設定されていない場合は、singleColumnから生成
        if (columns == null && singleColumn != null) {
            return Arrays.asList(singleColumn);
        }
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getSingleColumn() {
        return singleColumn;
    }

    public void setSingleColumn(String singleColumn) {
        this.singleColumn = singleColumn;
    }

    /**
     * 単一主キーかどうかを判定
     */
    public boolean isSingle() {
        return "single".equals(type);
    }

    /**
     * 複合主キーかどうかを判定
     */
    public boolean isComposite() {
        return "composite".equals(type);
    }

    /**
     * プライマリキーカラム名を取得（単一主キーの場合）
     */
    public String getSingleColumnName() {
        List<String> cols = getColumns();
        if (isSingle() && cols != null && !cols.isEmpty()) {
            return cols.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        return "PrimaryKeyDefinition{" +
                "type='" + type + '\'' +
                ", columns=" + getColumns() +
                '}';
    }
}