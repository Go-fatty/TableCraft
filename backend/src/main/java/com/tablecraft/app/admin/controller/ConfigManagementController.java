package com.tablecraft.app.admin.controller;

import com.tablecraft.app.admin.service.ConfigGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 設定ファイル管理API（Phase 2）
 * POST統一エンドポイント
 */
@RestController
@RequestMapping("/api/admin/config")
@CrossOrigin(origins = "*")
public class ConfigManagementController {

    @Autowired
    private ConfigGeneratorService configGeneratorService;

    /**
     * table-config.jsonを生成（プレビュー）
     * POST /api/admin/config/generate-table-config
     */
    @PostMapping("/generate-table-config")
    public ResponseEntity<Map<String, Object>> generateTableConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<Long> tableIds = (List<Long>) request.get("tableIds");

            @SuppressWarnings("unchecked")
            Map<String, Object> customOptions = request.containsKey("options")
                    ? (Map<String, Object>) request.get("options")
                    : new HashMap<>();

            // 設定生成
            Map<String, Object> config = configGeneratorService.generateTableConfig(tableIds, customOptions);

            // ファイル保存するかどうか
            boolean saveToFile = request.containsKey("saveToFile") && (boolean) request.get("saveToFile");

            if (saveToFile) {
                String filename = request.getOrDefault("filename", "table-config.json").toString();
                configGeneratorService.saveConfigToFile(config, filename);
                response.put("message", "Config saved to " + filename);
            }

            response.put("success", true);
            response.put("data", config);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * UI設定（プロジェクト名）をtable-config.jsonのproject.nameに保存
     * POST /api/admin/config/generate-ui-config
     */
    @PostMapping("/generate-ui-config")
    public ResponseEntity<Map<String, Object>> generateUiConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newProjectName = request.getOrDefault("projectName", "").toString();
            // table-config.jsonをロード
            Map<String, Object> tableConfig = configGeneratorService.loadConfigFromFile("table-config.json");
            if (tableConfig.containsKey("project")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> project = (Map<String, Object>) tableConfig.get("project");
                project.put("name", newProjectName);
            }
            // 保存
            configGeneratorService.saveConfigToFile(tableConfig, "table-config.json");
            response.put("success", true);
            response.put("message", "project.name updated in table-config.json");
            response.put("data", tableConfig);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * validation-config.jsonを生成（プレビュー）
     * POST /api/admin/config/generate-validation-config
     */
    @PostMapping("/generate-validation-config")
    public ResponseEntity<Map<String, Object>> generateValidationConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<Long> tableIds = (List<Long>) request.get("tableIds");

            // 設定生成
            Map<String, Object> config = configGeneratorService.generateValidationConfig(tableIds);

            // ファイル保存するかどうか
            boolean saveToFile = request.containsKey("saveToFile") && (boolean) request.get("saveToFile");

            if (saveToFile) {
                String filename = request.getOrDefault("filename", "validation-config.json").toString();
                configGeneratorService.saveConfigToFile(config, filename);
                response.put("message", "Config saved to " + filename);
            }

            response.put("success", true);
            response.put("data", config);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 既存の設定ファイルを取得
     * POST /api/admin/config/get
     */
    @PostMapping("/get")
    public ResponseEntity<Map<String, Object>> getConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String filename = request.get("filename").toString();

            Map<String, Object> config = configGeneratorService.loadConfigFromFile(filename);

            response.put("success", true);
            response.put("data", config);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 設定ファイルを更新（上書き保存）
     * POST /api/admin/config/update
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("[ConfigManagementController] /api/admin/config/update 呼び出し");
            System.out.println("[ConfigManagementController] リクエスト内容: " + request);

            String filename = request.get("filename").toString();

            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) request.get("config");

            System.out.println("[ConfigManagementController] filename: " + filename);
            System.out.println("[ConfigManagementController] config keys: " + config.keySet());

            // 保存
            configGeneratorService.saveConfigToFile(config, filename);

            response.put("success", true);
            response.put("message", "Config updated successfully: " + filename);

            System.out.println("[ConfigManagementController] ✅ 保存成功レスポンスを返却");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("[ConfigManagementController] ❌ エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * すべての設定ファイルを一括生成
     * POST /api/admin/config/generate-all
     */
    @PostMapping("/generate-all")
    public ResponseEntity<Map<String, Object>> generateAllConfigs(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> results = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<Long> tableIds = (List<Long>) request.get("tableIds");

            @SuppressWarnings("unchecked")
            Map<String, Object> customOptions = request.containsKey("options")
                    ? (Map<String, Object>) request.get("options")
                    : new HashMap<>();

            boolean saveToFile = request.containsKey("saveToFile") && (boolean) request.get("saveToFile");

            // table-config.json
            Map<String, Object> tableConfig = configGeneratorService.generateTableConfig(tableIds, customOptions);
            if (saveToFile) {
                configGeneratorService.saveConfigToFile(tableConfig, "table-config.json");
            }
            results.put("tableConfig", tableConfig);

            // ui-config.json
            Map<String, Object> uiConfig = configGeneratorService.generateUiConfig(tableIds, customOptions);
            if (saveToFile) {
                configGeneratorService.saveConfigToFile(uiConfig, "ui-config.json");
            }
            results.put("uiConfig", uiConfig);

            // validation-config.json
            Map<String, Object> validationConfig = configGeneratorService.generateValidationConfig(tableIds);
            if (saveToFile) {
                configGeneratorService.saveConfigToFile(validationConfig, "validation-config.json");
            }
            results.put("validationConfig", validationConfig);

            response.put("success", true);
            response.put("data", results);
            if (saveToFile) {
                response.put("message", "All configs generated and saved successfully");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * テーブル構造をDBに適用
     * POST /api/admin/schema/apply
     * 注意: この機能は現在、/api/admin/tables/create エンドポイントを使用してください
     */
    @PostMapping("/schema/apply")
    public ResponseEntity<Map<String, Object>> applySchema(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        response.put("success", false);
        response.put("error", "This endpoint is deprecated. Please use /api/admin/tables/create instead.");
        response.put("message", "テーブル作成は /api/admin/tables/create エンドポイントを使用してください");

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }

    /**
     * デフォルト設定を読み込み
     * GET /api/admin/config/default/{filename}
     */
    @GetMapping("/default/{filename}")
    public ResponseEntity<Map<String, Object>> getDefaultConfig(@PathVariable String filename) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> config = configGeneratorService.loadDefaultConfig(filename);
            response.put("success", true);
            response.put("data", config);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * テーブルテンプレート一覧を取得
     * GET /api/admin/config/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getTableTemplates() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> templates = configGeneratorService.loadTableTemplates();
            response.put("success", true);
            response.put("data", templates);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * デフォルト設定で初期化
     * POST /api/admin/config/initialize
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeWithDefaults() {
        Map<String, Object> response = new HashMap<>();

        try {
            configGeneratorService.initializeWithDefaults();
            response.put("success", true);
            response.put("message", "Configuration initialized with defaults");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
