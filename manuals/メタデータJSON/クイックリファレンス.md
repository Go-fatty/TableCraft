# メタデータJSON クイックリファレンス

## 設定ファイル一覧

| ファイル名 | 用途 | 更新頻度 |
|-----------|------|---------|
| `table-config.json` | テーブル・フォーム・一覧設定 | 高 |
| `validation-config.json` | バリデーションルール | 中 |
| `ui-config.json` | UI設定（将来拡張用） | 低 |
| `messages_ja.properties` | 日本語メッセージ | 中 |
| `messages_en.properties` | 英語メッセージ | 中 |

## よく使う設定パターン

### 1. 基本的なテキストフィールド
```json
{
  "name": "title",
  "type": "text",
  "label": {"ja": "タイトル", "en": "Title"},
  "placeholder": {"ja": "タイトルを入力", "en": "Enter title"},
  "required": true,
  "maxLength": 100,
  "validation": {
    "required": true,
    "maxLength": 100
  }
}
```

### 2. 外部キー選択フィールド
```json
{
  "name": "category_id",
  "type": "select",
  "label": {"ja": "カテゴリー", "en": "Category"},
  "required": false,
  "options": {
    "type": "foreign_key",
    "table": "categories",
    "valueColumn": "id",
    "displayColumn": "name",
    "allowNull": true,
    "nullLabel": {"ja": "選択なし", "en": "None"}
  }
}
```

### 3. 数値フィールド
```json
{
  "name": "price",
  "type": "number",
  "label": {"ja": "価格", "en": "Price"},
  "required": true,
  "min": 0,
  "step": "0.01",
  "validation": {
    "required": true,
    "min": 0
  }
}
```

### 4. 日付フィールド
```json
{
  "name": "start_date",
  "type": "date",
  "label": {"ja": "開始日", "en": "Start Date"},
  "required": true,
  "validation": {
    "required": true
  }
}
```

### 5. チェックボックス
```json
{
  "name": "is_active",
  "type": "checkbox",
  "label": {"ja": "有効", "en": "Active"},
  "required": false,
  "defaultValue": "true",
  "validation": {
    "required": false
  }
}
```

### 6. 複数行テキスト
```json
{
  "name": "description",
  "type": "textarea",
  "label": {"ja": "説明", "en": "Description"},
  "placeholder": {"ja": "説明を入力", "en": "Enter description"},
  "required": false,
  "rows": 4,
  "maxLength": 500
}
```

## 一覧表示設定

### 通常カラム
```json
{
  "name": "title",
  "label": {"ja": "タイトル", "en": "Title"},
  "type": "VARCHAR",
  "sortable": true,
  "searchable": true,
  "width": "auto",
  "align": "left"
}
```

### 外部キー表示
```json
{
  "name": "category_id",
  "label": {"ja": "カテゴリー", "en": "Category"},
  "type": "BIGINT",
  "sortable": true,
  "searchable": true,
  "width": "auto",
  "align": "left",
  "foreignKey": {
    "table": "categories",
    "displayColumn": "name"
  }
}
```

### 数値フォーマット
```json
{
  "name": "price",
  "label": {"ja": "価格", "en": "Price"},
  "type": "DECIMAL",
  "sortable": true,
  "searchable": true,
  "width": "auto",
  "align": "right",
  "format": "decimal",
  "decimalPlaces": 2
}
```

### ブール値表示
```json
{
  "name": "is_active",
  "label": {"ja": "有効", "en": "Active"},
  "type": "BOOLEAN",
  "sortable": true,
  "searchable": false,
  "width": "80px",
  "align": "center",
  "format": "boolean"
}
```

## バリデーションルール

| ルール | 説明 | 例 |
|-------|------|-----|
| `required` | 必須チェック | `{"type": "required", "message": "validation.required"}` |
| `minLength` | 最小文字数 | `{"type": "minLength", "value": 2, "message": "validation.minLength"}` |
| `maxLength` | 最大文字数 | `{"type": "maxLength", "value": 100, "message": "validation.maxLength"}` |
| `min` | 最小値 | `{"type": "min", "value": 0, "message": "validation.min"}` |
| `max` | 最大値 | `{"type": "max", "value": 999999, "message": "validation.max"}` |
| `pattern` | パターン | `{"type": "pattern", "value": "email", "message": "validation.email"}` |

## メッセージキー

### 基本バリデーション
```properties
validation.required=この項目は必須です
validation.minLength=最低{0}文字入力してください
validation.maxLength=最大{0}文字まで入力可能です
validation.min=最小値は{0}です
validation.max=最大値は{0}です
validation.email=正しいメールアドレスを入力してください
```

### UI表示
```properties
ui.loading=読み込み中...
ui.error=エラーが発生しました
ui.save=保存
ui.cancel=キャンセル
ui.edit=編集
ui.delete=削除
ui.create=新規作成
```

## UI制御オプション

| プロパティ | 効果 | 使用場面 |
|-----------|------|---------|
| `hidden: true` | フィールド非表示 | ID、作成日時など |
| `readonly: true` | 読み取り専用 | 自動計算値など |
| `disabled: true` | 入力無効 | 条件付き無効化 |
| `required: true` | 必須フィールド | 必須項目 |

## 複合主キー設定例

```json
"primaryKey": {
  "type": "composite",
  "columns": ["order_id", "product_id"]
},
"columns": [
  {
    "name": "order_id",
    "constraints": {
      "primaryKey": true,
      "nullable": false
    },
    "foreignKey": {
      "table": "orders",
      "column": "id",
      "displayColumn": "order_number"
    }
  },
  {
    "name": "product_id", 
    "constraints": {
      "primaryKey": true,
      "nullable": false
    },
    "foreignKey": {
      "table": "products",
      "column": "id", 
      "displayColumn": "name"
    }
  }
]
```

## 新テーブル追加チェックリスト

- [ ] SQLファイルでテーブル作成
- [ ] `table-config.json` に追加
  - [ ] メタデータ設定
  - [ ] カラム定義
  - [ ] フォームフィールド
  - [ ] 一覧カラム
- [ ] `validation-config.json` に追加
- [ ] メッセージファイル更新（必要に応じて）
- [ ] システム再起動
- [ ] 動作確認（CRUD操作）

## デバッグのコツ

1. **ブラウザ開発者ツール**: コンソールログで設定読み込み状況確認
2. **ネットワークタブ**: API通信の成功/失敗確認  
3. **MySQL接続**: MySQL Workbench等でDB状態確認 (localhost:3306/tablecraft)
4. **ログ出力**: Spring Boot のログでSQLエラー確認

このリファレンスで効率的に設定できます！