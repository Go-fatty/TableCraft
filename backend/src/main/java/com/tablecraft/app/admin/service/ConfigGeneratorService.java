package com.tablecraft.app.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tablecraft.app.admin.entity.ParsedTableDefinition;
import com.tablecraft.app.admin.repository.ParsedTableDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 設定ファイル生成サービス
 * ParsedTableDefinitionからtable-config.json等を生成
 */
@Service
public class ConfigGeneratorService {

    @Value("${tablecraft.admin.config.save-path:src/main/resources/config}")
    private String configSavePath;

    @Autowired
    private ParsedTableDefinitionRepository tableDefinitionRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.config.location:classpath:/config/}")
    private String configLocation;

    /**
     * table-config.jsonを生成
     */
    public Map<String, Object> generateTableConfig(List<Long> tableIds, Map<String, Object> customOptions) {
        Map<String, Object> config = new LinkedHashMap<>();
        List<Map<String, Object>> tables = new ArrayList<>();

        for (Long tableId : tableIds) {
            Optional<ParsedTableDefinition> optionalTable = tableDefinitionRepository.findById(tableId);
            if (optionalTable.isEmpty()) {
                continue;
            }

            ParsedTableDefinition tableDef = optionalTable.get();
            Map<String, Object> tableConfig = generateTableConfigEntry(tableDef, customOptions);
            tables.add(tableConfig);
        }

        config.put("tables", tables);
        config.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        config.put("version", "1.0.0");

        return config;
    }

    /**
     * 単一テーブルのtable-config.jsonエントリを生成
     */
    private Map<String, Object> generateTableConfigEntry(ParsedTableDefinition tableDef,
            Map<String, Object> customOptions) {
        Map<String, Object> tableConfig = new LinkedHashMap<>();

        tableConfig.put("id", tableDef.getTableName().toLowerCase());
        tableConfig.put("name", tableDef.getTableName());
        tableConfig.put("label", convertToLabel(tableDef.getTableName()));
        tableConfig.put("icon", customOptions.getOrDefault("defaultIcon", "Table"));

        // テーブル構造をパース
        JsonNode structure;
        try {
            structure = objectMapper.readTree(tableDef.getTableStructure());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse table structure", e);
        }

        // カラム定義を生成
        List<Map<String, Object>> columns = new ArrayList<>();
        JsonNode columnsNode = structure.get("columns");
        if (columnsNode != null && columnsNode.isArray()) {
            for (JsonNode colNode : columnsNode) {
                Map<String, Object> column = generateColumnConfig(colNode, customOptions);
                columns.add(column);
            }
        }
        tableConfig.put("columns", columns);

        // リストカラムを生成（最初の5カラムをデフォルト）
        List<String> listColumns = new ArrayList<>();
        int maxListColumns = (int) customOptions.getOrDefault("maxListColumns", 5);
        for (int i = 0; i < Math.min(columns.size(), maxListColumns); i++) {
            listColumns.add((String) columns.get(i).get("name"));
        }
        tableConfig.put("listColumns", listColumns);

        // 外部キー情報を追加
        try {
            JsonNode foreignKeys = objectMapper.readTree(tableDef.getForeignKeys());
            if (foreignKeys != null && foreignKeys.isArray() && foreignKeys.size() > 0) {
                tableConfig.put("foreignKeys", objectMapper.convertValue(foreignKeys, List.class));
            }
        } catch (Exception e) {
            // 外部キーがない場合はスキップ
        }

        return tableConfig;
    }

    /**
     * カラム定義を生成
     */
    private Map<String, Object> generateColumnConfig(JsonNode colNode, Map<String, Object> customOptions) {
        Map<String, Object> column = new LinkedHashMap<>();

        String columnName = colNode.get("name").asText();
        String columnType = colNode.get("type").asText();

        column.put("name", columnName);
        column.put("label", convertToLabel(columnName));
        column.put("type", mapColumnTypeToUiType(columnType));
        column.put("required", !colNode.get("nullable").asBoolean());

        // バリデーションルール
        List<String> validation = new ArrayList<>();
        if (!colNode.get("nullable").asBoolean()) {
            validation.add("required");
        }
        if (colNode.has("length")) {
            String length = colNode.get("length").asText();
            validation.add("maxLength:" + length.split(",")[0]);
        }
        if (colNode.get("unique").asBoolean()) {
            validation.add("unique");
        }
        column.put("validation", validation);

        // デフォルト値
        if (colNode.has("defaultValue")) {
            column.put("defaultValue", colNode.get("defaultValue").asText());
        }

        // プライマリキー
        if (colNode.get("primaryKey").asBoolean()) {
            column.put("primaryKey", true);
            column.put("editable", false);
        }

        // AUTO_INCREMENT
        if (colNode.get("autoIncrement").asBoolean()) {
            column.put("autoIncrement", true);
        }

        // コメント（ラベルとして使用）
        if (colNode.has("comment")) {
            column.put("label", colNode.get("comment").asText());
        }

        return column;
    }

    /**
     * SQLのカラム型をUI用の型にマッピング
     */
    private String mapColumnTypeToUiType(String sqlType) {
        sqlType = sqlType.toUpperCase();

        // 数値型
        if (sqlType.matches("(TINYINT|SMALLINT|MEDIUMINT|INT|INTEGER|BIGINT)")) {
            return "number";
        }
        if (sqlType.matches("(DECIMAL|NUMERIC|FLOAT|DOUBLE|REAL)")) {
            return "decimal";
        }

        // 文字列型
        if (sqlType.matches("(CHAR|VARCHAR|TEXT|TINYTEXT|MEDIUMTEXT|LONGTEXT)")) {
            return "text";
        }

        // 日付時刻型
        if (sqlType.equals("DATE")) {
            return "date";
        }
        if (sqlType.matches("(DATETIME|TIMESTAMP)")) {
            return "datetime";
        }
        if (sqlType.equals("TIME")) {
            return "time";
        }

        // 真偽値型
        if (sqlType.matches("(BOOLEAN|BOOL|BIT)")) {
            return "boolean";
        }

        // バイナリ型
        if (sqlType.matches("(BLOB|BINARY|VARBINARY|TINYBLOB|MEDIUMBLOB|LONGBLOB)")) {
            return "file";
        }

        // JSON型
        if (sqlType.equals("JSON")) {
            return "json";
        }

        // デフォルトはtext
        return "text";
    }

    /**
     * カラム名をラベルに変換（snake_case -> Title Case）
     */
    private String convertToLabel(String name) {
        // snake_caseをTitle Caseに変換
        String[] words = name.split("_");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (label.length() > 0) {
                label.append(" ");
            }
            if (word.length() > 0) {
                label.append(word.substring(0, 1).toUpperCase());
                if (word.length() > 1) {
                    label.append(word.substring(1).toLowerCase());
                }
            }
        }
        return label.toString();
    }

    /**
     * 生成した設定をファイルに保存
     */
    public void saveConfigToFile(Map<String, Object> config, String filename) throws IOException {
        // 保存先パスを解決
        Path configPath = resolveConfigPath();
        Path targetFile = configPath.resolve(filename);

        System.out.println("[ConfigGeneratorService] 設定ファイルを保存: " + targetFile.toAbsolutePath());

        // 既存ファイルがあればバックアップ
        if (Files.exists(targetFile)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path backupFile = configPath.resolve(filename + "." + timestamp + ".bak");
            Files.copy(targetFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // JSON書き込み
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(targetFile.toFile(), config);
        System.out.println(
                "[ConfigGeneratorService] ✅ 保存完了: " + filename + " (size: " + Files.size(targetFile) + " bytes)");
    }

    /**
     * 既存の設定ファイルを読み込み
     */
    public Map<String, Object> loadConfigFromFile(String filename) throws IOException {
        Path configPath = resolveConfigPath();
        Path targetFile = configPath.resolve(filename);

        if (!Files.exists(targetFile)) {
            throw new IOException("Config file not found: " + filename);
        }

        return objectMapper.readValue(targetFile.toFile(), Map.class);
    }

    /**
     * デフォルト設定ファイルを読み込み
     */
    public Map<String, Object> loadDefaultConfig(String filename) throws IOException {
        try {
            // クラスパスからデフォルト設定を読み込み
            var resource = getClass().getClassLoader().getResourceAsStream(filename);
            if (resource == null) {
                throw new IOException("Default config file not found: " + filename);
            }
            return objectMapper.readValue(resource, Map.class);
        } catch (Exception e) {
            System.err.println("[ConfigGeneratorService] Failed to load default config: " + filename);
            throw new IOException("Failed to load default config: " + filename, e);
        }
    }

    /**
     * テーブルテンプレートを読み込み
     */
    public Map<String, Object> loadTableTemplates() throws IOException {
        return loadDefaultConfig("table-templates.json");
    }

    /**
     * デフォルト設定で初期化
     * 設定ファイルが存在しない場合にデフォルト値をコピー
     */
    public void initializeWithDefaults() {
        try {
            Path configPath = resolveConfigPath();

            // table-config.json が存在しない場合はデフォルトをコピー
            Path tableConfigFile = configPath.resolve("table-config.json");
            if (!Files.exists(tableConfigFile)) {
                Map<String, Object> defaultConfig = loadDefaultConfig("default-table-config.json");
                saveConfigToFile(defaultConfig, "table-config.json");
                System.out.println("[ConfigGeneratorService] Initialized table-config.json with defaults");
            }

            // ui-config.json が存在しない場合はデフォルトをコピー
            Path uiConfigFile = configPath.resolve("ui-config.json");
            if (!Files.exists(uiConfigFile)) {
                Map<String, Object> defaultConfig = loadDefaultConfig("default-ui-config.json");
                saveConfigToFile(defaultConfig, "ui-config.json");
                System.out.println("[ConfigGeneratorService] Initialized ui-config.json with defaults");
            }

            // validation-config.json が存在しない場合はデフォルトをコピー
            Path validationConfigFile = configPath.resolve("validation-config.json");
            if (!Files.exists(validationConfigFile)) {
                Map<String, Object> defaultConfig = loadDefaultConfig("default-validation-config.json");
                saveConfigToFile(defaultConfig, "validation-config.json");
                System.out.println("[ConfigGeneratorService] Initialized validation-config.json with defaults");
            }

        } catch (Exception e) {
            System.err.println("[ConfigGeneratorService] Failed to initialize with defaults: " + e.getMessage());
        }
    }

    /**
     * 設定ファイルのパスを解決
     * application.propertiesで設定されたパスを使用
     */
    private Path resolveConfigPath() {
        // プロパティファイルから保存先パスを取得
        Path resourcesConfigPath = Paths.get(configSavePath);

        System.out.println("[ConfigGeneratorService] 保存先パス: " + resourcesConfigPath.toAbsolutePath());

        // ディレクトリが存在しない場合は作成
        if (!Files.exists(resourcesConfigPath)) {
            try {
                Files.createDirectories(resourcesConfigPath);
                System.out.println("[ConfigGeneratorService] Created config directory: "
                        + resourcesConfigPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("[ConfigGeneratorService] Failed to create config directory: " + e.getMessage());
            }
        }

        return resourcesConfigPath;
    }

    /**
     * ui-config.jsonを生成
     */
    public Map<String, Object> generateUiConfig(List<Long> tableIds, Map<String, Object> customOptions) {
        Map<String, Object> uiConfig = new LinkedHashMap<>();

        // テーブルごとのUI設定
        List<Map<String, Object>> tableUiConfigs = new ArrayList<>();
        for (Long tableId : tableIds) {
            Optional<ParsedTableDefinition> optionalTable = tableDefinitionRepository.findById(tableId);
            if (optionalTable.isEmpty()) {
                continue;
            }

            ParsedTableDefinition tableDef = optionalTable.get();
            Map<String, Object> tableUi = new LinkedHashMap<>();
            tableUi.put("tableId", tableDef.getTableName().toLowerCase());
            tableUi.put("displayName", convertToLabel(tableDef.getTableName()));
            tableUi.put("icon", customOptions.getOrDefault("defaultIcon", "Table"));
            tableUi.put("color", customOptions.getOrDefault("defaultColor", "blue"));
            tableUi.put("pageSize", customOptions.getOrDefault("defaultPageSize", 20));
            tableUi.put("sortable", true);
            tableUi.put("filterable", true);
            tableUi.put("exportable", true);

            tableUiConfigs.add(tableUi);
        }

        uiConfig.put("tables", tableUiConfigs);
        uiConfig.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return uiConfig;
    }

    /**
     * validation-config.jsonを生成
     */
    public Map<String, Object> generateValidationConfig(List<Long> tableIds) {
        Map<String, Object> validationConfig = new LinkedHashMap<>();
        Map<String, Map<String, Object>> tableValidations = new LinkedHashMap<>();

        for (Long tableId : tableIds) {
            Optional<ParsedTableDefinition> optionalTable = tableDefinitionRepository.findById(tableId);
            if (optionalTable.isEmpty()) {
                continue;
            }

            ParsedTableDefinition tableDef = optionalTable.get();
            Map<String, Object> tableValidation = new LinkedHashMap<>();
            Map<String, List<Map<String, Object>>> columnValidations = new LinkedHashMap<>();

            try {
                JsonNode structure = objectMapper.readTree(tableDef.getTableStructure());
                JsonNode columnsNode = structure.get("columns");

                if (columnsNode != null && columnsNode.isArray()) {
                    for (JsonNode colNode : columnsNode) {
                        String columnName = colNode.get("name").asText();
                        List<Map<String, Object>> rules = new ArrayList<>();

                        // required
                        if (!colNode.get("nullable").asBoolean()) {
                            rules.add(Map.of("type", "required", "message", columnName + " is required"));
                        }

                        // maxLength
                        if (colNode.has("length")) {
                            String length = colNode.get("length").asText();
                            int maxLength = Integer.parseInt(length.split(",")[0]);
                            rules.add(Map.of(
                                    "type", "maxLength",
                                    "value", maxLength,
                                    "message", columnName + " must be at most " + maxLength + " characters"));
                        }

                        // unique
                        if (colNode.get("unique").asBoolean()) {
                            rules.add(Map.of("type", "unique", "message", columnName + " must be unique"));
                        }

                        if (!rules.isEmpty()) {
                            columnValidations.put(columnName, rules);
                        }
                    }
                }
            } catch (Exception e) {
                // エラーはスキップ
            }

            tableValidation.put("columns", columnValidations);
            tableValidations.put(tableDef.getTableName().toLowerCase(), tableValidation);
        }

        validationConfig.put("tables", tableValidations);
        validationConfig.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return validationConfig;
    }

    /**
     * table-config.jsonを保存
     */
    public void saveTableConfig(Map<String, Object> config) throws IOException {
        Path configPath = resolveConfigPath();
        Path tableConfigFile = configPath.resolve("table-config.json");
        
        // 1. src/main/resources/config/table-config.json に保存（業務画面が読み込む）
        Path resourcesConfigPath = Paths.get("backend", "src", "main", "resources", "config");
        if (!Files.exists(resourcesConfigPath)) {
            Files.createDirectories(resourcesConfigPath);
        }
        Path resourcesTableConfig = resourcesConfigPath.resolve("table-config.json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(resourcesTableConfig.toFile(), config);
        System.out.println("✅ table-config.json saved to: " + resourcesTableConfig);
        
        // 2. frontend/public にも保存
        Path frontendPublicPath = Paths.get("frontend", "public");
        if (Files.exists(frontendPublicPath)) {
            Path frontendTableConfig = frontendPublicPath.resolve("table-config.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(frontendTableConfig.toFile(), config);
            System.out.println("✅ table-config.json saved to: " + frontendTableConfig);
        }
        
        // 3. bin/config にも保存（デフォルト）
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(tableConfigFile.toFile(), config);
        System.out.println("✅ table-config.json saved to: " + tableConfigFile);
    }
}
