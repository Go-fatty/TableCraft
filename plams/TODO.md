# TableCraft - 今後の実装予定

## 📋 業務画面の機能拡張

### 1. 登録・更新画面の項目制御
**目的**: 登録時と更新時で表示・編集可能な項目を分ける

#### 実装内容
- **カラムプロパティの追加**
  - `editableOnCreate`: 登録時のみ編集可能（例: コード）
  - `editableOnUpdate`: 更新時のみ編集可能（例: ステータス）
  - `hiddenOnCreate`: 登録時は非表示（例: 作成日時）
  - `hiddenOnUpdate`: 更新時は非表示

#### 修正対象ファイル
- `backend/src/main/java/com/tablecraft/app/admin/service/TableDefinitionService.java`
  - `ColumnRequest`に新プロパティ追加
  - `regenerateTableConfig`で反映

- `frontend-admin/src/components/TableEdit/FieldModal.tsx`
  - 編集モーダルにチェックボックス追加

- `frontend/src/components/Forms/DynamicForm.tsx`
  - `mode`（create/update）によって項目の表示・編集可否を制御

#### 実装例
```typescript
// table-config.json
{
  "name": "code",
  "label": {"ja": "コード", "en": "Code"},
  "editableOnCreate": true,
  "editableOnUpdate": false,  // 更新時は編集不可
  "hiddenOnCreate": false,
  "hiddenOnUpdate": false
}
```

---

### 2. 画面カテゴリ設定
**目的**: テーブルをカテゴリ別に整理し、サイドバーで見やすく表示

#### 実装内容
- **テーブルにカテゴリ情報を追加**
  - カテゴリ: `master`, `transaction`, `report`, `other`
  - カテゴリ表示名（多言語対応）

- **サイドバーのグルーピング表示**
  - カテゴリごとにセクション分け
  - アコーディオン形式で開閉可能

#### 修正対象ファイル
- `backend/src/main/java/com/tablecraft/app/admin/entity/ManualTableDefinition.java`
  - `category`フィールド追加

- `backend/src/main/java/com/tablecraft/app/admin/service/TableDefinitionService.java`
  - `TableDefinitionRequest`に`category`追加
  - `regenerateTableConfig`でカテゴリ情報を反映

- `frontend-admin/src/components/TableEdit/TableEditPanel.tsx`
  - カテゴリ選択ドロップダウン追加

- `frontend/src/components/Layout/Sidebar.tsx`
  - カテゴリごとのグルーピング表示

#### 実装例
```json
// table-config.json
{
  "tables": {
    "user": {
      "category": "master",
      "categoryLabel": {"ja": "マスタ", "en": "Master"}
    },
    "order": {
      "category": "transaction",
      "categoryLabel": {"ja": "業務", "en": "Transaction"}
    }
  }
}
```

---

## 🛠️ 管理画面の機能拡張

### 3. Swagger UIへのリンク
**目的**: API仕様をすぐに確認できるようにする

#### 実装内容
- ヘッダーに「API仕様」ボタンを追加
- Swagger UIを新しいタブで開く

#### 修正対象ファイル
- `frontend-admin/src/components/Layout/Header.tsx`
  - 「API仕様」ボタン追加
  - `window.open('http://localhost:8082/swagger-ui.html', '_blank')`

#### 実装例
```tsx
<button 
  className="btn btn-secondary"
  onClick={() => window.open('http://localhost:8082/swagger-ui.html', '_blank')}
>
  📖 API仕様
</button>
```

---

## 💾 その他の機能

### 4. DB初期化スクリプト
**目的**: 新規環境構築時にDBを簡単に初期化できるようにする

#### 実装内容
- **SQLファイル作成**
  - `db/schema.sql`: テーブル定義
    - `manual_table_definitions`
    - `parsed_table_definitions`
    - その他システムテーブル
  
  - `db/data.sql`: 初期データ
    - サンプルテーブル定義
    - デフォルト設定

- **バッチファイル作成**
  - `init-db.bat` (Windows)
  - `init-db.sh` (Linux/Mac)
  - MySQLに接続してSQLファイルを実行

#### ファイル構成
```
TableCraft/
├── db/
│   ├── schema.sql          # テーブル定義
│   ├── data.sql            # 初期データ
│   ├── init-db.bat         # Windows用実行スクリプト
│   └── init-db.sh          # Linux/Mac用実行スクリプト
└── README.md               # 初期化手順を追記
```

#### init-db.bat 実装例
```batch
@echo off
echo TableCraft DB初期化スクリプト
echo ==============================

set MYSQL_USER=root
set MYSQL_PASS=root
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set DB_NAME=tablecraft

echo データベースを削除して再作成します...
mysql -u%MYSQL_USER% -p%MYSQL_PASS% -h%MYSQL_HOST% -P%MYSQL_PORT% -e "DROP DATABASE IF EXISTS %DB_NAME%; CREATE DATABASE %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo テーブルを作成します...
mysql -u%MYSQL_USER% -p%MYSQL_PASS% -h%MYSQL_HOST% -P%MYSQL_PORT% %DB_NAME% < db/schema.sql

echo 初期データを投入します...
mysql -u%MYSQL_USER% -p%MYSQL_PASS% -h%MYSQL_HOST% -P%MYSQL_PORT% %DB_NAME% < db/data.sql

echo ✅ DB初期化完了
pause
```

#### schema.sql 実装例
```sql
-- manual_table_definitions テーブル
CREATE TABLE IF NOT EXISTS manual_table_definitions (
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

-- parsed_table_definitions テーブル
CREATE TABLE IF NOT EXISTS parsed_table_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL,
    schema_name VARCHAR(255),
    table_type VARCHAR(50),
    table_comment TEXT,
    columns TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_table_schema (table_name, schema_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 📝 実装の優先順位

### Phase 1 (優先度: 高)
1. **DB初期化スクリプト** - 環境構築の簡易化
2. **Swagger UIリンク** - 開発効率向上

### Phase 2 (優先度: 中)
3. **画面カテゴリ設定** - UI/UX改善

### Phase 3 (優先度: 低)
4. **登録・更新画面の項目制御** - 詳細な画面制御

---

## 🔧 技術メモ

### DB初期化について
- `application.properties`で`spring.jpa.hibernate.ddl-auto=none`に設定し、手動スキーマ管理を推奨
- 本番環境ではマイグレーションツール（Flyway/Liquibase）の導入を検討

### カテゴリ機能について
- 将来的にカテゴリをDBで管理し、動的に追加・編集可能にすることも検討
- アイコンの設定も追加可能（`categoryIcon`プロパティ）

### API仕様について
- SpringDoc（Swagger）の設定を確認し、適切なアノテーションが付いているか確認
- セキュリティ設定でSwagger UIへのアクセスを許可

---

## 🗑️ 削除・廃止予定

### settings_creates ディレクトリ
**理由**: 管理画面から直接テーブル定義を登録・編集できるようになったため不要

#### 削除対象
- `settings_creates/src/*.py` - すべてのPythonスクリプト
  - `frontend_generator.py`
  - `generator.py`
  - `metadata_parser.py`
  - `properties_generator.py`
  - `sql_generator.py`
- `settings_creates/examples/` - サンプルファイル
- `settings_creates/output/` - 生成ファイル（管理画面で自動生成されるため不要）
- `settings_creates/templates/` - テンプレートファイル
- `settings_creates/requirements.txt`
- `settings_creates/README.md`

#### 残すファイル（参考用）
- 削除前にドキュメントとして内容を`docs/legacy/`に移動することを検討

#### 削除手順
```powershell
# バックアップ（念のため）
Compress-Archive -Path settings_creates -DestinationPath settings_creates_backup.zip

# ディレクトリ削除
Remove-Item -Path settings_creates -Recurse -Force
```

---

## ✅ 完了済み機能（2025/12/01時点）

### 管理画面
- ✅ テーブル一覧表示
- ✅ テーブル新規作成（テンプレート機能）
- ✅ テーブル編集（カラム追加・削除・編集）
- ✅ カラムの表示/非表示設定
- ✅ UI設定（検索、ソート、ページネーション、CRUD権限）
- ✅ 一括保存機能
- ✅ テーブル削除（DB実テーブルも削除）

### 業務画面
- ✅ テーブルデータの一覧表示
- ✅ 検索・ソート機能
- ✅ データのCRUD操作
- ✅ 外部キー参照
- ✅ `visible`設定の反映

### バックエンド
- ✅ テーブル定義のCRUD API
- ✅ 動的DDL実行（CREATE/DROP/TRUNCATE）
- ✅ `table-config.json`自動生成
- ✅ VARCHAR型の自動正規化
- ✅ UI設定のDB保存
- ✅ 多言語対応のラベル生成
