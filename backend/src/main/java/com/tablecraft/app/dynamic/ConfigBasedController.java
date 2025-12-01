package com.tablecraft.app.dynamic;

import com.tablecraft.app.model.TableDefinition;
import com.tablecraft.app.service.ConfigBasedTableService;
import com.tablecraft.app.service.ExternalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JSON設定ファイルベースのAPIコントローラー
 * 外部設定ファイルから読み込んだテーブル定義を使用してCRUD操作を提供
 */
@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ConfigBasedController {

    @Autowired
    private ConfigBasedTableService configBasedTableService;

    @Autowired
    private ExternalConfigService externalConfigService;

    /**
     * 利用可能なテーブル一覧を取得
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getTables() {
        try {
            Set<String> tables = configBasedTableService.getAllTableNames();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tables);
            response.put("count", tables.size());
            response.put("configInfo", configBasedTableService.getConfigInfo());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 指定テーブルの定義を取得
     */
    @PostMapping("/table-definition")
    public ResponseEntity<Map<String, Object>> getTableDefinition(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            var tableDefinition = configBasedTableService.getTableDefinition(tableName);
            if (tableDefinition == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Table not found: " + tableName);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tableName", tableName);
            response.put("definition", tableDefinition);
            response.put("columns", configBasedTableService.getTableColumns(tableName));
            response.put("primaryKeys", configBasedTableService.getPrimaryKeyColumns(tableName));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * レコード作成
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createRecord(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.get("data");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (data == null || data.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "data is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            int result = configBasedTableService.insert(tableName, data);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Record created successfully");
            response.put("affectedRows", result);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 主キーによるレコード検索
     */
    @PostMapping("/find")
    public ResponseEntity<Map<String, Object>> findRecord(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            @SuppressWarnings("unchecked")
            Map<String, Object> primaryKeyValues = (Map<String, Object>) request.get("primaryKeyValues");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (primaryKeyValues == null || primaryKeyValues.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "primaryKeyValues is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> result = configBasedTableService.findById(tableName, primaryKeyValues);
            Map<String, Object> response = new HashMap<>();

            if (result != null) {
                response.put("success", true);
                response.put("data", result);
            } else {
                response.put("success", false);
                response.put("message", "Record not found");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 全レコード取得（ページネーション付き）
     */
    @PostMapping("/findAll")
    public ResponseEntity<Map<String, Object>> findAllRecords(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            Integer offset = request.get("offset") != null ? ((Number) request.get("offset")).intValue() : 0;
            Integer limit = request.get("limit") != null ? ((Number) request.get("limit")).intValue() : 100;
            String orderBy = (String) request.get("orderBy");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            List<Map<String, Object>> results = configBasedTableService.findAll(tableName, offset, limit, orderBy);
            long totalCount = configBasedTableService.count(tableName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            response.put("count", results.size());
            response.put("totalCount", totalCount);
            response.put("offset", offset);
            response.put("limit", limit);
            response.put("hasMore", (offset + limit) < totalCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * レコード更新
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateRecord(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            @SuppressWarnings("unchecked")
            Map<String, Object> primaryKeyValues = (Map<String, Object>) request.get("primaryKeyValues");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.get("data");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (primaryKeyValues == null || primaryKeyValues.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "primaryKeyValues is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (data == null || data.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "data is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            int affectedRows = configBasedTableService.update(tableName, data, primaryKeyValues);
            Map<String, Object> response = new HashMap<>();

            if (affectedRows > 0) {
                // 更新後のレコードを取得
                Map<String, Object> updatedRecord = configBasedTableService.findById(tableName, primaryKeyValues);
                response.put("success", true);
                response.put("message", "Record updated successfully");
                response.put("affectedRows", affectedRows);
                response.put("data", updatedRecord);
            } else {
                response.put("success", false);
                response.put("message", "No records were updated");
                response.put("affectedRows", 0);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * レコード削除
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteRecord(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            @SuppressWarnings("unchecked")
            Map<String, Object> primaryKeyValues = (Map<String, Object>) request.get("primaryKeyValues");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (primaryKeyValues == null || primaryKeyValues.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "primaryKeyValues is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            int affectedRows = configBasedTableService.delete(tableName, primaryKeyValues);
            Map<String, Object> response = new HashMap<>();

            if (affectedRows > 0) {
                response.put("success", true);
                response.put("message", "Record deleted successfully");
                response.put("affectedRows", affectedRows);
            } else {
                response.put("success", false);
                response.put("message", "No records were deleted");
                response.put("affectedRows", 0);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 設定ファイルの再読み込み
     */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reloadConfig(
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            configBasedTableService.reloadConfig();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Configuration reloaded successfully");
            response.put("configInfo", configBasedTableService.getConfigInfo());
            response.put("tables", configBasedTableService.getAllTableNames());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * テーブル設定ファイルの内容を取得（listColumnsを展開）
     */
    @PostMapping("/table-config")
    public ResponseEntity<String> getTableConfig(@RequestBody(required = false) Map<String, Object> request) {
        try {
            // JSONファイルを読み込んでlistColumnsを展開
            String configContent = readResourceFile("config/table-config.json");
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> config = mapper.readValue(configContent, Map.class);

            // 各テーブルのlistColumnsを文字列配列からオブジェクト配列に展開
            Map<String, Object> tables = (Map<String, Object>) config.get("tables");
            if (tables != null) {
                for (Object tableObj : tables.values()) {
                    Map<String, Object> table = (Map<String, Object>) tableObj;
                    List<Object> listColumns = (List<Object>) table.get("listColumns");
                    List<Map<String, Object>> columns = (List<Map<String, Object>>) table.get("columns");

                    if (listColumns != null && columns != null) {
                        List<Map<String, Object>> expandedColumns = new ArrayList<>();
                        for (Object columnNameObj : listColumns) {
                            String columnName = (String) columnNameObj;
                            // columnsから該当するカラム定義を検索
                            columns.stream()
                                    .filter(col -> columnName.equals(col.get("name")))
                                    .findFirst()
                                    .ifPresent(col -> {
                                        // labelsをlabelにコピー（フロントエンド互換性）
                                        if (col.containsKey("labels") && !col.containsKey("label")) {
                                            col.put("label", col.get("labels"));
                                        }
                                        expandedColumns.add(col);
                                    });
                        }
                        table.put("listColumns", expandedColumns);
                    }
                }
            }

            String expandedJson = mapper.writeValueAsString(config);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(expandedJson);
        } catch (Exception e) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to load table configuration: " + e.getMessage());
            try {
                return ResponseEntity.status(500)
                        .header("Content-Type", "application/json")
                        .body(mapper.writeValueAsString(errorResponse));
            } catch (Exception jsonException) {
                return ResponseEntity.status(500).body("{\"success\":false,\"error\":\"Internal server error\"}");
            }
        }
    }

    /**
     * バリデーション設定ファイルを取得
     */
    @PostMapping("/validation-config")
    public ResponseEntity<String> getValidationConfig(@RequestBody(required = false) Map<String, Object> request) {
        try {
            String configContent = readResourceFile("config/validation-config.json");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(configContent);
        } catch (Exception e) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load validation configuration: " + e.getMessage());
            try {
                return ResponseEntity.status(500)
                        .header("Content-Type", "application/json")
                        .body(mapper.writeValueAsString(errorResponse));
            } catch (Exception jsonException) {
                return ResponseEntity.status(500).body("{\"error\":\"Internal server error\"}");
            }
        }
    }

    /**
     * UI設定ファイルを取得
     */
    @PostMapping("/ui-config")
    public ResponseEntity<String> getUiConfig(@RequestBody(required = false) Map<String, Object> request) {
        try {
            String configContent = readResourceFile("config/ui-config.json");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(configContent);
        } catch (Exception e) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load UI configuration: " + e.getMessage());
            try {
                return ResponseEntity.status(500)
                        .header("Content-Type", "application/json")
                        .body(mapper.writeValueAsString(errorResponse));
            } catch (Exception jsonException) {
                return ResponseEntity.status(500).body("{\"error\":\"Internal server error\"}");
            }
        }
    }

    /**
     * メッセージファイルを取得
     */
    @PostMapping("/messages")
    public ResponseEntity<Map<String, String>> getMessages(@RequestBody(required = false) Map<String, Object> request) {
        try {
            String language = "ja"; // デフォルト
            if (request != null && request.containsKey("language")) {
                language = (String) request.get("language");
            }

            String fileName = "i18n/messages.properties";
            if ("en".equals(language)) {
                fileName = "i18n/messages_en.properties";
            } else if ("ja".equals(language)) {
                fileName = "i18n/messages_ja.properties";
            }

            Map<String, String> messages = loadPropertiesFile(fileName);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load messages: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * システム情報（ヘルスチェック）
     */
    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(@RequestBody(required = false) Map<String, Object> request) {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("success", true);
            status.put("service", "TableCraft Config-Based API");
            status.put("configValid", externalConfigService.isConfigValid());
            status.put("configInfo", configBasedTableService.getConfigInfo());
            status.put("availableTables", configBasedTableService.getAllTableNames());
            status.put("tableCount", configBasedTableService.getAllTableNames().size());
            status.put("timestamp", new Date());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ユーティリティメソッド（既存コントローラーから移行）
    private Map<String, String> loadPropertiesFile(String fileName) throws Exception {
        Resource resource = new ClassPathResource(fileName);
        Map<String, String> messages = new HashMap<>();

        try (InputStream inputStream = resource.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalIndex = line.indexOf("=");
                if (equalIndex > 0) {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();
                    messages.put(key, value);
                }
            }
        }

        return messages;
    }

    private String readResourceFile(String fileName) throws Exception {
        // 1. 外部設定ファイルを優先的に読み込み（本番環境）
        java.nio.file.Path externalFilePath = java.nio.file.Paths.get("./config/" + fileName);
        if (java.nio.file.Files.exists(externalFilePath)) {
            System.out.println("Loading external config file: " + externalFilePath.toAbsolutePath());
            return new String(java.nio.file.Files.readAllBytes(externalFilePath), StandardCharsets.UTF_8);
        }

        // 2. JARファイル内の設定ファイルをフォールバック（開発環境）
        System.out.println("Loading internal config file: " + fileName);
        Resource resource = new ClassPathResource(fileName);
        try (InputStream inputStream = resource.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * UI設定を取得
     */
    @GetMapping("/ui")
    public ResponseEntity<String> getUiConfig() {
        try {
            String configContent = readResourceFile("config/ui-config.json");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(configContent);
        } catch (Exception e) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load UI configuration: " + e.getMessage());
            try {
                return ResponseEntity.status(500)
                        .header("Content-Type", "application/json")
                        .body(mapper.writeValueAsString(errorResponse));
            } catch (Exception jsonException) {
                return ResponseEntity.status(500).body("{\"error\":\"Internal server error\"}");
            }
        }
    }

    /**
     * バリデーション設定を取得
     */
    @GetMapping("/validation")
    public ResponseEntity<String> getValidationConfig() {
        try {
            String configContent = readResourceFile("config/validation-config.json");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(configContent);
        } catch (Exception e) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load validation configuration: " + e.getMessage());
            try {
                return ResponseEntity.status(500)
                        .header("Content-Type", "application/json")
                        .body(mapper.writeValueAsString(errorResponse));
            } catch (Exception jsonException) {
                return ResponseEntity.status(500).body("{\"error\":\"Internal server error\"}");
            }
        }
    }

    /**
     * スキーマ情報を取得
     */
    @GetMapping("/schema/{tableName}")
    public ResponseEntity<Map<String, Object>> getTableSchema(@PathVariable String tableName) {
        try {
            TableDefinition tableDefinition = configBasedTableService.getTableDefinition(tableName);
            if (tableDefinition == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "テーブルが見つかりません: " + tableName);
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> schema = new HashMap<>();
            schema.put("tableName", tableName);
            schema.put("columns", tableDefinition.getColumns());
            schema.put("primaryKey", tableDefinition.getPrimaryKey());
            schema.put("foreignKeys",
                    tableDefinition.getForeignKeys() != null ? tableDefinition.getForeignKeys() : new ArrayList<>());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", schema);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * データ一覧取得
     */
    /**
     * データ一覧取得
     */
    @GetMapping("/data/{tableName}")
    public ResponseEntity<Map<String, Object>> getData(@PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "") String orderBy) {
        try {
            int offset = page * size;
            List<Map<String, Object>> results = configBasedTableService.findAll(tableName, offset, size, orderBy);
            long totalElements = configBasedTableService.count(tableName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            Map<String, Object> pageData = new HashMap<>();
            pageData.put("content", results);
            pageData.put("totalElements", totalElements);
            pageData.put("totalPages", (int) Math.ceil((double) totalElements / size));
            pageData.put("size", size);
            pageData.put("number", page);

            response.put("data", pageData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 単一データ取得（autofill用）
     */
    @GetMapping("/data/{tableName}/{id}")
    public ResponseEntity<Map<String, Object>> getDataById(@PathVariable String tableName,
            @PathVariable Long id) {
        try {
            Map<String, Object> idMap = new HashMap<>();
            idMap.put("id", id);

            List<Map<String, Object>> results = configBasedTableService.findByPrimaryKey(tableName, idMap);

            Map<String, Object> response = new HashMap<>();
            if (results != null && !results.isEmpty()) {
                response.put("success", true);
                response.put("data", results.get(0));
            } else {
                response.put("success", false);
                response.put("error", "Record not found");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * データ作成
     */
    @PostMapping("/data/{tableName}")
    public ResponseEntity<Map<String, Object>> createData(@PathVariable String tableName,
            @RequestBody Map<String, Object> data) {
        try {
            int affectedRows = configBasedTableService.insert(tableName, data);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("affectedRows", affectedRows));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * データ更新
     */
    @PutMapping("/data/{tableName}/{id}")
    public ResponseEntity<Map<String, Object>> updateData(@PathVariable String tableName,
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> primaryKeyValues = Map.of("id", id);
            int affectedRows = configBasedTableService.update(tableName, data, primaryKeyValues);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("affectedRows", affectedRows));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * データ削除
     */
    @DeleteMapping("/data/{tableName}/{id}")
    public ResponseEntity<Map<String, Object>> deleteData(@PathVariable String tableName,
            @PathVariable Long id) {
        try {
            Map<String, Object> idMap = new HashMap<>();
            idMap.put("id", id);
            int affectedRows = configBasedTableService.delete(tableName, idMap);
            boolean success = affectedRows > 0;

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Record deleted successfully" : "Record not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}