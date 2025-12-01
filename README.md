# TableCraft - 動的テーブル管理システム

## 概要
TableCraftは、**管理画面からテーブル定義を作成・編集**し、動的にCRUD APIとUIを生成するローコード開発プラットフォームです。コードを書かずにデータベーステーブルとCRUD画面を作成できます。

## システム構成
- **バックエンド**: Spring Boot 2.7.5 (Java) + JPA/JDBC
- **フロントエンド（業務画面）**: React + TypeScript + Vite
- **フロントエンド（管理画面）**: React + TypeScript + Vite
- **データベース**: MySQL 8.0
- **設定管理**: 管理画面 → DB保存 → `table-config.json`自動生成
- **API エンドポイント**: 
  - `/api/admin/*` (管理機能)
  - `/api/config/*` (業務機能)

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
1. バックエンドのビルドと起動 (ポート8082)
2. 業務画面フロントエンドの起動 (ポート5173)
3. 管理画面フロントエンドの起動 (ポート5174)
4. 起動確認とステータス表示

### 📋 手動起動 (詳細制御が必要な場合)

#### 前提条件
- Java 11以上
- Node.js 16以上  
- Maven 3.6以上
- MySQL 8.0以上

#### 🗄️ MySQL データベースセットアップ

**初回セットアップ時に必須**

1. **データベース作成**
```sql
CREATE DATABASE tablecraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tablecraft;
```

2. **テーブル作成**  
```sql
-- manual_table_definitions テーブル
CREATE TABLE manual_table_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    columns TEXT NOT NULL,
    enable_search BOOLEAN DEFAULT TRUE,
    enable_sort BOOLEAN DEFAULT TRUE,
    enable_pagination BOOLEAN DEFAULT TRUE,
    page_size INT DEFAULT 20,
    allow_create BOOLEAN DEFAULT TRUE,
    allow_edit BOOLEAN DEFAULT TRUE,
    allow_delete BOOLEAN DEFAULT TRUE,
    allow_bulk BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

3. **接続情報設定**  
`backend/src/main/resources/application.properties` でMySQL認証情報を設定
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tablecraft
spring.datasource.username=root
spring.datasource.password=root
```

#### 1. 手動起動 - バックエンド (Spring Boot)

```powershell
# 1. バックエンドディレクトリに移動
cd backend

# 2. ビルド
mvn clean package -q

# 3. 起動 (ポート8082で起動)
java -jar target\tablecraft-backend-0.0.1-SNAPSHOT.jar
```

**起動確認**:
```powershell
# テーブル一覧取得
Invoke-RestMethod -Uri "http://localhost:8082/api/admin/tables/list" -Method GET
```

#### 2. 手動起動 - 業務画面フロントエンド

```powershell
# 1. フロントエンドディレクトリに移動
cd frontend

# 2. 依存関係インストール (初回のみ)
npm install

# 3. 開発サーバー起動
npm run dev
```

#### 3. 手動起動 - 管理画面フロントエンド

```powershell
# 1. 管理画面ディレクトリに移動
cd frontend-admin

# 2. 依存関係インストール (初回のみ)
npm install

# 3. 開発サーバー起動 (ポート5174)
npm run dev
```

#### 4. アクセス方法

- **管理画面**: http://localhost:5174 ← テーブル定義を作成・編集
- **業務画面**: http://localhost:5173 ← データのCRUD操作
- **バックエンドAPI**: http://localhost:8082
- **MySQL接続**: localhost:3306/tablecraft

**システム状態確認**:
```powershell
# テーブル一覧取得
Invoke-RestMethod -Uri "http://localhost:8082/api/admin/tables/list" -Method GET

# ポート使用状況確認
netstat -ano | findstr :8082  # バックエンド
netstat -ano | findstr :5173  # 業務画面
netstat -ano | findstr :5174  # 管理画面
```

## 🛑 システム停止

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

## 📅 アーキテクチャ概要

### 設定ファイルフロー
```
管理画面 (http://localhost:5174)
↓
テーブル定義作成・編集
↓
MySQL (manual_table_definitions テーブルに保存)
↓
ConfigGeneratorService (table-config.json 自動生成)
↓
業務画面 (http://localhost:5173) で自動反映
```

テーブルは管理画面から動的に作成されます。初回起動時はテーブルが存在しないため、管理画面でテーブル定義を作成してください。

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

### 主な機能
- **管理画面からのテーブル定義管理**
- **動的DDL実行** (CREATE/DROP/TRUNCATE)
- **table-config.json自動生成**
- **カラム表示/非表示設定**
- **UI設定** (検索、ソート、CRUD権限)

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

### MySQL接続エラー
- `application.properties`の接続情報を確認
- MySQLサービスが起動しているか確認
- データベース`tablecraft`が存在するか確認

## 📁 ディレクトリ構造

```
TableCraft/
├── README.md                          # このファイル
├── TODO.md                            # 今後の実装予定
├── start-system.ps1                   # システム起動スクリプト (PowerShell)
├── start-system.bat                   # システム起動スクリプト (バッチ)
├── stop-system.ps1                    # システム停止スクリプト
├── backend/                           # バックエンド (Spring Boot)
│   ├── pom.xml                        # Maven設定ファイル
│   ├── src/main/java/com/tablecraft/app/
│   │   ├── Application.java           # メインクラス
│   │   ├── admin/                     # 管理機能
│   │   │   ├── controller/            # 管理API
│   │   │   │   └── TableDefinitionController.java
│   │   │   ├── service/               # 管理サービス
│   │   │   │   ├── TableDefinitionService.java
│   │   │   │   └── ConfigGeneratorService.java
│   │   │   ├── entity/                # エンティティ
│   │   │   │   └── ManualTableDefinition.java
│   │   │   └── repository/            # リポジトリ
│   │   │       └── ManualTableDefinitionRepository.java
│   │   └── dynamic/                   # 業務機能（動的API）
│   │       ├── ConfigBasedController.java
│   │       └── ConfigBasedTableService.java
│   ├── src/main/resources/
│   │   ├── application.properties     # 設定ファイル
│   │   └── config/                    # 自動生成された設定ファイル
│   │       └── table-config.json      # テーブル設定
│   └── target/                        # ビルド成果物
├── frontend/                          # 業務画面フロントエンド
│   ├── package.json                   # Node.js依存関係
│   ├── vite.config.ts                 # Vite設定
│   ├── src/
│   │   ├── App.tsx                    # メインアプリ
│   │   ├── components/
│   │   │   ├── Layout/                # レイアウト
│   │   │   ├── Forms/                 # フォーム
│   │   │   └── Tables/                # テーブル
│   │   └── hooks/
│   │       └── useTable.ts            # カスタムフック
│   └── public/
│       └── table-config.json          # 設定ファイル（自動生成）
└── frontend-admin/                    # 管理画面フロントエンド
    ├── package.json                   # Node.js依存関係
    ├── vite.config.ts                 # Vite設定
    ├── src/
    │   ├── App.tsx                    # メインアプリ
    │   ├── components/
    │   │   ├── Layout/                # レイアウト
    │   │   ├── TableEdit/             # テーブル編集
    │   │   ├── TableBuilder/          # テーブル作成
    │   │   └── SqlUpload/             # SQL アップロード
    │   └── api/
    │       └── adminApi.ts            # 管理API クライアント
    └── public/
```

## 📝 使い方

### 1. 管理画面でテーブル定義を作成
1. http://localhost:5174 にアクセス
2. 「テンプレートからテーブル作成」または「新規作成」
3. カラムを追加・編集
4. 表示/非表示、UI設定を調整
5. 「一括保存」ボタンで保存

### 2. 業務画面でデータ管理
1. http://localhost:5173 にアクセス
2. 作成したテーブルが自動表示
3. データの登録・編集・削除

### 3. 設定の自動反映
- 管理画面で保存すると自動的に：
  - MySQLにテーブルが作成される
  - `table-config.json`が生成される
  - 業務画面で即座に利用可能になる

---

## 🎯 主な機能

- **管理画面からテーブル定義作成**: コードを書かずにテーブル作成
- **テンプレート機能**: よく使うテーブル構造をテンプレート化
- **動的DDL実行**: テーブル構造変更を自動反映
- **カラム表示制御**: 一覧画面での表示/非表示設定
- **UI設定**: 検索、ソート、CRUD権限などを画面から設定
- **多言語対応**: 日本語/英語切り替え
- **自動設定ファイル生成**: `table-config.json`を自動生成

---

**🚀 クイックスタート**:
1. MySQL で `tablecraft` データベース作成
2. `start-system.ps1` を実行
3. 管理画面 (http://localhost:5174) でテーブル作成
4. 業務画面 (http://localhost:5173) でデータ管理