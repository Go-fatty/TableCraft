# TableCraft - システム起動手順

## 概要
TableCraftは、JSONメタデータから動的にCRUD APIとUIを生成する外部設定ファイル駆動型システムです。table-metadata.jsonから自動生成される設定ファイルにより、完全に動的なフルスタックアプリケーションを構築できます。

## システム構成
- **バックエンド**: Spring Boot 2.7.5 (Java) + JSON設定ベースアーキテクチャ
- **フロントエンド**: React + TypeScript + Vite
- **データベース**: MySQL 8.0
- **設定ファイル**: `table-metadata.json` → Python生成パイプライン → JSON設定ファイル群
- **API エンドポイント**: `/api/config/*` (JSON設定ベース)

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
`backend/src/main/resources/application-dev.properties` でMySQL認証情報を設定

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
# システムステータス確認
Invoke-RestMethod -Uri "http://localhost:8082/api/config/status" -Method POST -ContentType "application/json" -Body "{}"
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
  - ユーザー名: `sa`
  - パスワード: `password`

**システム状態確認**:
```powershell
# バックエンドAPIの動作確認
Invoke-RestMethod -Uri "http://localhost:8082/api/config/status" -Method POST -ContentType "application/json" -Body "{}"

# テーブル一覧取得
Invoke-RestMethod -Uri "http://localhost:8082/api/config/tables" -Method POST -ContentType "application/json" -Body "{}"

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

## 📅 アーキテクチャ概要

### 設定ファイルフロー
```
table-metadata.json (マスターデータ)
↓
Python生成パイプライン (settings_creates/)
↓
JSON設定ファイル群 (backend/src/main/resources/config/)
↓
Spring Boot JSON設定ベースAPI (/api/config/*)
↓
React UI (動的テーブル管理)
```

システム起動時に以下のテーブルが設定ファイルから動的に認識されます：

| テーブル名 | 説明 | 主キー |
|-----------|------|--------|
| users | ユーザー管理 | id (BIGINT) |
| categories | カテゴリ管理 | id (BIGINT) |
| products | 商品管理 | id (BIGINT) |
| order_details | 注文詳細 | order_id, product_id (複合) |

## 🔧 API エンドポイント

### JSON設定ベースCRUD操作
- `POST /api/config/status` - システムステータス・設定情報取得
- `POST /api/config/tables` - 設定されたテーブル一覧取得
- `POST /api/config/find` - データ検索・取得 (フィルタ・ページネーション対応)
- `POST /api/config/create` - データ新規作成
- `POST /api/config/update` - データ更新
- `POST /api/config/delete` - データ削除
- `POST /api/config/reload` - 外部設定ファイルの動的リロード

### 設定システムの特徴
- 外部JSON設定ファイル駆動
- ホットリロード対応（再起動不要）
- table-metadata.json ベースの完全動的テーブル管理

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
│   │   ├── model/                     # JSON設定用Javaモデル
│   │   │   ├── TableConfig.java       # テーブル設定モデル
│   │   │   ├── TableDefinition.java   # テーブル定義モデル
│   │   │   └── ColumnDefinition.java  # カラム定義モデル
│   │   ├── service/                   # サービス層
│   │   │   ├── ExternalConfigService.java     # 外部設定ファイル管理
│   │   │   └── ConfigBasedTableService.java   # JSON設定ベースCRUD操作
│   │   └── dynamic/
│   │       ├── ConfigBasedController.java     # JSON設定ベースREST API
│   │       └── FieldDefinition.java           # フィールド定義クラス
│   ├── src/main/resources/
│   │   ├── application.properties     # 設定ファイル
│   │   ├── config/                    # 外部設定ファイル群
│   │   │   ├── table-config.json      # テーブル設定 (Python生成)
│   │   │   ├── validation-config.json # バリデーション設定
│   │   │   ├── ui-config.json         # UI設定
│   │   │   ├── types.ts               # TypeScript型定義
│   │   │   └── useTable.ts            # React カスタムフック
│   │   └── messages_*.properties      # 多言語メッセージファイル
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

### 設定ファイル更新時の手順
1. `table-metadata.json` でメタデータを更新
2. `settings_creates/src/generator.py` で設定ファイルを再生成
3. 生成されたファイルを`backend/src/main/resources/config/`にコピー
4. `/api/config/reload` で動的リロード (再起動不要)

### 新しいテーブル追加時
1. `settings_creates/examples/table-metadata.json`でメタデータを定義
2. Python生成パイプラインで設定ファイルを再生成
3. 設定ファイルのリロードで即座反映

### ホットリロード機能
```bash
# 設定ファイルの動的リロード
Invoke-RestMethod -Uri "http://localhost:8082/api/config/reload" -Method POST -ContentType "application/json" -Body "{}"
```

## 🎯 機能概要

- **完全動的テーブル管理**: table-metadata.json ベースの外部設定ファイル駆動
- **ホットリロード**: サーバー再起動不要の設定変更
- **JSON設定ベースCRUD**: 外部ファイルからの動的API生成
- **リアルタイム操作**: フロントエンドからの即座操作
- **多言語対応**: 日本語/英語切り替え対応
- **設定ベースバリデーション**: 外部設定ファイルベースの入力検証
- **レスポンシブUI**: モバイル対応済み
- **将来の管理UI対応**: 動的テーブル構造変更のインフラ整備済み

---

**🚀 クイックスタート**:
1. `start-system.ps1` または `start-system.bat` を実行
2. システムが自動起動 (バックエンド: 8082, フロントエンド: 5173)
3. ブラウザで http://localhost:5173 にアクセス