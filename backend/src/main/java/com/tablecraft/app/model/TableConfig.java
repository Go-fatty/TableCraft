package com.tablecraft.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * テーブル設定の全体構成を表すモデルクラス
 * table-config.jsonの構造に対応
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableConfig {
    private String version;
    private String generated;
    private ProjectInfo project;
    private DatabaseInfo database;
    private Map<String, TableDefinition> tables;

    public TableConfig() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGenerated() {
        return generated;
    }

    public void setGenerated(String generated) {
        this.generated = generated;
    }

    public ProjectInfo getProject() {
        return project;
    }

    public void setProject(ProjectInfo project) {
        this.project = project;
    }

    public DatabaseInfo getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseInfo database) {
        this.database = database;
    }

    public Map<String, TableDefinition> getTables() {
        return tables;
    }

    public void setTables(Map<String, TableDefinition> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return "TableConfig{" +
                "version='" + version + '\'' +
                ", generated='" + generated + '\'' +
                ", project=" + project +
                ", database=" + database +
                ", tables=" + tables +
                '}';
    }

    /**
     * プロジェクト情報
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectInfo {
        private String name;
        private String description;

        public ProjectInfo() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "ProjectInfo{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    /**
     * データベース情報
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DatabaseInfo {
        private String type;
        private String charset;
        private String collation;

        public DatabaseInfo() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getCollation() {
            return collation;
        }

        public void setCollation(String collation) {
            this.collation = collation;
        }

        @Override
        public String toString() {
            return "DatabaseInfo{" +
                    "type='" + type + '\'' +
                    ", charset='" + charset + '\'' +
                    ", collation='" + collation + '\'' +
                    '}';
        }
    }
}