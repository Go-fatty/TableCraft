#!/usr/bin/env python3
"""
メタデータ駆動CRUDシステム生成ツール
JSONメタデータから各種ファイルを自動生成するメインスクリプト
"""

import argparse
import json
import sys
from pathlib import Path
from typing import Dict, List, Any

from metadata_parser import MetadataParser
from sql_generator import SQLGenerator
from properties_generator import PropertiesGenerator
from frontend_generator import FrontendGenerator
from backend_config_generator import BackendConfigGenerator


class CRUDSystemGenerator:
    """CRUDシステム生成のメインクラス"""
    
    def __init__(self, output_dir: Path = None):
        self.output_dir = output_dir or Path("./output")
        self.output_dir.mkdir(exist_ok=True)
        
        self.generators = {
            'sql': SQLGenerator(),
            'properties': PropertiesGenerator(), 
            'frontend': FrontendGenerator(),
            'backend': BackendConfigGenerator()
        }
    
    def generate_all(self, metadata_file: Path, file_types: List[str] = None) -> Dict[str, Any]:
        """
        メタデータからすべてのファイルを生成
        
        Args:
            metadata_file: メタデータJSONファイルパス
            file_types: 生成するファイルタイプ（デフォルト: 全て）
            
        Returns:
            生成結果の辞書
        """
        # メタデータ読み込み・パース
        parser = MetadataParser()
        metadata = parser.parse_file(metadata_file)
        
        # 生成するファイルタイプを決定
        if file_types is None:
            file_types = list(self.generators.keys())
        
        results = {}
        
        for file_type in file_types:
            if file_type not in self.generators:
                print(f"警告: 未対応のファイルタイプ '{file_type}' をスキップします")
                continue
                
            try:
                generator = self.generators[file_type]
                output_path = self.output_dir / file_type
                output_path.mkdir(exist_ok=True)
                
                result = generator.generate(metadata, output_path)
                results[file_type] = result
                
                print(f"✅ {file_type} ファイル生成完了: {output_path}")
                
            except Exception as e:
                print(f"❌ {file_type} ファイル生成エラー: {e}")
                results[file_type] = {"error": str(e)}
        
        return results
    
    def show_metadata_summary(self, metadata_file: Path):
        """メタデータの概要を表示"""
        parser = MetadataParser()
        metadata = parser.parse_file(metadata_file)
        
        print(f"\n📋 メタデータ概要: {metadata_file.name}")
        print(f"プロジェクト名: {metadata.get('project', {}).get('name', 'N/A')}")
        print(f"バージョン: {metadata.get('project', {}).get('version', 'N/A')}")
        print(f"サポート言語: {', '.join(metadata.get('project', {}).get('supportedLanguages', []))}")
        
        tables = metadata.get('tables', {})
        print(f"\nテーブル数: {len(tables)}")
        for table_name, table_def in tables.items():
            columns = table_def.get('columns', {})
            icon = table_def.get('metadata', {}).get('icon', '🗄️')
            label = table_def.get('metadata', {}).get('labels', {}).get('ja', table_name)
            print(f"  {icon} {label} ({table_name}): {len(columns)} カラム")


def main():
    """メイン関数"""
    parser = argparse.ArgumentParser(
        description="メタデータ駆動CRUDシステム生成ツール",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
使用例:
  python generator.py examples/table-metadata.json
  python generator.py examples/table-metadata.json --output ./custom-output
  python generator.py examples/table-metadata.json --types sql,properties
  python generator.py examples/table-metadata.json --summary
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
        default=Path('./output'),
        help='出力ディレクトリ (デフォルト: ./output)'
    )
    
    parser.add_argument(
        '--types', '-t',
        type=str,
        help='生成するファイルタイプ（カンマ区切り: sql,properties,frontend,backend）'
    )
    
    parser.add_argument(
        '--summary', '-s',
        action='store_true',
        help='メタデータの概要のみ表示'
    )
    
    args = parser.parse_args()
    
    # メタデータファイル存在チェック
    if not args.metadata_file.exists():
        print(f"❌ エラー: メタデータファイルが見つかりません: {args.metadata_file}")
        sys.exit(1)
    
    try:
        generator = CRUDSystemGenerator(args.output)
        
        # 概要表示のみの場合
        if args.summary:
            generator.show_metadata_summary(args.metadata_file)
            return
        
        # ファイル生成実行
        file_types = None
        if args.types:
            file_types = [t.strip() for t in args.types.split(',')]
        
        print(f"🚀 ファイル生成開始...")
        print(f"📁 メタデータ: {args.metadata_file}")
        print(f"📁 出力先: {args.output}")
        
        results = generator.generate_all(args.metadata_file, file_types)
        
        # 結果サマリー
        print(f"\n📊 生成結果:")
        success_count = sum(1 for r in results.values() if 'error' not in r)
        error_count = len(results) - success_count
        
        print(f"✅ 成功: {success_count} 件")
        if error_count > 0:
            print(f"❌ エラー: {error_count} 件")
        
        print(f"\n🎉 生成完了! 出力ディレクトリ: {args.output}")
        
    except Exception as e:
        print(f"❌ 予期しないエラー: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()