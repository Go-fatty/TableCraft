#!/usr/bin/env python3
"""
バックエンド設定ファイル生成専用スクリプト
table-metadata.jsonからSpring Boot用のJSON設定ファイルを一括生成
"""

import argparse
import sys
from pathlib import Path

from metadata_parser import MetadataParser
from backend_config_generator import BackendConfigGenerator


def main():
    """メイン関数"""
    parser = argparse.ArgumentParser(
        description="TableCraft バックエンド設定ファイル生成ツール",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
使用例:
  python generate_backend_configs.py examples/table-metadata.json
  python generate_backend_configs.py examples/table-metadata.json --output ../backend/src/main/resources/config
        """
    )
    
    parser.add_argument(
        'metadata_file',
        type=Path,
        help='メタデータJSONファイルのパス'
    )
    
    parser.add_argument(
        '--output', '-o',
        type=Path,
        default=Path('./output/backend'),
        help='出力ディレクトリ (デフォルト: ./output/backend)'
    )
    
    args = parser.parse_args()
    
    # メタデータファイル存在チェック
    if not args.metadata_file.exists():
        print(f"❌ エラー: メタデータファイルが見つかりません: {args.metadata_file}")
        sys.exit(1)
    
    try:
        print(f"🚀 バックエンド設定ファイル生成開始...")
        print(f"📁 メタデータ: {args.metadata_file}")
        print(f"📁 出力先: {args.output}")
        
        # メタデータ読み込み・パース
        parser = MetadataParser()
        metadata = parser.parse_file(args.metadata_file)
        
        # 出力ディレクトリ作成
        args.output.mkdir(parents=True, exist_ok=True)
        
        # バックエンド設定ファイル生成
        generator = BackendConfigGenerator()
        results = generator.generate(metadata, args.output)
        
        # 結果表示
        print(f"\n📊 生成結果:")
        for file_type, file_path in results.items():
            print(f"✅ {file_type}: {file_path}")
        
        # プロジェクト情報表示
        project = metadata.get('project', {})
        tables = metadata.get('tables', {})
        print(f"\n📋 プロジェクト情報:")
        print(f"  プロジェクト名: {project.get('name', 'N/A')}")
        print(f"  バージョン: {project.get('version', 'N/A')}")
        print(f"  テーブル数: {len(tables)}")
        
        table_names = list(tables.keys())
        if table_names:
            print(f"  テーブル: {', '.join(table_names)}")
        
        print(f"\n🎉 生成完了! 出力ディレクトリ: {args.output}")
        print(f"\n💡 次のステップ:")
        print(f"   1. 生成されたファイルを確認してください")
        print(f"   2. 必要に応じてバックエンドのresources/configディレクトリにコピーしてください")
        print(f"   3. Spring Bootアプリケーションを再起動してください")
        
    except Exception as e:
        print(f"❌ 予期しないエラー: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()