package com.tablecraft.app.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tablecraft.app.admin.dto.TableSchemaInfo;
import com.tablecraft.app.admin.entity.TableUISettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Configuration file generation service
 * Generates table-config.json from INFORMATION_SCHEMA and UI settings
 */
@Service
public class ConfigGeneratorService {

    @Value("${tablecraft.admin.config.save-path:src/main/resources/config}")
    private String configSavePath;

    @Value("${tablecraft.admin.system-tables:manual_table_definitions,parsed_table_definitions,table_ui_settings}")
    private String systemTablesConfig;

    @Autowired
    private TableSchemaService tableSchemaService;

    @Autowired
    private TableUISettingsService tableUISettingsService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Regenerate table-config.json from INFORMATION_SCHEMA and UI settings
     * Used after table creation/update/deletion
     */
    public void regenerateTableConfig() throws IOException {
        try {
            System.out.println("[ConfigGeneratorService] Regenerating table-config.json from INFORMATION_SCHEMA");

            // Get all tables from INFORMATION_SCHEMA
            List<String> tableNames = tableSchemaService.getAllTableNames();

            // Filter out system tables
            List<String> userTables = tableNames.stream()
                    .filter(name -> !isSystemTable(name))
                    .collect(java.util.stream.Collectors.toList());

            System.out.println("[ConfigGeneratorService] Found " + tableNames.size() + " total tables, "
                    + userTables.size() + " user tables");

            // Build table-config.json structure
            Map<String, Object> config = new LinkedHashMap<>();
            Map<String, Object> tablesMap = new LinkedHashMap<>();

            for (String tableName : userTables) {
                Optional<TableSchemaInfo> schemaOpt = tableSchemaService.getTableSchema(tableName);
                if (!schemaOpt.isPresent())
                    continue;

                TableSchemaInfo schema = schemaOpt.get();
                Optional<TableUISettings> uiSettings = tableUISettingsService.getSettingsByTableName(tableName);

                Map<String, Object> tableConfig = buildTableConfig(schema, uiSettings.orElse(null));
                tablesMap.put(tableName, tableConfig);
            }

            config.put("tables", tablesMap);

            // Write to multiple locations
            // 1. src/main/resources/config/table-config.json
            String configPath = "src/main/resources/config/table-config.json";
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(configPath), config);
            System.out.println("[ConfigGeneratorService] table-config.json saved to: " + configPath);

            // 2. frontend/public/table-config.json (for business UI)
            List<Path> frontendPaths = Arrays.asList(
                    Paths.get("frontend", "public"),
                    Paths.get("..", "frontend", "public"),
                    Paths.get(".", "frontend", "public"));

            boolean frontendSaved = false;
            for (Path frontendPublicPath : frontendPaths) {
                if (Files.exists(frontendPublicPath)) {
                    Path frontendTableConfig = frontendPublicPath.resolve("table-config.json");
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(frontendTableConfig.toFile(), config);
                    System.out.println("[ConfigGeneratorService] table-config.json saved to: "
                            + frontendTableConfig.toAbsolutePath());
                    frontendSaved = true;
                    break;
                }
            }

            if (!frontendSaved) {
                System.out.println("[ConfigGeneratorService] Warning: Could not find frontend/public directory");
            }

            // 3. bin/config/table-config.json (runtime config)
            Path binConfigPath = Paths.get("bin", "config");
            if (Files.exists(binConfigPath)) {
                Path binTableConfig = binConfigPath.resolve("table-config.json");
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(binTableConfig.toFile(), config);
                System.out.println("[ConfigGeneratorService] table-config.json saved to: " + binTableConfig);
            }

            System.out.println("[ConfigGeneratorService] table-config.json regenerated successfully");

        } catch (Exception e) {
            System.err.println("[ConfigGeneratorService] Failed to regenerate table-config.json: " + e.getMessage());
            throw new IOException("Failed to regenerate table-config.json", e);
        }
    }

    /**
     * Build table configuration from TableSchemaInfo and TableUISettings
     */
    private Map<String, Object> buildTableConfig(TableSchemaInfo schema, TableUISettings uiSettings) {
        Map<String, Object> config = new LinkedHashMap<>();

        config.put("name", schema.getTableName());
        config.put("label", uiSettings != null && uiSettings.getDisplayName() != null ? uiSettings.getDisplayName()
                : schema.getTableName());
        config.put("icon", "Table"); // Default icon

        // Build columns
        List<Map<String, Object>> columns = new ArrayList<>();
        List<String> listColumns = new ArrayList<>();

        for (TableSchemaInfo.ColumnSchemaInfo col : schema.getColumns()) {
            Map<String, Object> columnConfig = new LinkedHashMap<>();
            columnConfig.put("name", col.getColumnName());

            // Label (multilingual)
            Map<String, String> label = new LinkedHashMap<>();
            String displayLabel = col.getColumnComment() != null ? col.getColumnComment() : col.getColumnName();
            label.put("ja", displayLabel);
            label.put("en", displayLabel);
            columnConfig.put("label", label);

            // Type mapping: SQL type -> table-config type
            columnConfig.put("type", mapSqlTypeToConfigType(col.getDataType()));
            columnConfig.put("required", !col.getIsNullable());
            boolean isPrimaryKey = "PRI".equals(col.getColumnKey());
            boolean isAutoIncrement = col.getExtra() != null && col.getExtra().contains("auto_increment");
            columnConfig.put("visible", !isPrimaryKey && !isAutoIncrement);
            columnConfig.put("sortable", true);
            columnConfig.put("filterable", true);

            columns.add(columnConfig);

            // Add to listColumns if visible
            if (!isPrimaryKey && !isAutoIncrement) {
                listColumns.add(col.getColumnName());
            }
        }

        config.put("columns", columns);
        config.put("listColumns", listColumns);

        // UI settings from table_ui_settings
        if (uiSettings != null) {
            config.put("enableSearch", uiSettings.getEnableSearch());
            config.put("enableSort", uiSettings.getEnableSort());
            config.put("enablePagination", uiSettings.getEnablePagination());
            config.put("pageSize", uiSettings.getPageSize());
            config.put("allowCreate", uiSettings.getAllowCreate());
            config.put("allowEdit", uiSettings.getAllowEdit());
            config.put("allowDelete", uiSettings.getAllowDelete());
            config.put("allowBulk", uiSettings.getAllowBulk());
        } else {
            // Default UI settings
            config.put("enableSearch", true);
            config.put("enableSort", true);
            config.put("enablePagination", true);
            config.put("pageSize", 20);
            config.put("allowCreate", true);
            config.put("allowEdit", true);
            config.put("allowDelete", true);
            config.put("allowBulk", false);
        }

        return config;
    }

    /**
     * Map SQL data type to table-config type
     */
    private String mapSqlTypeToConfigType(String sqlType) {
        String type = sqlType.toUpperCase();

        if (type.contains("INT") || type.contains("NUMERIC") || type.contains("DECIMAL")) {
            return "number";
        } else if (type.contains("DATE") || type.contains("TIME")) {
            return "date";
        } else if (type.contains("BOOL")) {
            return "boolean";
        } else if (type.contains("TEXT") || type.contains("CLOB")) {
            return "textarea";
        } else {
            return "text";
        }
    }

    /**
     * Check if table is a system table (should be excluded from table-config.json)
     * System tables are defined in application.properties:
     * tablecraft.admin.system-tables
     */
    private boolean isSystemTable(String tableName) {
        if (systemTablesConfig == null || systemTablesConfig.trim().isEmpty()) {
            return false;
        }

        String[] systemTables = systemTablesConfig.split(",");
        for (String systemTable : systemTables) {
            if (tableName.equalsIgnoreCase(systemTable.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Load table templates from resources
     */
    public Map<String, Object> loadTableTemplates() throws IOException {
        try {
            org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource(
                    "table-templates.json");

            if (!resource.exists()) {
                Map<String, Object> emptyResponse = new HashMap<>();
                emptyResponse.put("templates", new HashMap<>());
                return emptyResponse;
            }

            return objectMapper.readValue(
                    resource.getInputStream(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        } catch (Exception e) {
            throw new IOException("Failed to load table templates: " + e.getMessage(), e);
        }
    }

    /**
     * Load default config file
     */
    public Map<String, Object> loadDefaultConfig(String filename) throws IOException {
        try {
            org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource(
                    "defaults/" + filename);

            if (!resource.exists()) {
                throw new IOException("Default config file not found: " + filename);
            }

            return objectMapper.readValue(
                    resource.getInputStream(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        } catch (Exception e) {
            throw new IOException("Failed to load default config: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize with default settings
     */
    public void initializeWithDefaults() throws IOException {
        // This is a placeholder for initialization logic
        // Implement based on your requirements
        System.out.println("[ConfigGeneratorService] Initialize with defaults called");
    }

    /**
     * Load config from file
     */
    public Map<String, Object> loadConfigFromFile(String filename) throws IOException {
        String configPath = configSavePath + "/" + filename;
        File file = new File(configPath);

        if (!file.exists()) {
            throw new IOException("Config file not found: " + configPath);
        }

        return objectMapper.readValue(file,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
    }

    /**
     * Save config to file
     */
    public void saveConfigToFile(Map<String, Object> config, String filename) throws IOException {
        String configPath = configSavePath + "/" + filename;
        File file = new File(configPath);

        // Create parent directories if they don't exist
        file.getParentFile().mkdirs();

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
        System.out.println("[ConfigGeneratorService] Config saved to: " + configPath);
    }

    /**
     * Generate table config (placeholder - implement based on requirements)
     */
    public Map<String, Object> generateTableConfig(List<Long> tableIds, Map<String, Object> options) {
        // Implement based on your requirements
        Map<String, Object> config = new HashMap<>();
        config.put("tables", new HashMap<>());
        return config;
    }

    /**
     * Generate UI config (placeholder - implement based on requirements)
     */
    public Map<String, Object> generateUiConfig(List<Long> tableIds, Map<String, Object> options) {
        // Implement based on your requirements
        Map<String, Object> config = new HashMap<>();
        return config;
    }

    /**
     * Generate validation config (placeholder - implement based on requirements)
     */
    public Map<String, Object> generateValidationConfig(List<Long> tableIds) {
        // Implement based on your requirements
        Map<String, Object> config = new HashMap<>();
        config.put("tables", new HashMap<>());
        return config;
    }
}
