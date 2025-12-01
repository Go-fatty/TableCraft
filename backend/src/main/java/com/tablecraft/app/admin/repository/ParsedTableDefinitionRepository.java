package com.tablecraft.app.admin.repository;

import com.tablecraft.app.admin.entity.ParsedTableDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ParsedTableDefinitionのリポジトリ
 */
@Repository
public interface ParsedTableDefinitionRepository extends JpaRepository<ParsedTableDefinition, Long> {

    /**
     * SQLファイルIDで検索
     */
    List<ParsedTableDefinition> findBySqlFileId(Long sqlFileId);

    /**
     * テーブル名で検索
     */
    List<ParsedTableDefinition> findByTableName(String tableName);

    /**
     * SQLファイルIDとテーブル名で検索
     */
    Optional<ParsedTableDefinition> findBySqlFileIdAndTableName(Long sqlFileId, String tableName);
}
