#!/usr/bin/env python3
"""
バックエンド設定ファイル生成機能
table-metadata.jsonからSpring Boot APIが参照するJSON設定ファイルを生成
"""

import json
from pathlib import Path
from typing import Dict, Any, List
from datetime import datetime


class BackendConfigGenerator:
    """バックエンド用JSON設定ファイル生成クラス"""
    
    def __init__(self):
        pass
    
    def generate(self, metadata: Dict[str, Any], output_path: Path) -> Dict[str, str]:
        """
        メタデータからバックエンド設定ファイルを生成
        
        Args:
            metadata: パース済みメタデータ
            output_path: 出力ディレクトリパス
            
        Returns:
            生成されたファイルの情報
        """
        results = {}
        
        # table-config.json生成
        table_config = self._generate_table_config(metadata)
        table_config_file = output_path / "table-config.json"
        with open(table_config_file, 'w', encoding='utf-8') as f:
            json.dump(table_config, f, ensure_ascii=False, indent=2)
        results['table_config'] = str(table_config_file)
        
        # ui-config.json生成
        ui_config = self._generate_ui_config(metadata)
        ui_config_file = output_path / "ui-config.json"
        with open(ui_config_file, 'w', encoding='utf-8') as f:
            json.dump(ui_config, f, ensure_ascii=False, indent=2)
        results['ui_config'] = str(ui_config_file)
        
        # validation-config.json生成
        validation_config = self._generate_validation_config(metadata)
        validation_config_file = output_path / "validation-config.json"
        with open(validation_config_file, 'w', encoding='utf-8') as f:
            json.dump(validation_config, f, ensure_ascii=False, indent=2)
        results['validation_config'] = str(validation_config_file)
        
        return results
    
    def _generate_table_config(self, metadata: Dict[str, Any]) -> Dict[str, Any]:
        """table-config.json形式の設定を生成"""
        project = metadata.get('project', {})
        tables = metadata.get('tables', {})
        
        table_config = {
            "version": project.get('version', '1.0.0'),
            "generated": datetime.now().isoformat(),
            "project": {
                "name": project.get('name', 'TableCraft Project'),
                "description": f"Auto-generated table configuration for {project.get('name', 'TableCraft Project')}"
            },
            "database": {
                "type": metadata.get('database', {}).get('type', 'mysql'),
                "charset": metadata.get('database', {}).get('charset', 'utf8mb4'),
                "collation": metadata.get('database', {}).get('collation', 'utf8mb4_unicode_ci')
            },
            "tables": {}
        }
        
        # テーブル定義を変換
        for table_name, table_def in tables.items():
            table_config["tables"][table_name] = self._convert_table_definition(table_name, table_def)
        
        return table_config
    
    def _convert_table_definition(self, table_name: str, table_def: Dict[str, Any]) -> Dict[str, Any]:
        """個別テーブル定義をバックエンド用フォーマットに変換"""
        metadata = table_def.get('metadata', {})
        columns = table_def.get('columns', {})
        primary_key = table_def.get('primaryKey', {})
        
        converted = {
            "name": table_name,
            "displayName": metadata.get('labels', {}).get('ja', table_name),
            "description": metadata.get('description', {}).get('ja', ''),
            "metadata": {
                "category": metadata.get('category', 'general'),
                "icon": metadata.get('icon', '📋'),
                "color": metadata.get('color', '#6B7280'),
                "sortOrder": metadata.get('sortOrder', 999),
                "isSystemTable": False
            },
            "primaryKey": self._convert_primary_key(primary_key, columns),
            "columns": [],
            "foreignKeys": []
        }
        
        # カラム定義を変換
        for col_name, col_def in columns.items():
            converted_column = self._convert_column_definition(col_name, col_def)
            converted["columns"].append(converted_column)
            
            # 外部キー情報を抽出
            if 'foreignKey' in col_def:
                fk_def = self._convert_foreign_key(col_name, col_def['foreignKey'])
                converted["foreignKeys"].append(fk_def)
        
        return converted
    
    def _convert_primary_key(self, primary_key: Dict[str, Any], columns: Dict[str, Any]) -> Dict[str, Any]:
        """プライマリキー定義を変換"""
        if primary_key.get('type') == 'composite':
            return {
                "type": "composite",
                "columns": primary_key.get('columns', [])
            }
        else:
            # 単一主キーの場合、auto_incrementカラムを探す
            primary_column = None
            for col_name, col_def in columns.items():
                constraints = col_def.get('constraints', {})
                if constraints.get('primaryKey', False):
                    primary_column = col_name
                    break
            
            return {
                "type": "single",
                "columns": [primary_column] if primary_column else ["id"]
            }
    
    def _convert_column_definition(self, col_name: str, col_def: Dict[str, Any]) -> Dict[str, Any]:
        """カラム定義を変換"""
        constraints = col_def.get('constraints', {})
        labels = col_def.get('labels', {})
        ui = col_def.get('ui', {})
        
        converted = {
            "name": col_name,
            "displayName": labels.get('ja', col_name),
            "type": self._convert_data_type(col_def),
            "nullable": constraints.get('nullable', True),
            "autoIncrement": constraints.get('autoIncrement', False),
            "comment": labels.get('en', '')
        }
        
        # サイズ・精度情報
        if 'length' in col_def:
            converted["size"] = col_def['length']
        if 'precision' in col_def:
            converted["size"] = col_def['precision']
        if 'scale' in col_def:
            converted["scale"] = col_def['scale']
        
        # デフォルト値
        if 'default' in constraints:
            converted["defaultValue"] = constraints['default']
        
        return converted
    
    def _convert_data_type(self, col_def: Dict[str, Any]) -> str:
        """データ型を変換"""
        data_type = col_def.get('type', 'VARCHAR').upper()
        
        # 長さ指定
        if 'length' in col_def:
            return f"{data_type}({col_def['length']})"
        
        # 精度・スケール指定
        if 'precision' in col_def:
            if 'scale' in col_def:
                return f"{data_type}({col_def['precision']},{col_def['scale']})"
            else:
                return f"{data_type}({col_def['precision']})"
        
        # デフォルトサイズ設定
        if data_type == 'VARCHAR' and 'length' not in col_def:
            return 'VARCHAR(255)'
        
        return data_type
    
    def _convert_foreign_key(self, col_name: str, fk_def: Dict[str, Any]) -> Dict[str, Any]:
        """外部キー定義を変換"""
        return {
            "name": f"fk_{col_name}",
            "column": col_name,
            "referencedTable": fk_def.get('table', ''),
            "referencedColumn": fk_def.get('column', 'id'),
            "onDelete": fk_def.get('onDelete', 'RESTRICT'),
            "onUpdate": fk_def.get('onUpdate', 'RESTRICT')
        }
    
    def _generate_ui_config(self, metadata: Dict[str, Any]) -> Dict[str, Any]:
        """ui-config.json形式の設定を生成"""
        project = metadata.get('project', {})
        tables = metadata.get('tables', {})
        
        ui_config = {
            "version": "1.0.0",
            "generated": datetime.now().isoformat(),
            "theme": {
                "primaryColor": "#3B82F6",
                "secondaryColor": "#6B7280",
                "accentColor": "#F59E0B"
            },
            "layout": {
                "sidebarWidth": 280,
                "headerHeight": 60,
                "footerHeight": 40
            },
            "tables": {}
        }
        
        # テーブルのUI設定を変換
        for table_name, table_def in tables.items():
            ui_config["tables"][table_name] = self._convert_table_ui_config(table_name, table_def)
        
        return ui_config
    
    def _convert_table_ui_config(self, table_name: str, table_def: Dict[str, Any]) -> Dict[str, Any]:
        """テーブルのUI設定を変換"""
        metadata = table_def.get('metadata', {})
        columns = table_def.get('columns', {})
        
        ui_config = {
            "icon": metadata.get('icon', '📋'),
            "color": metadata.get('color', '#6B7280'),
            "category": metadata.get('category', 'general'),
            "sortOrder": metadata.get('sortOrder', 999),
            "display": {
                "listView": {
                    "pageSize": 20,
                    "sortable": True,
                    "searchable": True
                },
                "formView": {
                    "layout": "vertical",
                    "columns": 2
                }
            },
            "columns": {}
        }
        
        # カラムのUI設定を変換
        for col_name, col_def in columns.items():
            ui = col_def.get('ui', {})
            ui_config["columns"][col_name] = {
                "hidden": ui.get('hidden', False),
                "readonly": ui.get('readonly', False),
                "required": not col_def.get('constraints', {}).get('nullable', True),
                "placeholder": ui.get('placeholder', ''),
                "helpText": ui.get('helpText', ''),
                "inputType": self._determine_input_type(col_def),
                "validation": self._extract_ui_validation(col_def)
            }
        
        return ui_config
    
    def _determine_input_type(self, col_def: Dict[str, Any]) -> str:
        """カラム定義からUI入力タイプを決定"""
        data_type = col_def.get('type', 'VARCHAR').upper()
        constraints = col_def.get('constraints', {})
        ui = col_def.get('ui', {})
        
        # UI定義で明示的に指定されている場合
        if 'inputType' in ui:
            return ui['inputType']
        
        # データ型に基づく推論
        if data_type == 'BOOLEAN':
            return 'checkbox'
        elif data_type in ['TEXT', 'LONGTEXT']:
            return 'textarea'
        elif data_type in ['DATETIME', 'TIMESTAMP']:
            return 'datetime'
        elif data_type == 'DATE':
            return 'date'
        elif data_type == 'TIME':
            return 'time'
        elif data_type in ['INT', 'BIGINT', 'SMALLINT']:
            return 'number'
        elif data_type.startswith('DECIMAL'):
            return 'number'
        elif 'email' in col_def.get('name', '').lower():
            return 'email'
        elif 'password' in col_def.get('name', '').lower():
            return 'password'
        elif 'url' in col_def.get('name', '').lower():
            return 'url'
        else:
            return 'text'
    
    def _extract_ui_validation(self, col_def: Dict[str, Any]) -> Dict[str, Any]:
        """UIバリデーション設定を抽出"""
        constraints = col_def.get('constraints', {})
        validation = col_def.get('validation', {})
        
        ui_validation = {}
        
        # 必須フィールド
        if not constraints.get('nullable', True):
            ui_validation['required'] = True
        
        # 長さ制限
        if 'length' in col_def:
            ui_validation['maxLength'] = col_def['length']
        
        # 最小値・最大値
        if 'min' in validation:
            ui_validation['min'] = validation['min']
        if 'max' in validation:
            ui_validation['max'] = validation['max']
        
        # パターン
        if 'pattern' in validation:
            ui_validation['pattern'] = validation['pattern']
        
        return ui_validation
    
    def _generate_validation_config(self, metadata: Dict[str, Any]) -> Dict[str, Any]:
        """validation-config.json形式の設定を生成"""
        project = metadata.get('project', {})
        tables = metadata.get('tables', {})
        
        validation_config = {
            "version": "1.0.0",
            "generated": datetime.now().isoformat(),
            "global": {
                "errorMessages": {
                    "required": "このフィールドは必須です",
                    "email": "有効なメールアドレスを入力してください",
                    "minLength": "最小文字数: {min}",
                    "maxLength": "最大文字数: {max}",
                    "min": "最小値: {min}",
                    "max": "最大値: {max}",
                    "pattern": "入力形式が正しくありません"
                }
            },
            "tables": {}
        }
        
        # テーブルのバリデーション設定を変換
        for table_name, table_def in tables.items():
            validation_config["tables"][table_name] = self._convert_table_validation_config(table_name, table_def)
        
        return validation_config
    
    def _convert_table_validation_config(self, table_name: str, table_def: Dict[str, Any]) -> Dict[str, Any]:
        """テーブルのバリデーション設定を変換"""
        columns = table_def.get('columns', {})
        
        validation_config = {
            "columns": {}
        }
        
        # カラムのバリデーション設定を変換
        for col_name, col_def in columns.items():
            constraints = col_def.get('constraints', {})
            validation = col_def.get('validation', {})
            
            col_validation = {}
            
            # 必須チェック
            if not constraints.get('nullable', True):
                col_validation['required'] = True
            
            # データ型バリデーション
            data_type = col_def.get('type', 'VARCHAR').upper()
            if data_type == 'EMAIL' or 'email' in col_name.lower():
                col_validation['email'] = True
            
            # 長さ制限
            if 'length' in col_def:
                col_validation['maxLength'] = col_def['length']
            
            # 数値範囲
            if data_type in ['INT', 'BIGINT', 'DECIMAL']:
                if 'min' in validation:
                    col_validation['min'] = validation['min']
                if 'max' in validation:
                    col_validation['max'] = validation['max']
            
            # パターンマッチング
            if 'pattern' in validation:
                col_validation['pattern'] = validation['pattern']
            
            # カスタムバリデーション
            if 'custom' in validation:
                col_validation['custom'] = validation['custom']
            
            if col_validation:
                validation_config["columns"][col_name] = col_validation
        
        return validation_config