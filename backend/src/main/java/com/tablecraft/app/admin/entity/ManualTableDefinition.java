package com.tablecraft.app.admin.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 手動で作成されたテーブル定義のエンティティ
 */
@Entity
@Table(name = "manual_table_definitions")
public class ManualTableDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name", nullable = false, unique = true, length = 255)
    private String tableName;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "columns", nullable = false, columnDefinition = "TEXT")
    private String columns; // JSON形式のカラム定義（TEXT型で保存）
    
    // UI設定
    @Column(name = "enable_search")
    private Boolean enableSearch = true;
    
    @Column(name = "enable_sort")
    private Boolean enableSort = true;
    
    @Column(name = "enable_pagination")
    private Boolean enablePagination = true;
    
    @Column(name = "page_size")
    private Integer pageSize = 20;
    
    @Column(name = "allow_create")
    private Boolean allowCreate = true;
    
    @Column(name = "allow_edit")
    private Boolean allowEdit = true;
    
    @Column(name = "allow_delete")
    private Boolean allowDelete = true;
    
    @Column(name = "allow_bulk")
    private Boolean allowBulk = false;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor
    public ManualTableDefinition() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
