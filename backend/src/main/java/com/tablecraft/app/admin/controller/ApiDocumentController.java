package com.tablecraft.app.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * APIドキュメント管理コントローラー
 * OpenAPI/Swagger仕様の生成・保存機能を提供
 */
@RestController
@RequestMapping("/api/admin/docs")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
@Tag(name = "API Document Management", description = "APIドキュメント生成・管理API")
public class ApiDocumentController {

    @Autowired
    private OpenApiResource openApiResource;

    /**
     * OpenAPI JSON生成・保存エンドポイント
     * 
     * @return 生成結果（成功/失敗、ファイルパス、タイムスタンプ）
     */
    @PostMapping("/generate")
    @Operation(summary = "OpenAPI仕様生成", description = "現在のAPI仕様をOpenAPI JSON形式で生成し、backend/docs/openapi.jsonに保存します。")
    public ResponseEntity<Map<String, Object>> generateOpenApiDoc(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // ドキュメント保存ディレクトリ作成
            Path docsDir = Paths.get("docs");
            if (!Files.exists(docsDir)) {
                Files.createDirectories(docsDir);
            }

            // OpenAPI JSON保存パス
            Path outputPath = docsDir.resolve("openapi.json");

            // SpringDocのOpenApiResourceから直接OpenAPI仕様を取得
            byte[] openApiBytes = openApiResource.openapiJson(request, "en", null);
            Files.write(outputPath, openApiBytes);

            response.put("success", true);
            response.put("message", "OpenAPI仕様を正常に生成しました");
            response.put("filePath", outputPath.toAbsolutePath().toString());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "OpenAPI仕様の生成に失敗しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
