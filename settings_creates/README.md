# メタデータ駆動CRUDシステム生成ツール

JSONメタデータから各種ファイルを自動生成するPythonツールです。

## プロジェクト構成

```
├── src/                    # ソースコード
│   ├── generator.py       # メイン生成ロジック
│   ├── metadata_parser.py # JSONパーサー
│   ├── sql_generator.py   # SQL生成
│   ├── properties_generator.py # Properties生成
│   └── frontend_generator.py   # フロントエンド設定生成
├── templates/              # Jinja2テンプレート
│   ├── sql/
│   ├── properties/
│   └── frontend/
├── examples/              # サンプル設定
│   └── table-metadata.json
├── output/               # 生成ファイル出力先
└── requirements.txt      # 依存関係
```

## 機能

### 生成対象ファイル
1. **SQL**: CREATE TABLE文、INDEX、CONSTRAINT
2. **Properties**: 多言語ラベル、UIメッセージ  
3. **フロントエンド設定**: UIコンポーネント、バリデーション
4. **API設定**: エンドポイント、デフォルト値

### 対応機能
- ✅ 複合主キーサポート
- ✅ 多言語対応（日本語/英語）
- ✅ UIカスタマイズ（アイコン、色、入力タイプ）
- ✅ バリデーションルール
- ✅ 外部キーリレーション

## インストール

```bash
pip install -r requirements.txt
```

## 使用方法

```bash
# 基本的な使用方法
python src/generator.py examples/table-metadata.json

# 出力ディレクトリ指定
python src/generator.py examples/table-metadata.json --output ./custom-output

# 特定のファイルタイプのみ生成
python src/generator.py examples/table-metadata.json --types sql,properties
```

## メタデータJSONフォーマット

詳細は `examples/table-metadata.json` を参照してください。

## テンプレートカスタマイズ

`templates/` ディレクトリ内のJinja2テンプレートを編集することで、生成されるファイルをカスタマイズできます。