package com.tablecraft.app.admin.service;

import com.tablecraft.app.admin.entity.ParsedTableDefinition;
import com.tablecraft.app.admin.entity.UploadedSqlFile;
import com.tablecraft.app.admin.repository.ParsedTableDefinitionRepository;
import com.tablecraft.app.admin.repository.UploadedSqlFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SQLファイル管理サービス
 */
@Service
public class SqlFileService {

    @Autowired
    private UploadedSqlFileRepository sqlFileRepository;

    @Autowired
    private ParsedTableDefinitionRepository tableDefinitionRepository;

    @Autowired
    private SqlParserService sqlParserService;

    @Autowired
    private EntityManager entityManager;

    /**
     * SQLファイルをアップロードして保存
     */
    @Transactional
    public UploadedSqlFile uploadSqlFile(MultipartFile file, String sqlType, String dbmsType, String uploadedBy)
            throws IOException {
        // ファイル内容を読み込み
        String sqlContent = new String(file.getBytes(), StandardCharsets.UTF_8);

        // エンティティ作成
        UploadedSqlFile sqlFile = new UploadedSqlFile();
        sqlFile.setFileName(file.getOriginalFilename());
        sqlFile.setFileSize(file.getSize());
        sqlFile.setSqlContent(sqlContent);
        sqlFile.setSqlType(sqlType);
        sqlFile.setDbmsType(dbmsType);
        sqlFile.setParseStatus("pending");
        sqlFile.setUploadedBy(uploadedBy);
        sqlFile.setUploadedAt(LocalDateTime.now());

        return sqlFileRepository.save(sqlFile);
    }

    /**
     * SQLファイルを解析してテーブル定義を抽出
     */
    @Transactional
    public UploadedSqlFile parseSqlFile(Long sqlFileId) {
        Optional<UploadedSqlFile> optionalFile = sqlFileRepository.findById(sqlFileId);
        if (optionalFile.isEmpty()) {
            throw new IllegalArgumentException("SQL file not found: " + sqlFileId);
        }

        UploadedSqlFile sqlFile = optionalFile.get();

        try {
            // SQL解析
            List<ParsedTableDefinition> definitions = sqlParserService.parseAllTables(
                    sqlFile.getSqlContent(),
                    sqlFileId);

            // 既存の解析結果を削除
            List<ParsedTableDefinition> existing = tableDefinitionRepository.findBySqlFileId(sqlFileId);
            if (!existing.isEmpty()) {
                tableDefinitionRepository.deleteAll(existing);
            }

            // 新しい解析結果を保存
            tableDefinitionRepository.saveAll(definitions);

            // ステータス更新
            sqlFile.setParseStatus("parsed");
            sqlFile.setParseResult("{\"tableCount\": " + definitions.size() + "}");
            sqlFile.setErrorMessage(null);

        } catch (Exception e) {
            // エラーハンドリング
            sqlFile.setParseStatus("error");
            sqlFile.setErrorMessage(e.getMessage());
        }

        return sqlFileRepository.save(sqlFile);
    }

    /**
     * アップロード済みSQLファイル一覧を取得
     */
    public List<UploadedSqlFile> listAllSqlFiles() {
        return sqlFileRepository.findAllByOrderByUploadedAtDesc();
    }

    /**
     * 特定のSQLファイルを取得
     */
    public Optional<UploadedSqlFile> getSqlFile(Long sqlFileId) {
        return sqlFileRepository.findById(sqlFileId);
    }

    /**
     * 特定のSQLファイルに紐づくテーブル定義を取得
     */
    public List<ParsedTableDefinition> getTableDefinitions(Long sqlFileId) {
        return tableDefinitionRepository.findBySqlFileId(sqlFileId);
    }

    /**
     * 特定のテーブル定義を取得
     */
    public Optional<ParsedTableDefinition> getTableDefinition(Long sqlFileId, String tableName) {
        return tableDefinitionRepository.findBySqlFileIdAndTableName(sqlFileId, tableName);
    }

    /**
     * SQLファイルを削除（関連するテーブル定義も削除）
     */
    @Transactional
    public void deleteSqlFile(Long sqlFileId) {
        // 関連テーブル定義を削除
        List<ParsedTableDefinition> definitions = tableDefinitionRepository.findBySqlFileId(sqlFileId);
        if (!definitions.isEmpty()) {
            tableDefinitionRepository.deleteAll(definitions);
        }

        // SQLファイルを削除
        sqlFileRepository.deleteById(sqlFileId);
    }

    /**
     * SQLファイル名で検索
     */
    public List<UploadedSqlFile> findByFileName(String fileName) {
        return sqlFileRepository.findByFileName(fileName);
    }

    /**
     * 解析ステータスで検索
     */
    public List<UploadedSqlFile> findByParseStatus(String parseStatus) {
        return sqlFileRepository.findByParseStatus(parseStatus);
    }

    /**
     * 全てのSQLファイルとテーブル定義をクリア
     */
    @Transactional
    public void clearAll() {
        // 外部キー制約を考慮して、まずテーブル定義を削除
        entityManager.createNativeQuery("DELETE FROM parsed_table_definitions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM uploaded_sql_files").executeUpdate();
    }
}
