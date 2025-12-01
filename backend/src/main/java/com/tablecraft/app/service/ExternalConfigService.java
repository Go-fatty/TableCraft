package com.tablecraft.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tablecraft.app.model.TableConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 外部設定ファイル管理サービス
 * JSONファイルの読み込み、ホットリロード、バリデーションを担当
 */
@Service
public class ExternalConfigService {

    @Value("${tablecraft.admin.config.save-path:src/main/resources/config}")
    private String externalConfigPath;

    @Value("${tablecraft.config.enable-external:true}")
    private boolean enableExternalConfig;

    @Value("${tablecraft.config.table-config-file:table-config.json}")
    private String tableConfigFileName;

    private final ObjectMapper objectMapper;
    private TableConfig cachedTableConfig;
    private long lastModified = 0;

    public ExternalConfigService() {
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void initialize() {
        loadTableConfig();
    }

    /**
     * テーブル設定を読み込み（キャッシュ機能付き）
     */
    public TableConfig getTableConfig() {
        if (shouldReload()) {
            loadTableConfig();
        }
        return cachedTableConfig;
    }

    /**
     * 設定ファイルを強制的にリロード
     */
    public void reloadTableConfig() {
        cachedTableConfig = null;
        lastModified = 0;
        loadTableConfig();
    }

    /**
     * 設定ファイルが更新されているかチェック
     */
    private boolean shouldReload() {
        if (cachedTableConfig == null) {
            return true;
        }

        try {
            Path configPath = getConfigFilePath();
            if (configPath != null && Files.exists(configPath)) {
                long currentModified = Files.getLastModifiedTime(configPath).toMillis();
                return currentModified > lastModified;
            }
        } catch (Exception e) {
            System.err.println("設定ファイルの更新時刻チェックに失敗: " + e.getMessage());
        }

        return false;
    }

    /**
     * テーブル設定ファイルを読み込み
     */
    private synchronized void loadTableConfig() {
        try {
            System.out.println("テーブル設定ファイルを読み込み中...");

            String configContent = readConfigFile();
            if (configContent == null) {
                throw new RuntimeException("設定ファイルの読み込みに失敗しました");
            }

            cachedTableConfig = objectMapper.readValue(configContent, TableConfig.class);
            updateLastModifiedTime();

            System.out.println("✅ テーブル設定ファイルの読み込み完了: " + cachedTableConfig.getTables().size() + " テーブル");

        } catch (Exception e) {
            System.err.println("❌ テーブル設定ファイルの読み込みエラー: " + e.getMessage());
            e.printStackTrace();

            // フォールバック: デフォルト設定を使用
            cachedTableConfig = createDefaultTableConfig();
        }
    }

    /**
     * 設定ファイルの内容を読み込み
     */
    private String readConfigFile() throws IOException {
        // 1. 外部設定ファイルを優先的に読み込み
        if (enableExternalConfig) {
            Path externalFilePath = Paths.get(externalConfigPath, tableConfigFileName);
            if (Files.exists(externalFilePath)) {
                System.out.println("外部設定ファイルを読み込み: " + externalFilePath.toAbsolutePath());
                return Files.readString(externalFilePath, java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        // 2. クラスパス内の設定ファイルをフォールバック
        String classPathFile = "config/" + tableConfigFileName;
        System.out.println("クラスパス設定ファイルを読み込み: " + classPathFile);

        Resource resource = new ClassPathResource(classPathFile);
        if (resource.exists()) {
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    /**
     * 設定ファイルのパスを取得
     */
    private Path getConfigFilePath() {
        if (enableExternalConfig) {
            Path externalPath = Paths.get(externalConfigPath, tableConfigFileName);
            if (Files.exists(externalPath)) {
                return externalPath;
            }
        }
        return null;
    }

    /**
     * 最終更新時刻を更新
     */
    private void updateLastModifiedTime() {
        try {
            Path configPath = getConfigFilePath();
            if (configPath != null) {
                lastModified = Files.getLastModifiedTime(configPath).toMillis();
            }
        } catch (Exception e) {
            System.err.println("最終更新時刻の更新に失敗: " + e.getMessage());
        }
    }

    /**
     * デフォルトのテーブル設定を作成（フォールバック用）
     */
    private TableConfig createDefaultTableConfig() {
        TableConfig config = new TableConfig();
        config.setVersion("1.0.0-fallback");

        TableConfig.ProjectInfo project = new TableConfig.ProjectInfo();
        project.setName("TableCraft Default");
        project.setDescription("Fallback configuration");
        config.setProject(project);

        TableConfig.DatabaseInfo database = new TableConfig.DatabaseInfo();
        database.setType("mysql");
        database.setCharset("utf8mb4");
        database.setCollation("utf8mb4_unicode_ci");
        config.setDatabase(database);

        // 空のテーブルマップ
        config.setTables(new java.util.HashMap<>());

        return config;
    }

    /**
     * 設定の有効性をチェック
     */
    public boolean isConfigValid() {
        return cachedTableConfig != null && cachedTableConfig.getTables() != null;
    }

    /**
     * 設定情報のサマリーを取得
     */
    public String getConfigSummary() {
        if (cachedTableConfig == null) {
            return "設定ファイル未読み込み";
        }

        return String.format("プロジェクト: %s, バージョン: %s, テーブル数: %d",
                cachedTableConfig.getProject() != null ? cachedTableConfig.getProject().getName() : "N/A",
                cachedTableConfig.getVersion(),
                cachedTableConfig.getTables() != null ? cachedTableConfig.getTables().size() : 0);
    }
}