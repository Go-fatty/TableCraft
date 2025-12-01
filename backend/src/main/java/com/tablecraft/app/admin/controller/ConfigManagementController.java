package com.tablecraft.app.admin.controller;

import com.tablecraft.app.admin.service.ConfigGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration file management API
 * Provides endpoints for regenerating configuration files
 */
@RestController
@RequestMapping("/api/admin/config")
@CrossOrigin(origins = "*")
public class ConfigManagementController {

    @Autowired
    private ConfigGeneratorService configGeneratorService;

    /**
     * Regenerate table-config.json from INFORMATION_SCHEMA
     * POST /api/admin/config/regenerate
     */
    @PostMapping("/regenerate")
    public ResponseEntity<Map<String, Object>> regenerateTableConfig() {
        Map<String, Object> response = new HashMap<>();

        try {
            configGeneratorService.regenerateTableConfig();

            response.put("success", true);
            response.put("message", "table-config.json regenerated successfully from INFORMATION_SCHEMA");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}
