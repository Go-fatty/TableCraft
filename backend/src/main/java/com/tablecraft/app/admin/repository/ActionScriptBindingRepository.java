package com.tablecraft.app.admin.repository;

import com.tablecraft.app.admin.entity.ActionScriptBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ActionScriptBindingのリポジトリ
 */
@Repository
public interface ActionScriptBindingRepository extends JpaRepository<ActionScriptBinding, Long> {

    /**
     * テーブル名で検索
     */
    List<ActionScriptBinding> findByTableName(String tableName);

    /**
     * スクリプトIDで検索
     */
    List<ActionScriptBinding> findByScriptId(Long scriptId);

    /**
     * テーブル名とアクションタイプで検索
     */
    List<ActionScriptBinding> findByTableNameAndActionType(String tableName, String actionType);

    /**
     * アクティブな紐付けを取得
     */
    List<ActionScriptBinding> findByIsActiveTrueOrderByExecutionOrder();

    /**
     * テーブル名でアクティブな紐付けを取得
     */
    List<ActionScriptBinding> findByTableNameAndIsActiveTrueOrderByExecutionOrder(String tableName);
}
