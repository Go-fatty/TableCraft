package com.tablecraft.app.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tablecraft.app.admin.entity.ManualTableDefinition;
import com.tablecraft.app.admin.service.TableDefinitionService;
import com.tablecraft.app.admin.service.TableDefinitionService.TableDefinitionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * テーブル定義管理API
 * 手動作成・編集・削除
 */
@RestController
@RequestMapping("/api/admin/tables")
@CrossOrigin(origins = "*")
public class TableDefinitionController {

    @Autowired
    private TableDefinitionService tableDefinitionService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * テーブル定義を新規作成
     * POST /api/admin/tables/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTable(@RequestBody TableDefinitionRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("[TableDefinitionController] テーブル作成リクエスト: " + request.getTableName());

            ManualTableDefinition created = tableDefinitionService.createManualTable(request);

            response.put("success", true);
            response.put("data", convertToResponse(created));
            response.put("message", "Table created successfully: " + created.getTableName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("[TableDefinitionController] ❌ エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * テーブル定義一覧を取得
     * POST /api/admin/tables/list
     */
    @PostMapping("/list")
    public ResponseEntity<Map<String, Object>> listTables() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ManualTableDefinition> tables = tableDefinitionService.listAllTables();

            List<Map<String, Object>> tableList = new ArrayList<>();
            for (ManualTableDefinition table : tables) {
                tableList.add(convertToResponse(table));
            }

            response.put("success", true);
            response.put("data", tableList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("[TableDefinitionController] ❌ エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 特定のテーブル定義を取得
     * POST /api/admin/tables/get
     */
    @PostMapping("/get")
    public ResponseEntity<Map<String, Object>> getTable(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long tableId = Long.valueOf(request.get("tableId").toString());

            Optional<ManualTableDefinition> table = tableDefinitionService.getTable(tableId);

            if (table.isEmpty()) {
                response.put("success", false);
                response.put("error", "Table not found: " + tableId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("data", convertToResponse(table.get()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("[TableDefinitionController] ❌ エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * テーブル定義を更新
     * POST /api/admin/tables/update/{tableId}
     */
    @PostMapping("/update/{tableId}")
    public ResponseEntity<Map<String, Object>> updateTable(
            @PathVariable Long tableId,
            @RequestBody TableDefinitionRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("[TableDefinitionController] テーブル更新リクエスト: ID=" + tableId);

            ManualTableDefinition updated = tableDefinitionService.updateTable(tableId, request);

            response.put("success", true);
            response.put("data", convertToResponse(updated));
            response.put("message", "Table updated successfully: " + updated.getTableName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            System.err.println("[TableDefinitionController] ❌ エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * テーブル定義を更新（PUT版）
     * PUT /api/admin/tables/update/{tableId}
     */
    @PutMapping("/update/{tableId}")
    public ResponseEntity<Map<String, Object>> updateTablePut(
            @PathVariable Long tableId,
            @RequestBody TableDefinitionRequest request) {
        return updateTable(tableId, request);
    }

    /**
     * テーブル定義を削除
     * POST /api/admin/tables/delete/{tableId}
     */
    @PostMapping("/delete/{tableId}")
    public ResponseEntity<Map<String, Object>> deleteTable(@PathVariable Long tableId) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("[TableDefinitionController] テーブル削除リクエスト: ID=" + tableId);

            tableDefinitionService.deleteTable(tableId);

            response.put("success", true);
            response.put("message", "Table deleted successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            System.err.println("[TableDefinitionController] ❌ エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * エンティティをレスポンス形式に変換
     */
    private Map<String, Object> convertToResponse(ManualTableDefinition table) throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", table.getId());
        data.put("tableName", table.getTableName());
        data.put("displayName", table.getDisplayName());
        
        // JSON文字列をオブジェクトに変換して返す
        if (table.getColumns() != null) {
            data.put("columns", objectMapper.readValue(table.getColumns(), List.class));
        }
        
        data.put("createdAt", table.getCreatedAt());
        data.put("updatedAt", table.getUpdatedAt());
        return data;
    }
}