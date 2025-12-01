# TableCraft Frontend (業務画面)

TableCraftの業務画面フロントエンドです。動的に生成されたCRUD画面でデータを管理します。

## 🏗️ 技術スタック

- **React**: 18+
- **TypeScript**: 5+
- **Vite**: 5+
- **React Router**: 6+

## 📁 主要コンポーネント

```
src/
├── App.tsx                    # メインアプリ
├── components/
│   ├── Layout/                # レイアウトコンポーネント
│   │   ├── Header.tsx         # ヘッダー
│   │   └── Sidebar.tsx        # サイドバー（テーブル一覧）
│   ├── Forms/                 # フォームコンポーネント
│   │   └── DynamicForm.tsx    # 動的フォーム
│   └── Tables/                # テーブルコンポーネント
│       └── TableList.tsx      # データ一覧表示
└── hooks/
    └── useTable.ts            # テーブル操作カスタムフック
```

## 🚀 開発

### ローカル起動
```bash
npm install
npm run dev
```

アクセス: http://localhost:5173

### ビルド
```bash
npm run build
```

## 🎯 主な機能

- **動的テーブル一覧**: `table-config.json`から自動生成
- **データCRUD操作**: 作成・読取・更新・削除
- **検索・ソート**: カラムごとの検索・ソート機能
- **多言語対応**: 日本語/英語切り替え
- **レスポンシブ対応**: モバイル・タブレット対応

## 📝 設定ファイル

### table-config.json
管理画面で作成したテーブル定義が自動反映されます。

```json
{
  "tables": {
    "temp1": {
      "id": "temp1",
      "name": "TEMP1",
      "label": {"ja": "基本テーブル１", "en": "Basic Table 1"},
      "columns": [...],
      "listColumns": [...],
      "enableSearch": true,
      "allowCreate": true
    }
  }
}
```

---
📖 システム全体の情報は[メインREADME](../README.md)を参照
