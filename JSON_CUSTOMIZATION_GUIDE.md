# JSONファイルカスタマイズガイド

## 概要
自動生成されたJSONファイルをカスタマイズして、独自のテーブルやフィールドを追加する方法を説明します。

## カスタマイズの基本方針

1. **既存ファイルの構造を理解** - 生成されたJSONの構造を把握
2. **段階的な追加** - 一つずつ新しい要素を追加
3. **テスト駆動** - 変更後は必ず動作確認

---

## 新しいテーブルの追加

### 手順1: SQLファイルにテーブル定義を追加

```sql
-- 新しいテーブルをtable-definitions.sqlに追加
CREATE TABLE IF NOT EXISTS employees (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_number VARCHAR(20) UNIQUE NOT NULL,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  department_id BIGINT,
  hire_date DATE NOT NULL,
  salary DECIMAL(10,2),
  is_active BOOLEAN DEFAULT true,
  created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (department_id) REFERENCES departments(id)
);
```

### 手順2: table-config.jsonに設定追加

既存のテーブル設定を参考に、新しいテーブル設定を追加：

```json
{
  "tables": {
    "既存のテーブル": { ... },
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
        },
        "description": {
          "ja": "従業員情報の管理",
          "en": "Employee information management"
        }
      },
      "primaryKey": {
        "type": "single",
        "column": "id"
      },
      "columns": [
        {
          "name": "id",
          "type": "BIGINT",
          "constraints": {
            "primaryKey": true,
            "autoIncrement": true,
            "nullable": false
          },
          "labels": {
            "ja": "従業員ID",
            "en": "Employee ID"
          },
          "ui": {
            "hidden": true,
            "readonly": true,
            "inputType": "number"
          }
        },
        {
          "name": "employee_number",
          "type": "VARCHAR",
          "constraints": {
            "nullable": false,
            "unique": true
          },
          "labels": {
            "ja": "社員番号",
            "en": "Employee Number"
          },
          "ui": {
            "inputType": "text",
            "placeholder": {
              "ja": "社員番号を入力",
              "en": "Enter employee number"
            }
          },
          "validation": {
            "required": true,
            "maxLength": 20
          }
        }
      ],
      "formFields": [
        {
          "name": "employee_number",
          "type": "text",
          "label": {
            "ja": "社員番号",
            "en": "Employee Number"
          },
          "placeholder": {
            "ja": "社員番号を入力",
            "en": "Enter employee number"
          },
          "required": true,
          "readonly": false,
          "disabled": false,
          "maxLength": 20
        },
        {
          "name": "first_name",
          "type": "text",
          "label": {
            "ja": "名前",
            "en": "First Name"
          },
          "required": true,
          "maxLength": 50
        },
        {
          "name": "department_id",
          "type": "select",
          "label": {
            "ja": "部署",
            "en": "Department"
          },
          "required": false,
          "options": {
            "type": "foreign_key",
            "table": "departments",
            "valueColumn": "id",
            "displayColumn": "name",
            "allowNull": true,
            "nullLabel": {
              "ja": "未配属",
              "en": "Unassigned"
            }
          }
        }
      ],
      "listColumns": [
        {
          "name": "employee_number",
          "label": {
            "ja": "社員番号",
            "en": "Employee Number"
          },
          "type": "VARCHAR",
          "sortable": true,
          "searchable": true,
          "width": "120px",
          "align": "left"
        },
        {
          "name": "first_name",
          "label": {
            "ja": "名前",
            "en": "First Name"
          },
          "type": "VARCHAR",
          "sortable": true,
          "searchable": true,
          "width": "auto",
          "align": "left"
        },
        {
          "name": "department_id",
          "label": {
            "ja": "部署",
            "en": "Department"
          },
          "type": "BIGINT",
          "sortable": true,
          "searchable": true,
          "width": "auto",
          "align": "left",
          "foreignKey": {
            "table": "departments",
            "displayColumn": "name"
          }
        }
      ]
    }
  }
}
```

### 手順3: validation-config.jsonに追加

```json
{
  "tables": {
    "既存のテーブル": { ... },
    "employees": {
      "fields": {
        "employee_number": {
          "rules": [
            {
              "type": "required",
              "message": "validation.required"
            },
            {
              "type": "maxLength",
              "value": 20,
              "message": "validation.maxLength"
            }
          ]
        },
        "first_name": {
          "rules": [
            {
              "type": "required",
              "message": "validation.required"
            },
            {
              "type": "maxLength",
              "value": 50,
              "message": "validation.maxLength"
            }
          ]
        },
        "salary": {
          "rules": [
            {
              "type": "min",
              "value": 0,
              "message": "validation.min"
            }
          ]
        }
      }
    }
  }
}
```

---

## フィールドタイプ別設定例

### テキストフィールド

```json
{
  "name": "title",
  "type": "text",
  "label": {"ja": "タイトル", "en": "Title"},
  "placeholder": {"ja": "タイトルを入力", "en": "Enter title"},
  "required": true,
  "maxLength": 100
}
```

### メールアドレス

```json
{
  "name": "email",
  "type": "email",
  "label": {"ja": "メールアドレス", "en": "Email"},
  "placeholder": {"ja": "email@example.com", "en": "email@example.com"},
  "required": true
}
```

### 数値（価格など）

```json
{
  "name": "price",
  "type": "number",
  "label": {"ja": "価格", "en": "Price"},
  "required": true,
  "min": 0,
  "step": "0.01"
}
```

### 日付

```json
{
  "name": "start_date",
  "type": "date",
  "label": {"ja": "開始日", "en": "Start Date"},
  "required": true
}
```

### チェックボックス

```json
{
  "name": "is_active",
  "type": "checkbox",
  "label": {"ja": "有効", "en": "Active"},
  "defaultValue": "true"
}
```

### 複数行テキスト

```json
{
  "name": "description",
  "type": "textarea",
  "label": {"ja": "説明", "en": "Description"},
  "rows": 4,
  "maxLength": 1000
}
```

### 外部キー選択

```json
{
  "name": "category_id",
  "type": "select",
  "label": {"ja": "カテゴリー", "en": "Category"},
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

---

## 一覧表示のカスタマイズ

### 通常の列表示

```json
{
  "name": "title",
  "label": {"ja": "タイトル", "en": "Title"},
  "type": "VARCHAR",
  "sortable": true,
  "searchable": true,
  "width": "200px",  // 固定幅 or "auto"
  "align": "left"    // left, center, right
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
  "foreignKey": {
    "table": "categories",
    "displayColumn": "name"
  }
}
```

### 数値のフォーマット表示

```json
{
  "name": "price",
  "label": {"ja": "価格", "en": "Price"},
  "type": "DECIMAL",
  "sortable": true,
  "align": "right",
  "format": "decimal",
  "decimalPlaces": 2
}
```

### ブール値の表示

```json
{
  "name": "is_active",
  "label": {"ja": "有効", "en": "Active"},
  "type": "BOOLEAN",
  "sortable": true,
  "align": "center",
  "format": "boolean",
  "width": "80px"
}
```

---

## バリデーション設定

### 必須チェック

```json
{
  "type": "required",
  "message": "validation.required"
}
```

### 文字数制限

```json
{
  "type": "minLength",
  "value": 2,
  "message": "validation.minLength"
},
{
  "type": "maxLength",
  "value": 100,
  "message": "validation.maxLength"
}
```

### 数値範囲

```json
{
  "type": "min",
  "value": 0,
  "message": "validation.min"
},
{
  "type": "max",
  "value": 999999,
  "message": "validation.max"
}
```

### パターンマッチ

```json
{
  "type": "pattern",
  "value": "email",  // or 正規表現文字列
  "message": "validation.email"
}
```

---

## UI制御オプション

### フィールドの表示・非表示

```json
{
  "ui": {
    "hidden": true      // フォームで非表示
  }
}
```

### 読み取り専用

```json
{
  "ui": {
    "readonly": true    // 編集不可
  }
}
```

### 条件付き無効化

```json
{
  "disabled": true      // 入力無効
}
```

---

## 複合主キーの設定

```json
{
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
}
```

---

## メッセージのカスタマイズ

### messages_ja.properties

```properties
# バリデーションメッセージ
validation.required=この項目は必須です
validation.minLength=最低{0}文字入力してください
validation.maxLength=最大{0}文字まで入力可能です
validation.min=最小値は{0}です
validation.max=最大値は{0}です
validation.email=正しいメールアドレスを入力してください

# カスタムメッセージ
employee.validation.employeeNumber=社員番号は必須です
department.validation.name=部署名は2文字以上入力してください
```

### messages_en.properties

```properties
validation.required=This field is required
validation.minLength=Minimum {0} characters required
validation.maxLength=Maximum {0} characters allowed
validation.min=Minimum value is {0}
validation.max=Maximum value is {0}
validation.email=Please enter a valid email address

employee.validation.employeeNumber=Employee number is required
department.validation.name=Department name must be at least 2 characters
```

---

## デバッグとトラブルシューティング

### よくある問題

1. **フィールドが表示されない**
   - `formFields` に定義されているか確認
   - `ui.hidden: true` が設定されていないか確認

2. **バリデーションエラーが出る**
   - `validation-config.json` の設定確認
   - メッセージキーが存在するか確認

3. **外部キーが表示されない**
   - 参照先テーブルにデータが存在するか確認
   - `displayColumn` の設定確認

### デバッグ方法

1. **ブラウザコンソール**: 設定読み込み状況を確認
2. **ネットワークタブ**: API通信の成功/失敗を確認
3. **MySQL接続**: MySQL Workbench等でDB確認 (localhost:3306/tablecraft)

---

## テスト手順

新しい設定を追加した後は以下を確認：

1. ✅ **システム再起動**: Spring Boot + フロントエンド
2. ✅ **新規作成**: フォームが正しく表示・保存される
3. ✅ **編集**: 既存データの編集ができる
4. ✅ **一覧表示**: データが正しく表示される
5. ✅ **削除**: 削除操作が正常に動作する
6. ✅ **バリデーション**: エラーメッセージが正しく表示される

これらの手順に従って、システムを自由にカスタマイズできます！