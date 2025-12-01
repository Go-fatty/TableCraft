package com.tablecraft.app.admin.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * アクションとスクリプトの紐付けエンティティ
 */
@Entity
@Table(name = "action_script_bindings")
public class ActionScriptBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // 'button', 'before_save', 'after_save', etc.

    @Column(name = "action_name", length = 100)
    private String actionName; // ボタン名など

    @Column(name = "script_id", nullable = false)
    private Long scriptId;

    @Lob
    @Column(name = "binding_config", columnDefinition = "JSON")
    private String bindingConfig; // パラメータマッピング等

    @Column(name = "execution_order")
    private Integer executionOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor
    public ActionScriptBinding() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.executionOrder = 0;
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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Long getScriptId() {
        return scriptId;
    }

    public void setScriptId(Long scriptId) {
        this.scriptId = scriptId;
    }

    public String getBindingConfig() {
        return bindingConfig;
    }

    public void setBindingConfig(String bindingConfig) {
        this.bindingConfig = bindingConfig;
    }

    public Integer getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
}
