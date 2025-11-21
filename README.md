# TableCraft - システム起動手順

## 概要
TableCraftは、JSONメタデータから動的にCRUD APIとUIを生成する設定ファイル駆動型システムです。テーブル定義だけでフルスタックアプリケーションを構築できます。

## システム構成
- **バックエンド**: Spring Boot 2.7.5 (Java)
- **フロントエンド**: React + TypeScript + Vite
- **データベース**: MySQL 8.0
- **設定ファイル**: `settings_creates/output/` の生成ファイルを使用

## 🚀 システム起動手順

### 🎯 簡単起動 (推奨)

**PowerShell版**:
```powershell
.\start-system.ps1
```

**バッチファイル版**:
```cmd
start-system.bat
```

上記スクリプトが自動実行する内容：
1. 設定ファイルの自動コピー
2. バックエンドのビルドと起動 (ポート8082)
3. フロントエンドの起動 (ポート5173)
4. 起動確認とステータス表示

### 📋 手動起動 (詳細制御が必要な場合)

### 前提条件
- Java 11以上
- Node.js 16以上  
- Maven 3.6以上
- **MySQL 8.0以上** (新規追加)

### 🗄️ MySQL データベースセットアップ

**初回セットアップ時に必須**

1. **データベース作成**
```sql
CREATE DATABASE tablecraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tablecraft;
```

2. **テーブル作成**  
```bash
mysql -u root -p -D tablecraft < backend/src/main/resources/mysql-schema.sql
```

3. **接続情報設定**  
`backend/src/main/resources/application-dev.properties` でMySQL認証情報を設定：
```properties
spring.datasource.username=root
spring.datasource.password=your-mysql-password
```

4. **動作確認**
```bash
mysql -u root -p -D tablecraft
SHOW TABLES;
# 7つのテーブル(users, categories, products, order_details, inventory_logs, sales_matrix, detailed_analytics)が表示されればOK
```

### 前提条件

### 1. 手動起動 - バックエンド (Spring Boot)

```powershell
# 1. バックエンドディレクトリに移動
cd backend

# 2. 設定ファイルをコピー (初回のみ必要)
Copy-Item "..\settings_creates\output\frontend\*" "src\main\resources" -Force

# 3. ビルド
mvn clean package -q

# 4. 起動 (ポート8082で起動)
Start-Process -WindowStyle Hidden -FilePath "java" -ArgumentList "-jar","target\tablecraft-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=dev","--server.port=8082"
```

**起動確認**:
```powershell
# テーブル一覧確認
Invoke-WebRequest -Uri "http://localhost:8082/api/sql/tables" -Method POST -ContentType "application/json" -Body "{}" | ConvertFrom-Json
```

### 2. 手動起動 - フロントエンド (React + Vite)

```powershell
# 1. フロントエンドディレクトリに移動
cd frontend

# 2. 依存関係インストール (初回のみ)
npm install

# 3. 開発サーバー起動
npm run dev
```

### 3. アクセス方法

- **フロントエンド**: http://localhost:5173
- **バックエンドAPI**: http://localhost:8082
- **MySQL接続**: localhost:3306/tablecraft
  - 認証情報: application-dev.properties で設定
  - 推奨クライアント: MySQL Workbench

**システム状態確認**:
```powershell
# MySQLデータベースの確認
mysql -u root -p -D tablecraft -e "SHOW TABLES;"
mysql -u root -p -D tablecraft -e "SELECT COUNT(*) FROM tasks;"

# バックエンドAPIの動作確認
Invoke-WebRequest -Uri "http://localhost:8082/api/sql/tables" -Method POST -ContentType "application/json" -Body "{}" | ConvertFrom-Json

# ポート使用状況確認
netstat -ano | findstr :8082  # バックエンド
netstat -ano | findstr :5173  # フロントエンド
```

## 📋 利用可能なテーブル

**PowerShell版**:
```powershell
.\stop-system.ps1
```

**手動停止**:
```powershell
# Javaプロセスの強制終了
taskkill /F /IM java.exe

# Node.jsプロセスの強制終了
taskkill /F /IM node.exe

# またはCtrl+Cでターミナルを停止
```

## 📊 システム監視

システム起動時に以下のテーブルが自動作成されます：

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| users | ユーザー管理 | id (BIGINT) |
| categories | カテゴリ管理 | id (BIGINT) |
| products | 商品管理 | id (BIGINT) |
| order_details | 注文詳細 | order_id, product_id (複合) |

## 🔧 API エンドポイント

### 基本CRUD操作
- `POST /api/sql/tables` - テーブル一覧取得
- `POST /api/sql/schema` - テーブルスキーマ取得
- `POST /api/sql/findAll` - データ全件取得
- `POST /api/sql/create` - データ新規作成
- `POST /api/sql/update` - データ更新
- `POST /api/sql/delete` - データ削除

### 設定ファイル取得
- `POST /api/sql/config/table-config` - テーブル設定
- `POST /api/sql/config/validation-config` - バリデーション設定
- `POST /api/sql/config/ui-config` - UI設定

## 🛠 トラブルシューティング

### ポートが使用中の場合
```powershell
# 使用中のプロセスを確認
netstat -ano | findstr :8082

# プロセス終了
taskkill /F /PID [PID番号]
```

### Javaプロセスの強制終了
```powershell
taskkill /F /IM java.exe
```

### 設定ファイルの再コピー
```powershell
cd backend
Copy-Item "..\settings_creates\output\frontend\*" "src\main\resources" -Force
```

## 📁 ディレクトリ構造

```
TableCraft/
├── README.md                          # このファイル
├── start-system.ps1                  # システム起動スクリプト (PowerShell)
├── start-system.bat                  # システム起動スクリプト (バッチ)
├── stop-system.ps1                   # システム停止スクリプト
├── backend/                           # バックエンド (Spring Boot)
│   ├── pom.xml                       # Maven設定ファイル
│   ├── src/main/java/com/tablecraft/app/
│   │   ├── Application.java           # メインクラス
│   │   └── dynamic/
│   │       ├── SqlBasedController.java    # REST API コントローラー
│   │       ├── SqlBasedTableService.java  # テーブル操作サービス
│   │       └── FieldDefinition.java       # フィールド定義クラス
│   ├── src/main/resources/
│   │   ├── application.properties     # 設定ファイル
│   │   ├── table-definitions.sql      # テーブル定義SQL
│   │   ├── table-config.json         # テーブル設定 (settings_createsから)
│   │   ├── validation-config.json    # バリデーション設定
│   │   ├── ui-config.json            # UI設定
│   │   ├── types.ts                  # TypeScript型定義
│   │   ├── useTable.ts               # React カスタムフック
│   │   └── messages_*.properties     # 多言語メッセージファイル
│   └── target/                       # ビルド成果物
├── frontend/                         # フロントエンド (React + Vite)
│   ├── package.json                  # Node.js依存関係
│   ├── vite.config.ts               # Vite設定
│   ├── tsconfig.json                # TypeScript設定
│   ├── src/
│   │   ├── App.tsx                   # メインアプリ
│   │   ├── main.tsx                  # エントリーポイント
│   │   ├── components/
│   │   │   ├── Layout/               # レイアウトコンポーネント
│   │   │   ├── Forms/                # フォームコンポーネント
│   │   │   └── Tables/               # テーブルコンポーネント
│   │   ├── hooks/
│   │   │   └── useTable.ts           # カスタムフック
│   │   └── types/
│   │       └── generated.ts          # 生成された型定義
│   └── public/                       # 静的ファイル
└── settings_creates/                 # 設定ファイル生成ツール
    ├── requirements.txt              # Python依存関係
    ├── src/                          # 生成スクリプト
    │   ├── generator.py              # メイン生成器
    │   ├── frontend_generator.py     # フロントエンド設定生成
    │   ├── properties_generator.py   # プロパティファイル生成
    │   └── sql_generator.py          # SQL生成
    ├── examples/
    │   └── table-metadata.json       # メタデータ例
    └── output/                       # 生成された設定ファイル
        ├── frontend/                 # フロントエンド用設定
        ├── properties/               # プロパティファイル
        └── sql/                      # SQL ファイル
```

## 📝 開発メモ

### MySQL関連のトラブルシューティング

**接続エラーが発生する場合:**
1. MySQLサーバーが起動しているか確認
```bash
mysql -u root -p
```

2. `tablecraft`データベースが存在するか確認
```sql
SHOW DATABASES;
USE tablecraft;
SHOW TABLES;
```

3. `application-dev.properties`の接続情報が正しいか確認
```properties
spring.datasource.username=root
spring.datasource.password=your-actual-password
```

**テーブルが見つからないエラー:**
```bash
# テーブル作成コマンドを再実行
mysql -u root -p -D tablecraft < backend/src/main/resources/mysql-schema.sql
```

### 設定ファイル更新時の手順
1. `settings_creates`で設定ファイルを再生成
2. 設定ファイルをSpring Bootのresourcesにコピー
3. Spring Bootを再起動

### 新しいテーブル追加時
1. `settings_creates/examples/table-metadata.json`でメタデータを定義
2. `settings_creates`で設定ファイルを再生成
```bash
cd settings_creates
python src/generator.py examples/table-metadata.json
```
3. 生成されたSQLファイルをMySQLで実行
```bash
mysql -u root -p -D tablecraft < output/sql/table_definitions.sql
```
4. 生成された設定ファイルをバックエンドにコピーして再起動

## 🎯 機能概要

- **動的テーブル管理**: 設定ファイルベースでテーブル操作
- **リアルタイムCRUD**: フロントエンドからの即座な操作
- **多言語対応**: 日本語/英語切り替え対応
- **バリデーション**: 設定ファイルベースの入力検証
- **レスポンシブUI**: モバイル対応済み

---

**🚀 クイックスタート**:
1. `start-system.ps1` または `start-system.bat` を実行
2. システムが自動起動 (バックエンド: 8082, フロントエンド: 5173)
3. ブラウザで http://localhost:5173 にアクセス