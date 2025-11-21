# Dynamic Spring Boot REST API Examples

このファイルは、プロパティファイルで定義されたテーブルに対するCRUD操作のAPI例を示します。

## サーバー起動
```bash
# プロジェクトディレクトリで実行
mvn spring-boot:run
```

**アクセス先:** http://localhost:8081

## API一覧

すべてのAPIはPOSTメソッドを使用します。

### 1. テーブル一覧取得
利用可能なテーブル一覧を取得します。

**URL:** `POST /api/tables`
```bash
curl -X POST http://localhost:8081/api/tables \
  -H "Content-Type: application/json"
```

### 2. テーブルスキーマ取得
指定したテーブルのスキーマ情報を取得します。

**URL:** `POST /api/schema`
```bash
curl -X POST http://localhost:8081/api/schema \
  -H "Content-Type: application/json" \
  -d '{"tableName": "users"}'
```

### 3. データ作成 (CREATE)
新しいレコードを作成します。

**URL:** `POST /api/create`
```bash
# usersテーブルの例
curl -X POST http://localhost:8081/api/create \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "data": {
      "name": "田中太郎",
      "email": "tanaka@example.com",
      "age": 30
    }
  }'

# productテーブルの例
curl -X POST http://localhost:8081/api/create \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "product",
    "data": {
      "name": "ノートパソコン",
      "description": "高性能なビジネス向けノートパソコン",
      "price": 150000.00,
      "stock": 10
    }
  }'
```

### 4. データ検索 (READ)
IDでレコードを検索します。

**URL:** `POST /api/find`
```bash
curl -X POST http://localhost:8081/api/find \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "id": 1
  }'
```

### 5. 全データ取得 (READ ALL)
テーブルの全レコードを取得します。

**URL:** `POST /api/findAll`
```bash
curl -X POST http://localhost:8081/api/findAll \
  -H "Content-Type: application/json" \
  -d '{"tableName": "users"}'
```

### 6. データ更新 (UPDATE)
既存のレコードを更新します。

**URL:** `POST /api/update`
```bash
curl -X POST http://localhost:8081/api/update \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "id": 1,
    "data": {
      "name": "田中太郎（更新済）",
      "email": "tanaka.updated@example.com",
      "age": 31
    }
  }'
```

### 7. データ削除 (DELETE)
レコードを削除します。

**URL:** `POST /api/delete`
```bash
curl -X POST http://localhost:8081/api/delete \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "id": 1
  }'
```

## PowerShellでのテスト例

Windows PowerShellでInvoke-RestMethodを使用する場合：

```powershell
# テーブル一覧取得
Invoke-RestMethod -Uri 'http://localhost:8081/api/tables' -Method POST -ContentType 'application/json'

# ユーザー作成
$userData = @{
    tableName = "users"
    data = @{
        name = "佐藤花子"
        email = "sato@example.com"
        age = 25
    }
} | ConvertTo-Json

Invoke-RestMethod -Uri 'http://localhost:8081/api/create' -Method POST -ContentType 'application/json' -Body $userData

# 全ユーザー取得
$getAllUsers = @{ tableName = "users" } | ConvertTo-Json
Invoke-RestMethod -Uri 'http://localhost:8081/api/findAll' -Method POST -ContentType 'application/json' -Body $getAllUsers
```

## テーブル定義

現在プロパティファイルで定義されているテーブル：

1. **users** - ユーザー情報テーブル
   - id: BIGINT (AUTO_INCREMENT, PRIMARY KEY)
   - name: VARCHAR(255) NOT NULL
   - email: VARCHAR(255) NOT NULL
   - age: INT
   - created_date: DATETIME

2. **product** - 商品情報テーブル
   - id: BIGINT (AUTO_INCREMENT, PRIMARY KEY) 
   - name: VARCHAR(255) NOT NULL
   - description: VARCHAR(255)
   - price: DECIMAL(10,2) NOT NULL
   - stock: INT
   - created_date: DATETIME

3. **orders** - 注文情報テーブル
   - id: BIGINT (AUTO_INCREMENT, PRIMARY KEY)
   - user_id: BIGINT NOT NULL
   - product_id: BIGINT NOT NULL
   - quantity: INT NOT NULL
   - total_price: DECIMAL(10,2) NOT NULL
   - order_date: DATETIME

4. **category** - カテゴリ情報テーブル
   - id: BIGINT (AUTO_INCREMENT, PRIMARY KEY)
   - name: VARCHAR(255) NOT NULL
   - description: VARCHAR(255)

## MySQLデータベース接続

開発時のデータベース確認用：
- **ホスト:** localhost
- **ポート:** 3306  
- **データベース:** tablecraft
- **ユーザー名:** root (application-dev.properties で設定)
- **パスワード:** (application-dev.properties で設定)

## 新しいテーブルの追加方法

1. `src/main/resources/table-definitions.properties`を編集
2. 以下の形式で新しいテーブルを定義：

```properties
# テーブル名: new_table の例
tables.new_table.fields.id=BIGINT:auto_increment,not_null,primary_key
tables.new_table.fields.field_name=VARCHAR(255):not_null
tables.new_table.fields.optional_field=INT:
```

3. アプリケーションを再起動すると自動的にテーブルが作成される

## フィールドタイプと属性

**データタイプ:**
- BIGINT
- VARCHAR(サイズ)
- INT
- DECIMAL(桁数,小数点桁数)
- DATETIME

**属性（コロンの後に指定、カンマ区切り）:**
- `not_null` - NOT NULL制約
- `auto_increment` - AUTO_INCREMENT
- `primary_key` - PRIMARY KEY
- 属性なしの場合はコロンのみまたは空白