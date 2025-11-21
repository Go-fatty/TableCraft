#!/usr/bin/env python3
"""
メタデータJSONファイルのパースと検証
"""

import json
import jsonschema
from pathlib import Path
from typing import Dict, Any, List


class MetadataParser:
    """メタデータJSONの読み込み・検証・パースを担当"""
    
    def __init__(self):
        self.schema = self._get_metadata_schema()
    
    def parse_file(self, file_path: Path) -> Dict[str, Any]:
        """
        JSONファイルを読み込み・検証・パースする
        
        Args:
            file_path: メタデータJSONファイルのパス
            
        Returns:
            パース済みメタデータ辞書
            
        Raises:
            FileNotFoundError: ファイルが存在しない場合
            json.JSONDecodeError: JSON構文エラーの場合
            jsonschema.ValidationError: スキーマ検証エラーの場合
        """
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # スキーマ検証
        jsonschema.validate(data, self.schema)
        
        # 補完処理
        return self._enrich_metadata(data)
    
    def _enrich_metadata(self, metadata: Dict[str, Any]) -> Dict[str, Any]:
        """
        メタデータを補完・正規化する
        
        Args:
            metadata: 元のメタデータ
            
        Returns:
            補完されたメタデータ
        """
        enriched = metadata.copy()
        
        # プロジェクト情報の補完
        if 'project' not in enriched:
            enriched['project'] = {}
        
        project = enriched['project']
        project.setdefault('name', 'Generated CRUD System')
        project.setdefault('version', '1.0.0')
        project.setdefault('defaultLanguage', 'ja')
        project.setdefault('supportedLanguages', ['ja', 'en'])
        
        # データベース情報の補完
        if 'database' not in enriched:
            enriched['database'] = {}
        
        database = enriched['database']
        database.setdefault('type', 'mysql')
        database.setdefault('dialect', 'org.hibernate.dialect.MySQL8Dialect')
        database.setdefault('defaultTimezone', 'Asia/Tokyo')
        
        # テーブル情報の補完
        tables = enriched.get('tables', {})
        for table_name, table_def in tables.items():
            self._enrich_table_metadata(table_name, table_def, project['defaultLanguage'])
        
        return enriched
    
    def _enrich_table_metadata(self, table_name: str, table_def: Dict[str, Any], default_lang: str):
        """テーブル定義の補完"""
        # メタデータ補完
        metadata = table_def.setdefault('metadata', {})
        metadata.setdefault('icon', '🗄️')
        metadata.setdefault('color', '#4A90E2')
        metadata.setdefault('sortOrder', 0)
        metadata.setdefault('category', 'general')
        
        # ラベル補完
        labels = metadata.setdefault('labels', {})
        if default_lang not in labels:
            labels[default_lang] = table_name.replace('_', ' ').title()
        
        # 主キー解析
        primary_keys = []
        columns = table_def.get('columns', {})
        
        for col_name, col_def in columns.items():
            constraints = col_def.get('constraints', {})
            
            # カラム定義補完
            self._enrich_column_metadata(col_name, col_def, default_lang)
            
            # 主キー収集
            if constraints.get('primaryKey', False):
                primary_keys.append(col_name)
        
        # 主キー情報設定
        if primary_keys:
            pk_info = table_def.setdefault('primaryKey', {})
            if len(primary_keys) == 1:
                pk_info['type'] = 'single'
                pk_info['column'] = primary_keys[0]
            else:
                pk_info['type'] = 'composite'
                pk_info['columns'] = primary_keys
    
    def _enrich_column_metadata(self, col_name: str, col_def: Dict[str, Any], default_lang: str):
        """カラム定義の補完"""
        # 制約情報補完
        constraints = col_def.setdefault('constraints', {})
        constraints.setdefault('nullable', True)
        
        # ラベル補完
        labels = col_def.setdefault('labels', {})
        if default_lang not in labels:
            labels[default_lang] = col_name.replace('_', ' ').title()
        
        # UI設定補完
        ui = col_def.setdefault('ui', {})
        ui.setdefault('hidden', False)
        ui.setdefault('readonly', False)
        
        # データ型に基づくUI設定の推論
        data_type = col_def.get('type', '').upper()
        if 'inputType' not in ui:
            ui['inputType'] = self._infer_input_type(data_type, col_name)
        
        # バリデーション補完
        validation = col_def.setdefault('validation', {})
        if constraints.get('nullable', True) == False:
            validation['required'] = True
    
    def _infer_input_type(self, data_type: str, col_name: str) -> str:
        """データ型とカラム名からUI入力タイプを推論"""
        col_name_lower = col_name.lower()
        
        # カラム名ベースの推論
        if 'email' in col_name_lower:
            return 'email'
        elif 'phone' in col_name_lower or 'tel' in col_name_lower:
            return 'tel'
        elif 'url' in col_name_lower or 'website' in col_name_lower:
            return 'url'
        elif 'password' in col_name_lower:
            return 'password'
        elif 'date' in col_name_lower:
            return 'date'
        elif 'time' in col_name_lower:
            return 'datetime-local'
        
        # データ型ベースの推論
        if 'INT' in data_type or 'BIGINT' in data_type:
            return 'number'
        elif 'DECIMAL' in data_type or 'FLOAT' in data_type:
            return 'number'
        elif 'BOOLEAN' in data_type or 'BOOL' in data_type:
            return 'checkbox'
        elif 'TEXT' in data_type or 'LONGTEXT' in data_type:
            return 'textarea'
        elif 'DATE' in data_type:
            return 'date'
        elif 'DATETIME' in data_type or 'TIMESTAMP' in data_type:
            return 'datetime-local'
        else:
            return 'text'
    
    def _get_metadata_schema(self) -> Dict[str, Any]:
        """メタデータJSONのスキーマ定義"""
        return {
            "type": "object",
            "properties": {
                "project": {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"},
                        "version": {"type": "string"},
                        "defaultLanguage": {"type": "string"},
                        "supportedLanguages": {
                            "type": "array",
                            "items": {"type": "string"}
                        }
                    }
                },
                "database": {
                    "type": "object",
                    "properties": {
                        "type": {"type": "string"},
                        "dialect": {"type": "string"},
                        "defaultTimezone": {"type": "string"}
                    }
                },
                "tables": {
                    "type": "object",
                    "patternProperties": {
                        "^[a-zA-Z_][a-zA-Z0-9_]*$": {
                            "type": "object",
                            "properties": {
                                "metadata": {"type": "object"},
                                "columns": {
                                    "type": "object",
                                    "patternProperties": {
                                        "^[a-zA-Z_][a-zA-Z0-9_]*$": {
                                            "type": "object",
                                            "properties": {
                                                "type": {"type": "string"},
                                                "length": {"type": "integer"},
                                                "precision": {"type": "integer"},
                                                "scale": {"type": "integer"},
                                                "constraints": {"type": "object"},
                                                "labels": {"type": "object"},
                                                "ui": {"type": "object"},
                                                "validation": {"type": "object"},
                                                "foreignKey": {"type": "object"}
                                            },
                                            "required": ["type"]
                                        }
                                    }
                                }
                            },
                            "required": ["columns"]
                        }
                    }
                },
                "relations": {
                    "type": "array",
                    "items": {"type": "object"}
                },
                "ui": {"type": "object"}
            },
            "required": ["tables"]
        }