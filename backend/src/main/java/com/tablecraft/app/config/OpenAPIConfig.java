package com.tablecraft.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI/Swagger設定クラス
 * SpringDocを使用してAPIドキュメントを自動生成
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TableCraft API")
                        .version("1.0.0")
                        .description("TableCraft - ローコードツール API仕様書\n\n"
                                + "このAPIは、INFORMATION_SCHEMAベースのテーブル管理システムを提供します。\n"
                                + "管理画面からテーブルのCRUD操作、UI設定、自動設定ファイル生成が可能です。")
                        .contact(new Contact()
                                .name("TableCraft Support")
                                .email("support@tablecraft.example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("開発環境"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("本番環境（必要に応じて変更）")));
    }
}
