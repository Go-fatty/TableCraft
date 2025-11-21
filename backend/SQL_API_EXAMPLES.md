# SQL-Based Dynamic REST API Examples

このファイルは、SQLファイルで定義されたテーブルに対するCRUD操作のAPI例を示します。

## 特徴

- **SQLファイルベース**: `table-definitions.sql`でテーブル構造を定義
- **詳細な型指定**: VARCHAR(100), DECIMAL(12,2), TEXT など詳細な型指定が可能
- **デフォルト値**: DEFAULT CURRENT_TIMESTAMP など
- **制約**: UNIQUE, CHECK制約なども設定可能
- **複雑なテーブル**: 6つのテーブルが定義済み

## サーバー起動
```bash
# プロジェクトディレクトリで実行
mvn spring-boot:run
```

**アクセス先:** http://localhost:8081

## SQLベースAPI（推奨）

新しいSQLベースのAPI（`/api/sql/*`）を使用します。

### 1. テーブル一覧取得

**URL:** `POST /api/sql/tables`
```bash
curl -X POST http://localhost:8081/api/sql/tables \
  -H "Content-Type: application/json"
```

### 2. テーブルスキーマ取得（詳細情報）

**URL:** `POST /api/sql/schema`
```bash
curl -X POST http://localhost:8081/api/sql/schema \
  -H "Content-Type: application/json" \
  -d '{"tableName": "users"}'
```

### 3. データ作成例

**URL:** `POST /api/sql/create`

#### ユーザー作成
```bash
curl -X POST http://localhost:8081/api/sql/create \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "data": {
      "name": "田中太郎",
      "email": "tanaka@example.com",
      "age": 30,
      "phone": "090-1234-5678",
      "address": "東京都渋谷区1-2-3"
    }
  }'
```

#### 商品作成（より詳細な型定義）
```bash
curl -X POST http://localhost:8081/api/sql/create \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "product",
    "data": {
      "name": "高性能ノートPC",
      "description": "最新CPUと大容量メモリを搭載した高性能ノートパソコン。ビジネスからゲームまで幅広く対応。",
      "price": 189999.99,
      "stock": 15,
      "category_id": 1,
      "sku": "PC-NB-2024-001",
      "weight": 2.1
    }
  }'
```

#### カテゴリ作成
```bash
curl -X POST http://localhost:8081/api/sql/create \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "category",
    "data": {
      "name": "ノートパソコン",
      "description": "持ち運び可能なノートパソコンカテゴリ",
      "parent_id": null,
      "sort_order": 1,
      "is_active": true
    }
  }'
```

#### 商品レビュー作成（新テーブル）
```bash
curl -X POST http://localhost:8081/api/sql/create \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "reviews",
    "data": {
      "user_id": 1,
      "product_id": 1,
      "rating": 5,
      "title": "素晴らしい商品です！",
      "comment": "期待以上の性能で、動作も軽快です。デザインも洗練されており、満足しています。",
      "is_verified_purchase": true,
      "helpful_count": 0
    }
  }'
```

#### 在庫履歴記録（新テーブル）
```bash
curl -X POST http://localhost:8081/api/sql/create \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "inventory_history",
    "data": {
      "product_id": 1,
      "change_type": "IN",
      "quantity_change": 20,
      "previous_stock": 15,
      "new_stock": 35,
      "reason": "新規入荷",
      "reference_id": null,
      "created_by": "倉庫管理者"
    }
  }'
```

### 4. データ検索

**URL:** `POST /api/sql/find`
```bash
curl -X POST http://localhost:8081/api/sql/find \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "id": 1
  }'
```

### 5. 全データ取得

**URL:** `POST /api/sql/findAll`
```bash
curl -X POST http://localhost:8081/api/sql/findAll \
  -H "Content-Type: application/json" \
  -d '{"tableName": "users"}'
```

### 6. データ更新

**URL:** `POST /api/sql/update`
```bash
curl -X POST http://localhost:8081/api/sql/update \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "id": 1,
    "data": {
      "name": "田中太郎（更新済）",
      "phone": "090-9999-8888",
      "address": "東京都新宿区4-5-6 新住所"
    }
  }'
```

### 7. データ削除

**URL:** `POST /api/sql/delete`
```bash
curl -X POST http://localhost:8081/api/sql/delete \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "id": 1
  }'
```

## PowerShellでのテスト例

```powershell
# テーブル一覧取得
Invoke-RestMethod -Uri 'http://localhost:8081/api/sql/tables' -Method POST -ContentType 'application/json'

# カテゴリ作成
$categoryData = @{
    tableName = "category"
    data = @{
        name = "デスクトップPC"
        description = "高性能デスクトップパソコン"
        parent_id = $null
        sort_order = 2
        is_active = $true
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri 'http://localhost:8081/api/sql/create' -Method POST -ContentType 'application/json' -Body $categoryData

# 商品検索
$findProduct = @{ tableName = "product"; id = 1 } | ConvertTo-Json
Invoke-RestMethod -Uri 'http://localhost:8081/api/sql/find' -Method POST -ContentType 'application/json' -Body $findProduct
```

## 定義されているテーブル（SQL版）

### 1. users（ユーザー）
- id: BIGINT AUTO_INCREMENT PRIMARY KEY
- name: VARCHAR(100) NOT NULL
- email: VARCHAR(255) NOT NULL UNIQUE
- age: INT
- phone: VARCHAR(20)
- address: VARCHAR(500)
- created_date, updated_date: 自動タイムスタンプ

### 2. product（商品）
- id: BIGINT AUTO_INCREMENT PRIMARY KEY
- name: VARCHAR(200) NOT NULL
- description: TEXT（長文対応）
- price: DECIMAL(12,2) NOT NULL（より高精度）
- stock: INT DEFAULT 0
- category_id: BIGINT
- sku: VARCHAR(50) UNIQUE（商品コード）
- weight: DECIMAL(8,3)（重量）
- created_date, updated_date: 自動タイムスタンプ

### 3. category（カテゴリ）
- id: BIGINT AUTO_INCREMENT PRIMARY KEY
- name: VARCHAR(100) NOT NULL
- description: TEXT
- parent_id: BIGINT（親カテゴリ）
- sort_order: INT DEFAULT 0
- is_active: BOOLEAN DEFAULT TRUE
- created_date: 自動タイムスタンプ

### 4. orders（注文）
- id: BIGINT AUTO_INCREMENT PRIMARY KEY
- user_id, product_id: BIGINT NOT NULL
- quantity: INT DEFAULT 1
- unit_price, total_price: DECIMAL(12,2) NOT NULL
- status: VARCHAR(20) DEFAULT 'PENDING'
- order_date, shipped_date, delivery_date: DATETIME
- notes: TEXT

### 5. reviews（レビュー）【新テーブル】
- id: BIGINT AUTO_INCREMENT PRIMARY KEY
- user_id, product_id: BIGINT NOT NULL
- rating: INT CHECK (1-5)
- title: VARCHAR(200)
- comment: TEXT
- is_verified_purchase: BOOLEAN DEFAULT FALSE
- helpful_count: INT DEFAULT 0
- created_date: 自動タイムスタンプ

### 6. inventory_history（在庫履歴）【新テーブル】
- id: BIGINT AUTO_INCREMENT PRIMARY KEY
- product_id: BIGINT NOT NULL
- change_type: VARCHAR(20) NOT NULL ('IN', 'OUT', 'ADJUSTMENT')
- quantity_change, previous_stock, new_stock: INT NOT NULL
- reason: VARCHAR(200)
- reference_id: BIGINT（関連ID）
- created_date: 自動タイムスタンプ
- created_by: VARCHAR(100)

## 新しいテーブルの追加方法

1. `src/main/resources/table-definitions.sql`を編集
2. 新しいCREATE TABLE文を追加：

```sql
-- 新しいテーブルの例
CREATE TABLE IF NOT EXISTS my_new_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    custom_field VARCHAR(50) UNIQUE,
    price DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    metadata TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHECK (price >= 0)
);
```

3. アプリケーションを再起動

## SQLの利点

- **型の精密さ**: VARCHAR(100) vs VARCHAR(255) など
- **デフォルト値**: DEFAULT CURRENT_TIMESTAMP
- **制約**: UNIQUE, CHECK, FOREIGN KEY
- **インデックス**: CREATE INDEX文も追加可能
- **列追加**: ALTER TABLE文でスキーマ変更も可能
- **データ型の豊富さ**: TEXT, ENUM, DECIMAL精度指定など

## 従来のプロパティファイル版API

従来の`/api/*`エンドポイントも引き続き利用可能ですが、SQLベースの`/api/sql/*`の使用を推奨します。