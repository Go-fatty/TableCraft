package com.tablecraft.app.dynamic;

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

@RestController
@RequestMapping("/api/sql")
@CrossOrigin(origins = "*")
public class SqlBasedController {

    @Autowired
    private SqlBasedTableService sqlBasedTableService;

    @PostMapping("/tables")
    public ResponseEntity<Map<String, Object>> getTables(@RequestBody(required = false) Map<String, Object> request) {
        try {
            Set<String> tables = sqlBasedTableService.getAvailableTables();
            Map<String, Object> response = new HashMap<>();
            response.put("tables", tables);
            response.put("count", tables.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/schema")
    public ResponseEntity<Map<String, Object>> getTableSchema(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> schema = sqlBasedTableService.getTableSchema(tableName);
            return ResponseEntity.ok(schema);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createRecord(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.get("data");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (data == null || data.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "data is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> result = sqlBasedTableService.create(tableName, data);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/find")
    public ResponseEntity<Map<String, Object>> findRecord(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            Object idObj = request.get("id");
            @SuppressWarnings("unchecked")
            Map<String, Object> keyValues = (Map<String, Object>) request.get("keyValues");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> result = null;

            // 複合主キーの場合
            if ("composite".equals(sqlBasedTableService.getPrimaryKeyType(tableName))) {
                if (keyValues == null || keyValues.isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "keyValues is required for composite key tables");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                result = sqlBasedTableService.findByCompositeKey(tableName, keyValues);
            } else {
                // 単一主キーの場合
                if (idObj == null) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "id is required for single key tables");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                Long id = Long.valueOf(idObj.toString());
                result = sqlBasedTableService.findById(tableName, id);
            }

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
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/findAll")
    public ResponseEntity<Map<String, Object>> findAllRecords(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            List<Map<String, Object>> results = sqlBasedTableService.findAll(tableName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            response.put("count", results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateRecord(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            Object idObj = request.get("id");
            @SuppressWarnings("unchecked")
            Map<String, Object> keyValues = (Map<String, Object>) request.get("keyValues");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.get("data");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (data == null || data.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "data is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> result = null;

            // 複合主キーの場合
            if ("composite".equals(sqlBasedTableService.getPrimaryKeyType(tableName))) {
                if (keyValues == null || keyValues.isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "keyValues is required for composite key tables");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                result = sqlBasedTableService.updateComposite(tableName, keyValues, data);
            } else {
                // 単一主キーの場合
                if (idObj == null) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "id is required for single key tables");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                Long id = Long.valueOf(idObj.toString());
                result = sqlBasedTableService.update(tableName, id, data);
            }

            Map<String, Object> response = new HashMap<>();
            if (result != null) {
                response.put("success", true);
                response.put("data", result);
            } else {
                response.put("success", false);
                response.put("message", "Record not found or no changes made");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteRecord(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            Object idObj = request.get("id");
            @SuppressWarnings("unchecked")
            Map<String, Object> keyValues = (Map<String, Object>) request.get("keyValues");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean deleted = false;

            // 複合主キーの場合
            if ("composite".equals(sqlBasedTableService.getPrimaryKeyType(tableName))) {
                if (keyValues == null || keyValues.isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "keyValues is required for composite key tables");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                deleted = sqlBasedTableService.deleteComposite(tableName, keyValues);
            } else {
                // 単一主キーの場合
                if (idObj == null) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "id is required for single key tables");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                Long id = Long.valueOf(idObj.toString());
                deleted = sqlBasedTableService.delete(tableName, id);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            if (deleted) {
                response.put("message", "Record deleted successfully");
            } else {
                response.put("message", "Record not found");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRecords(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            @SuppressWarnings("unchecked")
            Map<String, Object> searchCriteria = (Map<String, Object>) request.get("searchCriteria");
            Integer page = request.get("page") != null ? (Integer) request.get("page") : 0;
            Integer size = request.get("size") != null ? (Integer) request.get("size") : 10;
            String sortBy = (String) request.get("sortBy");
            String sortOrder = (String) request.get("sortOrder");

            if (tableName == null || tableName.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "tableName is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> result = sqlBasedTableService.search(tableName, searchCriteria, page, size, sortBy,
                    sortOrder);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/config/table-config")
    public ResponseEntity<String> getTableConfig(@RequestBody(required = false) Map<String, Object> request) {
        try {
            String configContent = readResourceFile("config/table-config.json");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(configContent);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load table configuration: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse.toString());
        }
    }

    @PostMapping("/config/validation-config")
    public ResponseEntity<String> getValidationConfig(@RequestBody(required = false) Map<String, Object> request) {
        try {
            String configContent = readResourceFile("config/validation-config.json");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(configContent);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load validation configuration: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse.toString());
        }
    }

    @PostMapping("/config/ui-config")
    public ResponseEntity<String> getUiConfig(@RequestBody(required = false) Map<String, Object> request) {
        try {
            String configContent = readResourceFile("config/ui-config.json");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(configContent);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load UI configuration: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse.toString());
        }
    }

    @PostMapping("/config/messages")
    public ResponseEntity<Map<String, String>> getMessages(@RequestBody(required = false) Map<String, Object> request) {
        try {
            String language = "ja"; // デフォルト
            if (request != null && request.containsKey("language")) {
                language = (String) request.get("language");
            }

            String fileName = "messages.properties";
            if ("en".equals(language)) {
                fileName = "messages_en.properties";
            } else if ("ja".equals(language)) {
                fileName = "messages_ja.properties";
            }

            // Propertiesファイルを読み込み
            Map<String, String> messages = loadPropertiesFile(fileName);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load messages: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

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
                    continue; // 空行やコメントをスキップ
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
}