# データベースマイグレーション手順

## Phase 0: データベース初期化

### 概要
既存の`manual_table_definitions`と`parsed_table_definitions`から、UI設定のみを抽出して新しい`table_ui_settings`テーブルに移行します。

### 実行前の確認事項
- [ ] MySQLサーバーが起動していること
- [ ] バックアップ領域が十分にあること
- [ ] 本番環境の場合は、必ず本番バックアップを取得すること

### 実行手順

#### 1. データベースに接続
```bash
mysql -u root -p tablecraft_db
```

#### 2. マイグレーションSQLを実行
```sql
source C:/work/projects/thinking/TableCraft/backend/db-migration/phase0_initialize.sql
```

または、コマンドラインから:
```bash
mysql -u root -p tablecraft_db < C:/work/projects/thinking/TableCraft/backend/db-migration/phase0_initialize.sql
```

#### 3. 実行結果の確認
マイグレーションが成功すると、以下の出力が表示されます:
- ✓ バックアップ完了メッセージ
- ✓ 新テーブル作成完了メッセージ
- ✓ マイグレーション完了メッセージ
- マイグレーション結果サマリー
- マイグレーションされたUI設定のサンプル

### 作成されるテーブル

#### `table_ui_settings`
実際のMySQLテーブルに対するUI設定を保存するテーブル

| カラム名 | 型 | 説明 |
|---------|-----|------|
| id | BIGINT | 主キー（自動採番） |
| table_name | VARCHAR(255) | テーブル名（ユニーク制約） |
| display_name | VARCHAR(255) | 画面表示名 |
| description | TEXT | テーブルの説明 |
| enable_search | BOOLEAN | 検索機能の有効化 |
| enable_sort | BOOLEAN | ソート機能の有効化 |
| enable_pagination | BOOLEAN | ページネーション機能の有効化 |
| page_size | INT | 1ページあたりの表示件数 |
| allow_create | BOOLEAN | 新規作成の許可 |
| allow_edit | BOOLEAN | 編集の許可 |
| allow_delete | BOOLEAN | 削除の許可 |
| allow_bulk | BOOLEAN | 一括操作の許可 |
| created_by | VARCHAR(100) | 作成者 |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

### バックアップテーブル

#### `manual_table_definitions_backup`
既存の`manual_table_definitions`の完全なコピー

#### `parsed_table_definitions_backup`
既存の`parsed_table_definitions`の完全なコピー

### ロールバック手順

万が一問題が発生した場合:

```sql
-- 新テーブルを削除
DROP TABLE IF EXISTS table_ui_settings;

-- バックアップから復元（必要に応じて）
-- manual_table_definitionsとparsed_table_definitionsは削除していないため、
-- 通常はロールバック不要
```

### 次のステップ

Phase 0完了後:
- **Phase 1**: バックエンド - 新サービスクラスの作成
  - `TableSchemaService` (INFORMATION_SCHEMA取得)
  - `TableUISettingsService` (UI設定管理)
  - エンティティとRepositoryの作成

### トラブルシューティング

#### エラー: テーブルが既に存在する
```
Table 'table_ui_settings' already exists
```
**対処**: `DROP TABLE IF EXISTS table_ui_settings;` を実行してから再度マイグレーションを実行

#### エラー: 外部キー制約違反
```
Cannot add or update a child row: a foreign key constraint fails
```
**対処**: マイグレーション順序を確認し、依存テーブルから先に作成

#### データが正しくマイグレーションされない
**確認クエリ**:
```sql
-- 元データの件数確認
SELECT COUNT(*) FROM manual_table_definitions;

-- マイグレーション後の件数確認
SELECT COUNT(*) FROM table_ui_settings;

-- データの比較
SELECT 
    m.table_name,
    m.display_name,
    u.display_name AS migrated_display_name
FROM manual_table_definitions m
LEFT JOIN table_ui_settings u ON m.table_name = u.table_name;
```

### 注意事項

1. **既存テーブルは削除しない**: `manual_table_definitions`と`parsed_table_definitions`は、Phase 4のクリーンアップまで残します
2. **バックアップテーブル**: `_backup`接尾辞のついたテーブルは、問題発生時の復旧用です
3. **本番環境**: 本番環境で実行する前に、必ずステージング環境でテストしてください
