# TableCraft Backend

TableCraftの動的CRUD API生成を担当するSpring Bootバックエンドです。

## 🏗️ 技術スタック

- **Java**: 11+
- **Spring Boot**: 2.7.5
- **H2 Database**: インメモリ
- **Maven**: 3.6+

## 📁 コア コンポーネント

```
src/main/java/com/tablecraft/app/
├── Application.java                    # Spring Boot エントリポイント
└── dynamic/
    ├── SqlBasedController.java         # REST API エンドポイント
    ├── SqlBasedTableService.java       # データベース操作サービス
    └── FieldDefinition.java            # フィールド定義クラス
```

## 🔧 API エンドポイント

### データ操作
- `POST /api/sql/tables` - テーブル一覧
- `POST /api/sql/schema` - テーブルスキーマ
- `POST /api/sql/findAll` - データ全件取得
- `POST /api/sql/create` - データ作成
- `POST /api/sql/update` - データ更新  
- `POST /api/sql/delete` - データ削除

### 設定取得
- `POST /api/sql/config/table-config` - テーブル設定
- `POST /api/sql/config/validation-config` - バリデーション設定
- `POST /api/sql/config/ui-config` - UI設定

## 🔧 開発・デバッグ

### ローカル起動
```bash
mvn spring-boot:run
```

### H2コンソール
- URL: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- ユーザー: `sa` / パスワード: `password`

### ログ設定
```properties
# application.properties
logging.level.com.tablecraft.app.dynamic=DEBUG
spring.jpa.show-sql=true
```

### 設定ファイル構成
- `table-config.json`: テーブル表示設定
- `validation-config.json`: バリデーションルール
- `ui-config.json`: UI動作設定
- `types.ts`: TypeScript型定義
- `useTable.ts`: Reactカスタムフック

## 🛠️ カスタマイズ

### 新テーブル追加
1. `settings_creates`でメタデータ定義
2. 生成された設定ファイルを`src/main/resources/`にコピー
3. アプリケーション再起動

### バリデーション追加
`validation-config.json`を編集してカスタムバリデーション実装。

### 多言語対応
`messages_*.properties`でメッセージを多言語化。

## 🐛 トラブルシューティング

**ポート競合**:
```bash
netstat -ano | findstr :8082
taskkill /F /PID [PID]
```

**設定ファイル更新**:
```bash
Copy-Item "..\settings_creates\output\frontend\*" "src\main\resources\" -Force
```

**ビルドクリーン**:
```bash
mvn clean compile
```

---
📖 システム全体の情報は[メインREADME](../README.md)を参照