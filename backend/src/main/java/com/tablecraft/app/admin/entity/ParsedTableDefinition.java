package com.tablecraft.app.admin.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 解析されたテーブル定義のエンティティ
 */
@Entity
@Table(name = "parsed_table_definitions")
public class ParsedTableDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sql_file_id", nullable = false)
    private Long sqlFileId;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "schema_name", length = 100)
    private String schemaName;

    @Lob
    @Column(name = "table_structure", nullable = false, columnDefinition = "JSON")
    private String tableStructure; // カラム定義、制約

    @Lob
    @Column(name = "foreign_keys", columnDefinition = "JSON")
    private String foreignKeys;

    @Lob
    @Column(name = "indexes", columnDefinition = "JSON")
    private String indexes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructor
    public ParsedTableDefinition() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSqlFileId() {
        return sqlFileId;
    }

    public void setSqlFileId(Long sqlFileId) {
        this.sqlFileId = sqlFileId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableStructure() {
        return tableStructure;
    }

    public void setTableStructure(String tableStructure) {
        this.tableStructure = tableStructure;
    }

    public String getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(String foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public String getIndexes() {
        return indexes;
    }

    public void setIndexes(String indexes) {
        this.indexes = indexes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
