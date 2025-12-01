package com.tablecraft.app.admin.repository;

import com.tablecraft.app.admin.entity.UploadedSqlFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UploadedSqlFileのリポジトリ
 */
@Repository
public interface UploadedSqlFileRepository extends JpaRepository<UploadedSqlFile, Long> {

    /**
     * ファイル名で検索
     */
    List<UploadedSqlFile> findByFileName(String fileName);

    /**
     * 解析ステータスで検索
     */
    List<UploadedSqlFile> findByParseStatus(String parseStatus);

    /**
     * アップロード日時の降順で全件取得
     */
    List<UploadedSqlFile> findAllByOrderByUploadedAtDesc();
}
