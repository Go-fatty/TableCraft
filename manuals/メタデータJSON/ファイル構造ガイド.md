# メタデータJSONファイル構造ガイド

## 概要
このシステムでは、SQLファイルから自動生成されたJSONファイルを基に、動的なCRUDシステムを構築しています。生成されたJSONファイルを理解し、必要に応じてカスタマイズすることで、システムの動作を制御できます。

## 自動生成される設定ファイル

### 1. table-config.json
テーブルの基本設定、フォームフィールド、一覧表示カラムを定義

### 2. validation-config.json
各フィールドのバリデーションルールを定義

### 3. ui-config.json
UI表示設定（今後の拡張用）

### 4. types.ts
TypeScript型定義（フロントエンド用）

---

## 生成されたJSONファイルの理解

自動生成されたJSONファイルの各項目の意味と、カスタマイズ可能な箇所を説明します。

## table-config.json 詳細解説

### 基本構造
```json
{
  "version": "1.0.0",
  "generated": "2025-11-18 13:10:16",
  "project": {
    "name": "プロジェクト名",
    "version": "1.0.0",
    "defaultLanguage": "ja",
    "supportedLanguages": ["ja", "en"]
  },
  "tables": {
    "テーブル名": { ... }
  }
}
```

### テーブル設定詳細

#### メタデータ設定
```json
"metadata": {
  "icon": "👤",           // サイドバーアイコン
  "color": "#4A90E2",     // テーマカラー
  "sortOrder": 1,         // 表示順序
  "category": "user_management",  // カテゴリ
  "labels": {
    "ja": "ユーザー",     // 日本語ラベル
    "en": "Users"         // 英語ラベル
  },
  "description": {
    "ja": "システム利用者の管理",
    "en": "System user management"
  }
}
```

#### プライマリキー設定
```json
"primaryKey": {
  "type": "single",       // single | composite
  "column": "id"          // 単一PKの場合
},
// 複合PKの場合
"primaryKey": {
  "type": "composite",
  "columns": ["order_id", "product_id"]
}
```

#### カラム定義
```json
"columns": [
  {
    "name": "id",                    // カラム名
    "type": "BIGINT",               // データ型
    "constraints": {
      "primaryKey": true,           // プライマリキー
      "autoIncrement": true,        // 自動採番
      "nullable": false,            // NULL許可
      "unique": false,              // ユニーク制約
      "default": "CURRENT_TIMESTAMP"  // デフォルト値
    },
    "labels": {
      "ja": "ユーザーID",
      "en": "User ID"
    },
    "ui": {
      "hidden": true,               // フォームで非表示
      "readonly": true,             // 読み取り専用
      "inputType": "number"         // 入力タイプ
    },
    "validation": {
      "required": true              // 必須チェック
    },
    "foreignKey": {                 // 外部キー設定
      "table": "categories",        // 参照テーブル
      "column": "id",              // 参照カラム
      "displayColumn": "name",     // 表示用カラム
      "onDelete": "CASCADE",       // 削除時動作
      "onUpdate": "CASCADE"        // 更新時動作
    }
  }
]
```

#### フォームフィールド設定
```json
"formFields": [
  {
    "name": "name",                 // フィールド名
    "type": "text",                 // 入力タイプ
    "label": {
      "ja": "氏名",
      "en": "Name"
    },
    "placeholder": {
      "ja": "氏名を入力してください",
      "en": "Enter your name"
    },
    "required": true,               // 必須フィールド
    "readonly": false,              // 読み取り専用
    "disabled": false,              // 無効化
    "maxLength": 100,               // 最大文字数
    "min": 0,                       // 最小値（数値）
    "max": 150,                     // 最大値（数値）
    "step": "0.01",                 // ステップ（数値）
    "rows": 3,                      // 行数（textarea）
    "validation": {
      "required": true,
      "minLength": 2,
      "maxLength": 100,
      "pattern": "^[\\p{L}\\s]+$"
    },
    "options": {                    // select用オプション
      "type": "foreign_key",        // foreign_key | static
      "table": "categories",        // 参照テーブル
      "valueColumn": "id",          // 値カラム
      "displayColumn": "name",      // 表示カラム
      "allowNull": true,            // NULL許可
      "nullLabel": {
        "ja": "カテゴリーなし",
        "en": "No Category"
      }
    }
  }
]
```

#### 一覧表示カラム設定
```json
"listColumns": [
  {
    "name": "name",                 // カラム名
    "label": {
      "ja": "氏名",
      "en": "Name"
    },
    "type": "VARCHAR",              // データ型
    "sortable": true,               // ソート可能
    "searchable": true,             // 検索対象
    "width": "auto",                // 幅設定
    "align": "left",                // 配置（left|center|right）
    "format": "decimal",            // フォーマット（decimal|boolean）
    "decimalPlaces": 2,             // 小数点桁数
    "foreignKey": {                 // 外部キー表示
      "table": "categories",
      "displayColumn": "name"
    }
  }
]
```

---

## validation-config.json 詳細解説

### 基本構造
```json
{
  "version": "1.0.0",
  "generated": "2025-11-18 13:10:16",
  "tables": {
    "テーブル名": {
      "fields": {
        "フィールド名": {
          "rules": [
            {
              "type": "required",
              "message": "validation.required"
            }
          ]
        }
      }
    }
  }
}
```

### バリデーションルール種類
```json
{
  "type": "required",               // 必須チェック
  "message": "validation.required"
},
{
  "type": "minLength",             // 最小文字数
  "value": 2,
  "message": "validation.minLength"
},
{
  "type": "maxLength",             // 最大文字数
  "value": 100,
  "message": "validation.maxLength"
},
{
  "type": "min",                   // 最小値
  "value": 0,
  "message": "validation.min"
},
{
  "type": "max",                   // 最大値
  "value": 150,
  "message": "validation.max"
},
{
  "type": "pattern",               // パターンマッチ
  "value": "email",                // email | 正規表現
  "message": "validation.email"
}
```

---

## メッセージファイル（messages.properties）

### 日本語（messages_ja.properties）
```properties
validation.required=この項目は必須です
validation.minLength=最低{0}文字入力してください
validation.maxLength=最大{0}文字まで入力可能です
validation.min=最小値は{0}です
validation.max=最大値は{0}です
validation.email=正しいメールアドレスを入力してください
```

### 英語（messages_en.properties）
```properties
validation.required=This field is required
validation.minLength=Minimum {0} characters required
validation.maxLength=Maximum {0} characters allowed
validation.min=Minimum value is {0}
validation.max=Maximum value is {0}
validation.email=Please enter a valid email address
```

---

## 入力タイプ一覧

| type | 説明 | 追加属性 |
|------|------|----------|
| text | テキスト入力 | maxLength, placeholder |
| email | メールアドレス | placeholder |
| tel | 電話番号 | placeholder |
| textarea | 複数行テキスト | rows, placeholder |
| number | 数値入力 | min, max, step |
| date | 日付選択 | - |
| datetime-local | 日時選択 | - |
| checkbox | チェックボックス | defaultValue |
| select | プルダウン選択 | options |

---

## 外部キー設定例

### 商品 → カテゴリー参照
```json
{
  "name": "category_id",
  "type": "select",
  "label": {
    "ja": "カテゴリー",
    "en": "Category"
  },
  "required": false,
  "options": {
    "type": "foreign_key",
    "table": "categories",          // 参照先テーブル
    "valueColumn": "id",            // 保存する値（ID）
    "displayColumn": "name",        // 表示する値（名前）
    "allowNull": true,
    "nullLabel": {
      "ja": "カテゴリーなし",
      "en": "No Category"
    }
  }
}
```

### 一覧表示での外部キー解決
```json
{
  "name": "category_id",
  "label": {
    "ja": "カテゴリー",
    "en": "Category"
  },
  "type": "BIGINT",
  "sortable": true,
  "searchable": true,
  "width": "auto",
  "align": "left",
  "foreignKey": {
    "table": "categories",          // 参照先テーブル
    "displayColumn": "name"         // 表示用カラム
  }
}
```

---

## 実践的な追加例

### 新しいテーブル追加手順

1. **SQLファイル更新**
   ```sql
   CREATE TABLE IF NOT EXISTS employees (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     employee_number VARCHAR(20) UNIQUE NOT NULL,
     first_name VARCHAR(50) NOT NULL,
     last_name VARCHAR(50) NOT NULL,
     department_id BIGINT,
     hire_date DATE NOT NULL,
     salary DECIMAL(10,2),
     is_active BOOLEAN DEFAULT true,
     FOREIGN KEY (department_id) REFERENCES departments(id)
   );
   ```

2. **table-config.json に追加**
   ```json
   "employees": {
     "name": "employees",
     "metadata": {
       "icon": "👥",
       "color": "#2ECC71",
       "sortOrder": 5,
       "category": "human_resources",
       "labels": {
         "ja": "従業員",
         "en": "Employees"
       }
     },
     "formFields": [
       {
         "name": "employee_number",
         "type": "text",
         "label": {"ja": "社員番号", "en": "Employee Number"},
         "required": true,
         "maxLength": 20
       },
       {
         "name": "department_id",
         "type": "select",
         "label": {"ja": "部署", "en": "Department"},
         "options": {
           "type": "foreign_key",
           "table": "departments",
           "valueColumn": "id",
           "displayColumn": "name",
           "allowNull": true
         }
       }
     ],
     "listColumns": [
       {
         "name": "employee_number",
         "label": {"ja": "社員番号", "en": "Employee Number"},
         "type": "VARCHAR",
         "sortable": true,
         "searchable": true
       },
       {
         "name": "department_id",
         "label": {"ja": "部署", "en": "Department"},
         "type": "BIGINT",
         "foreignKey": {
           "table": "departments",
           "displayColumn": "name"
         }
       }
     ]
   }
   ```

3. **validation-config.json に追加**
   ```json
   "employees": {
     "fields": {
       "employee_number": {
         "rules": [
           {"type": "required", "message": "validation.required"},
           {"type": "maxLength", "value": 20, "message": "validation.maxLength"}
         ]
       }
     }
   }
   ```

これでシステムを再起動すると、新しい従業員テーブルが自動的にCRUD操作可能になります！

## カスタマイズについて

より詳細なカスタマイズ方法については、`JSON_CUSTOMIZATION_GUIDE.md` を参照してください。

---

## トラブルシューティング

### よくある問題と解決方法

1. **フィールドが表示されない**
   - `ui.hidden: true` がないか確認
   - `formFields` に定義されているか確認

2. **バリデーションエラーが出る**
   - `validation-config.json` の設定確認
   - メッセージファイルにキーが存在するか確認

3. **外部キーが表示されない**
   - 参照先テーブルにデータが存在するか確認
   - `displayColumn` の設定確認
   - 大文字小文字の問題確認

4. **更新時エラー**
   - 重複カラム名がないか確認
   - プライマリキーの設定確認

このガイドを参考に、自由に新しいテーブルやフィールドを追加できます！