# TableCraft Backend

TableCraftの動的CRUD API生成と管理機能を担当するSpring Bootバックエンドです。

## 🏗️ 技術スタック

- **Java**: 11+
- **Spring Boot**: 2.7.5
- **MySQL**: 8.0+
- **Maven**: 3.6+

## 📁 コア コンポーネント

```
src/main/java/com/tablecraft/app/
├── Application.java                    # Spring Boot エントリポイント
├── admin/                              # 管理機能
│   ├── controller/
│   │   └── TableDefinitionController.java  # テーブル定義管理API
│   ├── service/
│   │   ├── TableDefinitionService.java     # テーブル定義管理サービス
│   │   └── ConfigGeneratorService.java     # 設定ファイル自動生成
│   ├── entity/
│   │   └── ManualTableDefinition.java      # テーブル定義エンティティ
│   └── repository/
│       └── ManualTableDefinitionRepository.java
└── dynamic/                            # 業務機能（動的API）
    ├── ConfigBasedController.java      # 動的CRUD API
    └── ConfigBasedTableService.java    # データベース操作サービス
```

## 🔧 API エンドポイント

### 管理API (/api/admin/*)
- `GET /api/admin/tables/list` - テーブル一覧取得
- `POST /api/admin/tables/create` - テーブル新規作成
- `PUT /api/admin/tables/update/{id}` - テーブル更新
- `POST /api/admin/tables/delete/{id}` - テーブル削除
- `POST /api/admin/tables/create-from-template` - テンプレートから作成

### 業務API (/api/config/*)
- `POST /api/config/table-config` - テーブル設定取得
- `GET /api/config/data/{tableName}` - データ一覧取得
- `POST /api/config/create` - データ新規作成
- `POST /api/config/update` - データ更新
- `POST /api/config/delete` - データ削除

## 🔧 開発・デバッグ

### ローカル起動
```bash
mvn spring-boot:run
```

### MySQL接続設定
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/tablecraft
spring.datasource.username=root
spring.datasource.password=root
```

### ログ設定
```properties
# application.properties
logging.level.com.tablecraft.app=DEBUG
spring.jpa.show-sql=true
```

## 🛠️ 主な機能

### テーブル定義管理
- 管理画面からのテーブル作成・編集・削除
- 動的DDL実行（CREATE/DROP/TRUNCATE）
- UI設定（検索、ソート、CRUD権限）の管理

### 設定ファイル自動生成
- `table-config.json` の自動生成
- カラム設定（表示/非表示、ソート可否）
- 多言語対応ラベル

### 動的CRUD API
- テーブル定義に基づく自動API生成
- JDBCベースの動的クエリ実行

## 🐛 トラブルシューティング

**ポート競合**:
```bash
netstat -ano | findstr :8082
taskkill /F /PID [PID]
```

**MySQL接続エラー**:
- `application.properties`の接続情報を確認
- MySQLサービスが起動しているか確認
- データベース`tablecraft`が存在するか確認

**ビルドクリーン**:
```bash
mvn clean compile
```

---
📖 システム全体の情報は[メインREADME](../README.md)を参照
