# TableCraft API使用例

## 概要

TableCraft APIは、JSONベースの外部設定システムを使用してテーブル操作を行います。従来のハードコード化されたテーブル定義から、完全に設定可能な外部JSONファイルベースのアーキテクチャに移行しました。

## アーキテクチャ概要

```
table-metadata.json → Python生成 → JSON設定ファイル → Spring Boot API
                                      ↓
                            table-config.json
                            ui-config.json
                            validation-config.json
```

## 基本API エンドポイント

### 設定API (/api/config/*)

#### テーブル設定の取得
```bash
# 利用可能なテーブル一覧を取得
GET /api/config/tables
```

**レスポンス例:**
```json
{
  "success": true,
  "data": [
    {
      "id": "users",
      "label": "ユーザー管理",
      "description": "システムユーザーの管理"
    },
    {
      "id": "products",
      "label": "商品管理",
      "description": "商品情報の管理"
    }
  ]
}
```

#### UI設定の取得
```bash
GET /api/config/ui
```

**レスポンス例:**
```json
{
  "success": true,
  "data": {
    "pagination": {
      "defaultPageSize": 10,
      "pageSizeOptions": [10, 20, 50, 100]
    },
    "dateFormat": "yyyy-MM-dd",
    "theme": "default"
  }
}
```

#### バリデーション設定の取得
```bash
GET /api/config/validation
```

**レスポンス例:**
```json
{
  "success": true,
  "data": {
    "required": ["name", "email"],
    "minLength": {
      "name": 2,
      "password": 8
    },
    "patterns": {
      "email": "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"
    }
  }
}
```

## データ操作API (/api/config/data/*)

### 基本CRUD操作

#### データの取得
```bash
# 全データ取得（ページング対応）
GET /api/config/data/users?page=0&size=20

# フィルタリング
GET /api/config/data/users?name=田中&status=active

# ソート
GET /api/config/data/users?sort=createdAt,desc
```

**レスポンス例:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "田中太郎",
        "email": "tanaka@example.com",
        "status": "active",
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "pageable": {
      "page": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1
    }
  }
}
```

#### データの作成
```bash
POST /api/config/data/users
Content-Type: application/json

{
  "name": "佐藤花子",
  "email": "sato@example.com",
  "status": "active"
}
```

#### データの更新
```bash
PUT /api/config/data/users/1
Content-Type: application/json

{
  "name": "田中太郎（更新）",
  "email": "tanaka.updated@example.com",
  "status": "active"
}
```

#### データの削除
```bash
DELETE /api/config/data/users/1
```

## 高度な機能

### 設定のホットリロード
```bash
POST /api/config/reload
```

この機能により、サーバーを再起動することなく、JSON設定ファイルの変更をリアルタイムで反映できます。

**使用例:**
1. `table-metadata.json`を編集
2. Python生成パイプラインを実行
3. `/api/config/reload`を呼び出し
4. 新しい設定が即座に反映される

### スキーマ情報の取得
```bash
GET /api/config/schema/users
```

**レスポンス例:**
```json
{
  "success": true,
  "data": {
    "tableName": "users",
    "columns": [
      {
        "name": "id",
        "type": "BIGINT",
        "nullable": false,
        "primaryKey": true,
        "autoIncrement": true
      },
      {
        "name": "name",
        "type": "VARCHAR",
        "length": 255,
        "nullable": false
      },
      {
        "name": "email",
        "type": "VARCHAR",
        "length": 255,
        "nullable": false,
        "unique": true
      }
    ]
  }
}
```

## エラーハンドリング

### バリデーションエラー
```json
{
  "success": false,
  "message": "バリデーションエラーが発生しました",
  "errors": [
    {
      "field": "email",
      "message": "有効なメールアドレスを入力してください"
    }
  ]
}
```

### システムエラー
```json
{
  "success": false,
  "message": "内部サーバーエラーが発生しました",
  "timestamp": "2024-01-15T10:30:00"
}
```

## フロントエンド連携

### React Hooks の使用例
```typescript
import { useTable } from '../hooks/useTable';

function UsersList() {
  const {
    data,
    loading,
    error,
    fetchData,
    createRecord,
    updateRecord,
    deleteRecord
  } = useTable('users');

  // 自動的に /api/config/data/users にアクセス
  useEffect(() => {
    fetchData();
  }, []);

  return (
    <div>
      {loading && <div>読み込み中...</div>}
      {error && <div>エラー: {error.message}</div>}
      {data && (
        <ul>
          {data.content.map(user => (
            <li key={user.id}>{user.name}</li>
          ))}
        </ul>
      )}
    </div>
  );
}
```

## 開発ワークフロー

### 1. テーブル定義の追加/変更
```bash
# 1. table-metadata.json を編集
vim settings_creates/examples/table-metadata.json

# 2. 設定ファイルを生成
cd settings_creates
python src/generator.py

# 3. 設定をリロード
curl -X POST http://localhost:8080/api/config/reload
```

### 2. 新しいテーブルの確認
```bash
# テーブル一覧で新しいテーブルを確認
curl http://localhost:8080/api/config/tables

# スキーマ情報を確認
curl http://localhost:8080/api/config/schema/new_table
```

## 設定ファイルの構造

### table-config.json
```json
{
  "tables": {
    "users": {
      "label": "ユーザー管理",
      "description": "システムユーザーの管理",
      "columns": {
        "id": { "type": "id", "label": "ID" },
        "name": { "type": "text", "label": "名前", "required": true }
      }
    }
  }
}
```

### ui-config.json
```json
{
  "pagination": {
    "defaultPageSize": 10,
    "pageSizeOptions": [10, 20, 50, 100]
  },
  "dateFormat": "yyyy-MM-dd"
}
```

### validation-config.json
```json
{
  "users": {
    "name": {
      "required": true,
      "minLength": 2,
      "maxLength": 100
    },
    "email": {
      "required": true,
      "pattern": "email"
    }
  }
}
```

## パフォーマンスとセキュリティ

### ページング
- デフォルトページサイズ: 10件
- 最大ページサイズ: 100件
- 大量データに対する効率的な処理

### セキュリティ
- SQLインジェクション対策
- CSRF保護
- 入力値のバリデーション
- ログイン認証（予定）

## まとめ

TableCraft APIは、JSONベースの外部設定システムにより、柔軟で保守しやすいテーブル管理システムを提供します。ホットリロード機能により、開発効率が大幅に向上し、設定変更が即座に反映されます。