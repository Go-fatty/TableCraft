package com.tablecraft.app.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();

        try {
            // データベース接続確認
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            status.put("status", "UP");
            status.put("database", "Connected");
            status.put("timestamp", Instant.now().toString());
            status.put("environment", System.getProperty("spring.profiles.active", "default"));

            // 外部設定ファイルの存在確認
            boolean configExists = java.nio.file.Files.exists(
                    java.nio.file.Paths.get("./config/table-config.json"));
            status.put("externalConfig", configExists ? "Available" : "Missing");

        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("database", "Disconnected");
            status.put("error", e.getMessage());
            status.put("timestamp", Instant.now().toString());
        }

        return status;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "TableCraft");
        info.put("version", "0.0.1-SNAPSHOT");
        info.put("profile", System.getProperty("spring.profiles.active", "default"));
        info.put("java.version", System.getProperty("java.version"));
        info.put("java.vendor", System.getProperty("java.vendor"));

        // 外部設定ファイル情報
        Map<String, Boolean> configFiles = new HashMap<>();
        configFiles.put("table-config.json",
                java.nio.file.Files.exists(java.nio.file.Paths.get("./config/table-config.json")));
        configFiles.put("validation-config.json",
                java.nio.file.Files.exists(java.nio.file.Paths.get("./config/validation-config.json")));
        configFiles.put("ui-config.json",
                java.nio.file.Files.exists(java.nio.file.Paths.get("./config/ui-config.json")));

        info.put("externalConfigFiles", configFiles);

        return info;
    }
}