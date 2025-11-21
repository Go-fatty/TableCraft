# TableCraft - システム起動手順

## 概要
TableCraftは、JSONメタデータから動的にCRUD APIとUIを生成する設定ファイル駆動型システムです。テーブル定義だけでフルスタックアプリケーションを構築できます。

## システム構成
- **バックエンド**: Spring Boot 2.7.5 (Java)
- **フロントエンド**: React + TypeScript + Vite
- **データベース**: H2 (インメモリ)
- **設定ファイル**: `json_create/output/` の生成ファイルを使用

## 🚀 システム起動手順

### 前提条件
- Java 11以上
- Node.js 16以上
- Maven 3.6以上

### 1. バックエンド起動 (Spring Boot)

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

### 2. フロントエンド起動 (React + Vite)

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
- **H2コンソール**: http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - ユーザー名: `sa`
  - パスワード: `password`

## 📋 利用可能なテーブル

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
APIServerByProperties/
├── README.md                          # このファイル
├── backend/                           # バックエンド (Spring Boot)
│   ├── src/main/java/com/autostack/builder/
│   │   ├── Application.java           # メインクラス
│   │   └── dynamic/
│   │       ├── SqlBasedController.java    # REST API コントローラー
│   │       └── SqlBasedTableService.java  # テーブル操作サービス
│   ├── src/main/resources/
│   │   ├── application.properties     # 設定ファイル
│   │   ├── table-definitions.sql      # テーブル定義SQL
│   │   ├── table-config.json         # テーブル設定 (json_createから)
│   │   ├── validation-config.json    # バリデーション設定
│   │   └── ui-config.json            # UI設定
│   └── target/                       # ビルド成果物
├── frontend/                         # フロントエンド (React + Vite)
│   ├── src/
│   │   ├── App.tsx                   # メインアプリ
│   │   └── components/
│   │       ├── Layout/               # レイアウトコンポーネント
│   │       ├── Forms/                # フォームコンポーネント
│   │       └── Tables/               # テーブルコンポーネント
│   └── package.json
└── json_create/                      # 設定ファイル生成ツール
    └── output/                       # 生成された設定ファイル
        └── frontend/                 # フロントエンド用設定
```

## 📝 開発メモ

### 設定ファイル更新時の手順
1. `json_create`で設定ファイルを再生成
2. 設定ファイルをSpring Bootのresourcesにコピー
3. Spring Bootを再起動

### 新しいテーブル追加時
1. `table-definitions.sql`にCREATE文を追加
2. `json_create/output/frontend/`の設定ファイルを更新
3. 設定ファイルをコピーして再起動

## 🎯 機能概要

- **動的テーブル管理**: 設定ファイルベースでテーブル操作
- **リアルタイムCRUD**: フロントエンドからの即座な操作
- **多言語対応**: 日本語/英語切り替え対応
- **バリデーション**: 設定ファイルベースの入力検証
- **レスポンシブUI**: モバイル対応済み

---

**🚀 クイックスタート**:
1. バックエンド起動 → Spring Boot (ポート8082)
2. フロントエンド起動 → React (ポート5173)  
3. ブラウザで http://localhost:5173 にアクセス