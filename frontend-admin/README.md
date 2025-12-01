# TableCraft Frontend Admin (管理画面)

TableCraftの管理画面フロントエンドです。テーブル定義を作成・編集します。

## 🏗️ 技術スタック

- **React**: 18+
- **TypeScript**: 5+
- **Vite**: 5+

## 📁 主要コンポーネント

```
src/
├── App.tsx                    # メインアプリ
├── components/
│   ├── Layout/                # レイアウト
│   │   ├── Header.tsx         # ヘッダー（一括保存ボタン）
│   │   └── Sidebar.tsx        # サイドバー（テーブル一覧）
│   ├── TableEdit/             # テーブル編集
│   │   ├── TableEditPanel.tsx # テーブル設定パネル
│   │   └── FieldModal.tsx     # フィールド編集モーダル
│   ├── TableBuilder/          # テーブル作成
│   │   └── TemplateTableCreator.tsx
│   └── SqlUpload/             # SQLアップロード
└── api/
    └── adminApi.ts            # 管理APIクライアント
```

## 🚀 開発

### ローカル起動
```bash
npm install
npm run dev
```

アクセス: http://localhost:5174

### ビルド
```bash
npm run build
```

## 🎯 主な機能

- **テーブル作成**: テンプレートからまたは新規作成
- **カラム編集**: 追加・編集・削除
- **表示制御**: 一覧画面での表示/非表示設定
- **UI設定**: 検索、ソート、CRUD権限の設定
- **一括保存**: 全設定をまとめて保存
- **テーブル削除**: 定義と実テーブルを削除

## 🔗 API連携

### 管理APIエンドポイント
- `GET /api/admin/tables/list` - テーブル一覧
- `POST /api/admin/tables/create` - テーブル作成
- `PUT /api/admin/tables/update/{id}` - テーブル更新
- `POST /api/admin/tables/delete/{id}` - テーブル削除

---
📖 システム全体の情報は[メインREADME](../README.md)を参照
