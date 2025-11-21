package com.tablecraft.app.dynamic;

import java.util.List;

public class FieldDefinition {
    private String type;
    private List<String> attributes;

    public FieldDefinition(String type, List<String> attributes) {
        this.type = type;
        this.attributes = attributes;
    }

    public String getType() {
        return type;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public String getSqlType() {
        switch (type.toUpperCase()) {
            case "STRING":
                return "VARCHAR(255)";
            case "LONG":
                return "BIGINT" + (attributes.contains("AUTO_INCREMENT") ? " AUTO_INCREMENT" : "");
            case "INTEGER":
                return "INT";
            case "DECIMAL":
                return "DECIMAL(10,2)";
            case "DATE":
                return "DATE";
            case "DATETIME":
                return "DATETIME";
            case "BOOLEAN":
                return "BOOLEAN";
            default:
                return "VARCHAR(255)";
        }
    }

    public boolean isPrimaryKey() {
        return attributes.contains("PRIMARY_KEY");
    }

    public boolean isNotNull() {
        return attributes.contains("NOT_NULL");
    }

    public boolean isAutoIncrement() {
        return attributes.contains("AUTO_INCREMENT");
    }

    public String getForeignKeyTable() {
        for (String attr : attributes) {
            if (attr.startsWith("FOREIGN_KEY:")) {
                return attr.substring("FOREIGN_KEY:".length());
            }
        }
        return null;
    }
}