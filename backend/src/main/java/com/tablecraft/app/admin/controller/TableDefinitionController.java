package com.tablecraft.app.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tablecraft.app.admin.dto.TableDefinitionResponse;
import com.tablecraft.app.admin.service.IntegratedTableService;
import com.tablecraft.app.admin.service.IntegratedTableService.TableCreationRequest;
import com.tablecraft.app.admin.service.IntegratedTableService.TableUISettingsUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Table definition management API
 * Create, edit, delete tables with UI settings
 */
@RestController
@RequestMapping("/api/admin/tables")
@CrossOrigin(origins = "*")
public class TableDefinitionController {

    @Autowired
    private IntegratedTableService integratedTableService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Create new table definition
     * POST /api/admin/tables/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTable(@RequestBody TableCreationRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("[TableDefinitionController] Table creation request: " + request.getTableName());

            TableDefinitionResponse created = integratedTableService.createTable(request);

            response.put("success", true);
            response.put("data", created);
            response.put("message", "Table created successfully: " + created.getTableName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("[TableDefinitionController] Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get list of table definitions
     * POST /api/admin/tables/list
     */
    @PostMapping("/list")
    public ResponseEntity<Map<String, Object>> listTables() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<TableDefinitionResponse> tables = integratedTableService.getAllTables();

            response.put("success", true);
            response.put("data", tables);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("[TableDefinitionController] Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get specific table definition
     * POST /api/admin/tables/get
     */
    @PostMapping("/get")
    public ResponseEntity<Map<String, Object>> getTable(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long tableId = Long.valueOf(request.get("tableId").toString());

            Optional<TableDefinitionResponse> table = integratedTableService.getTableById(tableId);

            if (table.isEmpty()) {
                response.put("success", false);
                response.put("error", "Table not found: " + tableId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("data", table.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("[TableDefinitionController] Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update table UI settings
     * POST /api/admin/tables/update/{tableId}
     */
    @PostMapping("/update/{tableId}")
    public ResponseEntity<Map<String, Object>> updateTable(
            @PathVariable Long tableId,
            @RequestBody TableUISettingsUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("[TableDefinitionController] Table update request: ID=" + tableId);

            TableDefinitionResponse updated = integratedTableService.updateTableUISettings(tableId, request);

            response.put("success", true);
            response.put("data", updated);
            response.put("message", "Table updated successfully: " + updated.getTableName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            System.err.println("[TableDefinitionController] Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update table UI settings (PUT version)
     * PUT /api/admin/tables/update/{tableId}
     */
    @PutMapping("/update/{tableId}")
    public ResponseEntity<Map<String, Object>> updateTablePut(
            @PathVariable Long tableId,
            @RequestBody TableUISettingsUpdateRequest request) {
        return updateTable(tableId, request);
    }

    /**
     * Delete table definition
     * POST /api/admin/tables/delete/{tableId}
     */
    @PostMapping("/delete/{tableId}")
    public ResponseEntity<Map<String, Object>> deleteTable(@PathVariable Long tableId) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("[TableDefinitionController] Table deletion request: ID=" + tableId);

            integratedTableService.deleteTable(tableId);

            response.put("success", true);
            response.put("message", "Table deleted successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            System.err.println("[TableDefinitionController] Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}