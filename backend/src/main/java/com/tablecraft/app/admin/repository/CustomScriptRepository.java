package com.tablecraft.app.admin.repository;

import com.tablecraft.app.admin.entity.CustomScript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CustomScriptのリポジトリ
 */
@Repository
public interface CustomScriptRepository extends JpaRepository<CustomScript, Long> {

    /**
     * スクリプト名で検索
     */
    Optional<CustomScript> findByScriptName(String scriptName);

    /**
     * スクリプトタイプで検索
     */
    List<CustomScript> findByScriptType(String scriptType);

    /**
     * アクティブなスクリプトを取得
     */
    List<CustomScript> findByIsActiveTrue();

    /**
     * スクリプトタイプでアクティブなものを取得
     */
    List<CustomScript> findByScriptTypeAndIsActiveTrue(String scriptType);
}
