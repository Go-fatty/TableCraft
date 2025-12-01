package com.tablecraft.app.admin.controller;

import com.tablecraft.app.admin.entity.ParsedTableDefinition;
import com.tablecraft.app.admin.entity.UploadedSqlFile;
import com.tablecraft.app.admin.service.SqlFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SQL管理API（Phase 1）
 * POST統一エンドポイント
 */
@RestController
@RequestMapping("/api/admin/sql")
@CrossOrigin(origins = "*")
public class SqlManagementController {

    @Autowired
    private SqlFileService sqlFileService;

    /**
     * SQLファイルアップロード
     * POST /api/admin/sql/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadSqlFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sqlType") String sqlType,
            @RequestParam("dbmsType") String dbmsType,
            @RequestParam(value = "uploadedBy", defaultValue = "system") String uploadedBy) {
        Map<String, Object> response = new HashMap<>();

        try {
            // バリデーション
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".sql")) {
                response.put("success", false);
                response.put("error", "Only .sql files are allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // アップロード処理
            UploadedSqlFile sqlFile = sqlFileService.uploadSqlFile(file, sqlType, dbmsType, uploadedBy);

            response.put("success", true);
            response.put("data", Map.of(
                    "sqlFileId", sqlFile.getId(),
                    "fileName", sqlFile.getFileName(),
                    "fileSize", sqlFile.getFileSize(),
                    "parseStatus", sqlFile.getParseStatus(),
                    "uploadedAt", sqlFile.getUploadedAt().toString()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * SQL解析実行
     * POST /api/admin/sql/parse
     */
    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parseSqlFile(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long sqlFileId = Long.valueOf(request.get("sqlFileId").toString());

            // 解析処理
            UploadedSqlFile sqlFile = sqlFileService.parseSqlFile(sqlFileId);

            // テーブル定義を取得
            List<ParsedTableDefinition> definitions = sqlFileService.getTableDefinitions(sqlFileId);

            Map<String, Object> data = new HashMap<>();
            data.put("sqlFileId", sqlFile.getId());
            data.put("parseStatus", sqlFile.getParseStatus());
            data.put("tableCount", definitions.size());
            data.put("tables",
                    definitions.stream().map(ParsedTableDefinition::getTableName).collect(Collectors.toList()));
            data.put("errorMessage", sqlFile.getErrorMessage() != null ? sqlFile.getErrorMessage() : "");

            response.put("success", true);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * アップロード済みSQLファイル一覧
     * POST /api/admin/sql/list
     */
    @PostMapping("/list")
    public ResponseEntity<Map<String, Object>> listSqlFiles(
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<UploadedSqlFile> files = sqlFileService.listAllSqlFiles();

            List<Map<String, Object>> fileList = files.stream().map(file -> {
                Map<String, Object> fileMap = new HashMap<>();
                fileMap.put("sqlFileId", file.getId());
                fileMap.put("fileName", file.getFileName());
                fileMap.put("fileSize", file.getFileSize());
                fileMap.put("sqlType", file.getSqlType());
                fileMap.put("dbmsType", file.getDbmsType());
                fileMap.put("parseStatus", file.getParseStatus());
                fileMap.put("uploadedBy", file.getUploadedBy());
                fileMap.put("uploadedAt", file.getUploadedAt().toString());
                return fileMap;
            }).collect(Collectors.toList());

            response.put("success", true);
            response.put("data", fileList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 特定SQLファイルの詳細取得
     * POST /api/admin/sql/get
     */
    @PostMapping("/get")
    public ResponseEntity<Map<String, Object>> getSqlFile(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long sqlFileId = Long.valueOf(request.get("sqlFileId").toString());

            Optional<UploadedSqlFile> optionalFile = sqlFileService.getSqlFile(sqlFileId);
            if (optionalFile.isEmpty()) {
                response.put("success", false);
                response.put("error", "SQL file not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            UploadedSqlFile file = optionalFile.get();

            Map<String, Object> data = new HashMap<>();
            data.put("sqlFileId", file.getId());
            data.put("fileName", file.getFileName());
            data.put("fileSize", file.getFileSize());
            data.put("sqlContent", file.getSqlContent());
            data.put("sqlType", file.getSqlType());
            data.put("dbmsType", file.getDbmsType());
            data.put("parseStatus", file.getParseStatus());
            data.put("parseResult", file.getParseResult() != null ? file.getParseResult() : "");
            data.put("errorMessage", file.getErrorMessage() != null ? file.getErrorMessage() : "");
            data.put("uploadedBy", file.getUploadedBy());
            data.put("uploadedAt", file.getUploadedAt().toString());

            response.put("success", true);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * SQLファイル削除
     * POST /api/admin/sql/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteSqlFile(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long sqlFileId = Long.valueOf(request.get("sqlFileId").toString());

            sqlFileService.deleteSqlFile(sqlFileId);

            response.put("success", true);
            response.put("message", "SQL file deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * テーブル定義一覧取得
     * POST /api/admin/sql/tables
     */
    @PostMapping("/tables")
    public ResponseEntity<Map<String, Object>> getTableDefinitions(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long sqlFileId = Long.valueOf(request.get("sqlFileId").toString());

            List<ParsedTableDefinition> definitions = sqlFileService.getTableDefinitions(sqlFileId);

            List<Map<String, Object>> tableList = definitions.stream().map(table -> {
                Map<String, Object> tableMap = new HashMap<>();
                tableMap.put("tableId", table.getId());
                tableMap.put("tableName", table.getTableName());
                tableMap.put("schemaName", table.getSchemaName() != null ? table.getSchemaName() : "");
                tableMap.put("tableStructure", table.getTableStructure());
                tableMap.put("foreignKeys", table.getForeignKeys());
                tableMap.put("indexes", table.getIndexes());
                tableMap.put("createdAt", table.getCreatedAt().toString());
                return tableMap;
            }).collect(Collectors.toList());

            response.put("success", true);
            response.put("data", tableList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 全てのSQLファイルとテーブル定義をクリア
     * POST /api/admin/sql/clear-all
     */
    @PostMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAll() {
        Map<String, Object> response = new HashMap<>();

        try {
            sqlFileService.clearAll();

            response.put("success", true);
            response.put("message", "All SQL files and table definitions have been cleared");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 特定テーブル定義の詳細取得
     * POST /api/admin/sql/table-detail
     */
    @PostMapping("/table-detail")
    public ResponseEntity<Map<String, Object>> getTableDetail(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long sqlFileId = Long.valueOf(request.get("sqlFileId").toString());
            String tableName = request.get("tableName").toString();

            Optional<ParsedTableDefinition> optionalTable = sqlFileService.getTableDefinition(sqlFileId, tableName);
            if (optionalTable.isEmpty()) {
                response.put("success", false);
                response.put("error", "Table definition not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            ParsedTableDefinition table = optionalTable.get();

            response.put("success", true);
            response.put("data", Map.of(
                    "tableId", table.getId(),
                    "sqlFileId", table.getSqlFileId(),
                    "tableName", table.getTableName(),
                    "schemaName", table.getSchemaName() != null ? table.getSchemaName() : "",
                    "tableStructure", table.getTableStructure(),
                    "foreignKeys", table.getForeignKeys(),
                    "indexes", table.getIndexes(),
                    "createdAt", table.getCreatedAt().toString()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
