#!/usr/bin/env python3
"""
フロントエンド設定ファイル生成機能
メタデータからUIコンポーネント設定、バリデーション設定を生成
"""

import json
from pathlib import Path
from typing import Dict, Any, List


class FrontendGenerator:
    """フロントエンド設定生成クラス"""
    
    def generate(self, metadata: Dict[str, Any], output_path: Path) -> Dict[str, str]:
        """
        メタデータからフロントエンド設定ファイルを生成
        
        Args:
            metadata: パース済みメタデータ
            output_path: 出力ディレクトリパス
            
        Returns:
            生成されたファイルの情報
        """
        results = {}
        
        # テーブル設定ファイル生成
        table_config = self._generate_table_config(metadata)
        table_file = output_path / "table-config.json"
        with open(table_file, 'w', encoding='utf-8') as f:
            json.dump(table_config, f, indent=2, ensure_ascii=False)
        results['table_config'] = str(table_file)
        
        # バリデーション設定ファイル生成
        validation_config = self._generate_validation_config(metadata)
        validation_file = output_path / "validation-config.json"
        with open(validation_file, 'w', encoding='utf-8') as f:
            json.dump(validation_config, f, indent=2, ensure_ascii=False)
        results['validation_config'] = str(validation_file)
        
        # UI設定ファイル生成
        ui_config = self._generate_ui_config(metadata)
        ui_file = output_path / "ui-config.json"
        with open(ui_file, 'w', encoding='utf-8') as f:
            json.dump(ui_config, f, indent=2, ensure_ascii=False)
        results['ui_config'] = str(ui_file)
        
        # TypeScript型定義ファイル生成
        types_content = self._generate_typescript_types(metadata)
        types_file = output_path / "types.ts"
        with open(types_file, 'w', encoding='utf-8') as f:
            f.write(types_content)
        results['typescript_types'] = str(types_file)
        
        # React Hook 設定生成
        hooks_content = self._generate_react_hooks(metadata)
        hooks_file = output_path / "useTable.ts"
        with open(hooks_file, 'w', encoding='utf-8') as f:
            f.write(hooks_content)
        results['react_hooks'] = str(hooks_file)
        
        return results
    
    def _generate_table_config(self, metadata: Dict[str, Any]) -> Dict[str, Any]:
        """テーブル設定を生成"""
        tables = metadata.get('tables', {})
        config = {
            "version": "1.0.0",
            "generated": self._get_timestamp(),
            "project": metadata.get('project', {}),
            "tables": {}
        }
        
        for table_name, table_def in tables.items():
            table_config = self._generate_single_table_config(table_name, table_def)
            config["tables"][table_name] = table_config
        
        return config
    
    def _generate_single_table_config(self, table_name: str, table_def: Dict[str, Any]) -> Dict[str, Any]:
        """単一テーブルの設定を生成"""
        metadata = table_def.get('metadata', {})
        columns = table_def.get('columns', {})
        primary_key = table_def.get('primaryKey', {})
        
        config = {
            "name": table_name,
            "metadata": {
                "icon": metadata.get('icon', '🗄️'),
                "color": metadata.get('color', '#4A90E2'),
                "sortOrder": metadata.get('sortOrder', 0),
                "category": metadata.get('category', 'general'),
                "labels": metadata.get('labels', {}),
                "description": metadata.get('description', {})
            },
            "primaryKey": primary_key,
            "columns": [],
            "formFields": [],
            "listColumns": [],
            "searchableColumns": [],
            "sortableColumns": []
        }
        
        # カラム設定生成
        for col_name, col_def in columns.items():
            column_config = self._generate_column_config(col_name, col_def)
            config["columns"].append(column_config)
            
            # フォームフィールド設定
            if not col_def.get('ui', {}).get('hidden', False):
                form_field = self._generate_form_field_config(col_name, col_def)
                config["formFields"].append(form_field)
            
            # リスト表示カラム設定
            if not col_def.get('ui', {}).get('hidden', False):
                list_column = self._generate_list_column_config(col_name, col_def)
                config["listColumns"].append(list_column)
            
            # 検索可能カラム
            if self._is_searchable_column(col_def):
                config["searchableColumns"].append(col_name)
            
            # ソート可能カラム
            if self._is_sortable_column(col_def):
                config["sortableColumns"].append(col_name)
        
        return config
    
    def _generate_column_config(self, col_name: str, col_def: Dict[str, Any]) -> Dict[str, Any]:
        """カラム設定を生成"""
        return {
            "name": col_name,
            "type": col_def.get('type', 'VARCHAR'),
            "constraints": col_def.get('constraints', {}),
            "labels": col_def.get('labels', {}),
            "ui": col_def.get('ui', {}),
            "validation": col_def.get('validation', {}),
            "foreignKey": col_def.get('foreignKey')
        }
    
    def _generate_form_field_config(self, col_name: str, col_def: Dict[str, Any]) -> Dict[str, Any]:
        """フォームフィールド設定を生成"""
        ui = col_def.get('ui', {})
        validation = col_def.get('validation', {})
        constraints = col_def.get('constraints', {})
        
        # hidden フィールドの場合は type を hidden に設定
        input_type = ui.get('inputType', 'text')
        if ui.get('hidden', False):
            input_type = 'hidden'
        
        field = {
            "name": col_name,
            "type": input_type,
            "label": col_def.get('labels', {}),
            "placeholder": ui.get('placeholder', {}),
            "required": validation.get('required', not constraints.get('nullable', True)),
            "readonly": ui.get('readonly', False) or constraints.get('autoIncrement', False),
            "disabled": ui.get('disabled', False)
        }
        
        # hidden プロパティを追加（UI表示制御用）
        if ui.get('hidden', False):
            field["hidden"] = True
        
        # 入力タイプ別の追加設定
        input_type = field["type"]
        
        if input_type == 'number':
            if 'min' in validation:
                field["min"] = validation['min']
            if 'max' in validation:
                field["max"] = validation['max']
            if 'step' in ui:
                field["step"] = ui['step']
        
        elif input_type == 'text' or input_type == 'textarea':
            if 'minLength' in validation:
                field["minLength"] = validation['minLength']
            if 'maxLength' in validation:
                field["maxLength"] = validation['maxLength']
            if input_type == 'textarea' and 'rows' in ui:
                field["rows"] = ui['rows']
        
        elif input_type == 'select':
            # 外部キー参照の場合
            foreign_key = col_def.get('foreignKey')
            if foreign_key:
                field["options"] = {
                    "type": "foreign_key",
                    "table": foreign_key.get('table'),
                    "valueColumn": foreign_key.get('column', 'id'),
                    "displayColumn": foreign_key.get('displayColumn', 'name'),
                    "allowNull": ui.get('allowNull', constraints.get('nullable', True)),
                    "nullLabel": ui.get('nullLabel', {})
                }
        
        elif input_type == 'checkbox':
            field["defaultValue"] = constraints.get('default', False)
        
        # バリデーション設定
        if validation:
            field["validation"] = validation.copy()
        
        # Autofill 設定（メタデータに autofill プロパティがある場合）
        autofill = col_def.get('autofill')
        if autofill:
            field["autofill"] = {
                "enabled": autofill.get('enabled', True),
                "sourceTable": autofill.get('sourceTable'),
                "sourceColumn": autofill.get('sourceColumn'),
                "mappings": autofill.get('mappings', [])
            }
        
        # AutoCalculate 設定（メタデータに autoCalculate プロパティがある場合）
        auto_calculate = col_def.get('autoCalculate')
        if auto_calculate:
            field["autoCalculate"] = {
                "enabled": auto_calculate.get('enabled', True),
                "formula": auto_calculate.get('formula'),
                "targetField": auto_calculate.get('targetField'),
                "triggerFields": auto_calculate.get('triggerFields', [])
            }
        
        return field
    
    def _generate_list_column_config(self, col_name: str, col_def: Dict[str, Any]) -> Dict[str, Any]:
        """リストカラム設定を生成"""
        ui = col_def.get('ui', {})
        
        config = {
            "name": col_name,
            "label": col_def.get('labels', {}),
            "type": col_def.get('type', 'VARCHAR'),
            "sortable": self._is_sortable_column(col_def),
            "searchable": self._is_searchable_column(col_def),
            "width": ui.get('width', 'auto'),
            "align": ui.get('align', 'left')
        }
        
        # データ型別の表示設定
        data_type = col_def.get('type', '').upper()
        
        if 'DATETIME' in data_type or 'TIMESTAMP' in data_type:
            config["format"] = "datetime"
        elif 'DATE' in data_type:
            config["format"] = "date"
        elif 'DECIMAL' in data_type or 'FLOAT' in data_type:
            config["format"] = "decimal"
            if 'scale' in col_def:
                config["decimalPlaces"] = col_def['scale']
        elif 'BOOLEAN' in data_type:
            config["format"] = "boolean"
        
        # 外部キー参照の場合
        foreign_key = col_def.get('foreignKey')
        if foreign_key:
            config["foreignKey"] = {
                "table": foreign_key.get('table'),
                "displayColumn": foreign_key.get('displayColumn', 'name')
            }
        
        return config
    
    def _generate_validation_config(self, metadata: Dict[str, Any]) -> Dict[str, Any]:
        """バリデーション設定を生成"""
        tables = metadata.get('tables', {})
        
        config = {
            "version": "1.0.0",
            "generated": self._get_timestamp(),
            "tables": {}
        }
        
        for table_name, table_def in tables.items():
            table_validation = {
                "name": table_name,
                "fields": {},
                "rules": []
            }
            
            columns = table_def.get('columns', {})
            for col_name, col_def in columns.items():
                validation = col_def.get('validation', {})
                constraints = col_def.get('constraints', {})
                
                if validation or constraints:
                    field_validation = self._generate_field_validation(col_name, col_def)
                    table_validation["fields"][col_name] = field_validation
            
            config["tables"][table_name] = table_validation
        
        return config
    
    def _generate_field_validation(self, col_name: str, col_def: Dict[str, Any]) -> Dict[str, Any]:
        """フィールドバリデーション設定を生成"""
        validation = col_def.get('validation', {})
        constraints = col_def.get('constraints', {})
        
        rules = []
        
        # 必須チェック
        if validation.get('required') or not constraints.get('nullable', True):
            rules.append({"type": "required", "message": "validation.required"})
        
        # 長さチェック
        if 'minLength' in validation:
            rules.append({
                "type": "minLength",
                "value": validation['minLength'],
                "message": "validation.min.length"
            })
        
        if 'maxLength' in validation:
            rules.append({
                "type": "maxLength", 
                "value": validation['maxLength'],
                "message": "validation.max.length"
            })
        
        # 値の範囲チェック
        if 'min' in validation:
            rules.append({
                "type": "min",
                "value": validation['min'],
                "message": "validation.min.value"
            })
        
        if 'max' in validation:
            rules.append({
                "type": "max",
                "value": validation['max'],
                "message": "validation.max.value"
            })
        
        # パターンチェック
        if 'pattern' in validation:
            pattern_type = validation['pattern']
            if pattern_type == 'email':
                rules.append({
                    "type": "pattern",
                    "value": r'^[^\s@]+@[^\s@]+\.[^\s@]+$',
                    "message": "validation.pattern.email"
                })
            elif pattern_type == 'phone':
                rules.append({
                    "type": "pattern",
                    "value": r'^[\d\-\(\)\+\s]+$',
                    "message": "validation.pattern.phone"
                })
            elif pattern_type == 'url':
                rules.append({
                    "type": "pattern",
                    "value": r'^https?://[^\s]+$',
                    "message": "validation.pattern.url"
                })
            else:
                rules.append({
                    "type": "pattern",
                    "value": pattern_type,
                    "message": "validation.pattern.invalid"
                })
        
        # UNIQUE制約
        if constraints.get('unique', False):
            rules.append({
                "type": "unique",
                "message": "validation.unique"
            })
        
        return {
            "name": col_name,
            "rules": rules,
            "realtime": validation.get('realtime', True)
        }
    
    def _generate_ui_config(self, metadata: Dict[str, Any]) -> Dict[str, Any]:
        """UI設定を生成"""
        ui_settings = metadata.get('ui', {})
        project = metadata.get('project', {})
        
        config = {
            "version": "1.0.0",
            "generated": self._get_timestamp(),
            "project": {
                "name": project.get('name', 'CRUD System'),
                "languages": project.get('supportedLanguages', ['ja', 'en']),
                "defaultLanguage": project.get('defaultLanguage', 'ja')
            },
            "theme": ui_settings.get('theme', {
                "primary": "#4A90E2",
                "secondary": "#E67E22", 
                "success": "#27AE60",
                "warning": "#F39C12",
                "danger": "#E74C3C",
                "info": "#3498DB",
                "light": "#ECF0F1",
                "dark": "#2C3E50"
            }),
            "layout": {
                "sidebar": ui_settings.get('sidebar', {
                    "width": "280px",
                    "backgroundColor": "#2C3E50",
                    "textColor": "#ECF0F1"
                }),
                "header": {
                    "height": "60px",
                    "backgroundColor": "#34495E",
                    "textColor": "#FFFFFF"
                },
                "content": {
                    "padding": "20px",
                    "backgroundColor": "#FFFFFF"
                }
            },
            "pagination": ui_settings.get('pagination', {
                "defaultPageSize": 20,
                "pageSizeOptions": [10, 20, 50, 100]
            }),
            "table": {
                "striped": True,
                "hover": True,
                "bordered": False,
                "compact": False
            },
            "form": {
                "labelPosition": "top",
                "showRequiredAsterisk": True,
                "showOptionalText": False,
                "validateOnBlur": True,
                "validateOnChange": False
            }
        }
        
        return config
    
    def _generate_typescript_types(self, metadata: Dict[str, Any]) -> str:
        """TypeScript型定義を生成"""
        lines = []
        lines.append("// Generated TypeScript type definitions")
        lines.append(f"// Generated at: {self._get_timestamp()}")
        lines.append("")
        
        # 共通型定義
        lines.extend(self._generate_common_types())
        lines.append("")
        
        # テーブル型定義
        tables = metadata.get('tables', {})
        for table_name, table_def in tables.items():
            table_types = self._generate_table_types(table_name, table_def)
            lines.extend(table_types)
            lines.append("")
        
        return "\n".join(lines)
    
    def _generate_common_types(self) -> List[str]:
        """共通型定義を生成"""
        return [
            "// Common types",
            "export type ValidationRule = {",
            "  type: string;",
            "  value?: any;", 
            "  message: string;",
            "};",
            "",
            "export type FieldValidation = {",
            "  name: string;",
            "  rules: ValidationRule[];",
            "  realtime: boolean;",
            "};",
            "",
            "export type FormField = {",
            "  name: string;",
            "  type: string;",
            "  label: Record<string, string>;",
            "  placeholder?: Record<string, string>;",
            "  required: boolean;",
            "  readonly: boolean;",
            "  disabled: boolean;",
            "  [key: string]: any;",
            "};",
            "",
            "export type ListColumn = {", 
            "  name: string;",
            "  label: Record<string, string>;",
            "  type: string;",
            "  sortable: boolean;",
            "  searchable: boolean;",
            "  width: string;",
            "  align: string;",
            "  format?: string;",
            "  [key: string]: any;",
            "};"
        ]
    
    def _generate_table_types(self, table_name: str, table_def: Dict[str, Any]) -> List[str]:
        """テーブル型定義を生成"""
        lines = []
        
        # インターフェース名
        interface_name = self._to_pascal_case(table_name)
        lines.append(f"// {table_name} table types")
        
        # データ型定義
        lines.append(f"export interface {interface_name} {{")
        
        columns = table_def.get('columns', {})
        for col_name, col_def in columns.items():
            ts_type = self._get_typescript_type(col_def)
            nullable = col_def.get('constraints', {}).get('nullable', True)
            
            optional = "?" if nullable else ""
            lines.append(f"  {col_name}{optional}: {ts_type};")
        
        lines.append("}")
        lines.append("")
        
        # フォーム用型定義
        lines.append(f"export interface {interface_name}Form {{")
        for col_name, col_def in columns.items():
            # 自動生成カラムはフォームから除外
            if not col_def.get('constraints', {}).get('autoIncrement', False):
                ts_type = self._get_typescript_type(col_def)
                nullable = col_def.get('constraints', {}).get('nullable', True)
                optional = "?" if nullable else ""
                lines.append(f"  {col_name}{optional}: {ts_type};")
        
        lines.append("}")
        
        return lines
    
    def _generate_react_hooks(self, metadata: Dict[str, Any]) -> str:
        """React Hook を生成"""
        lines = []
        lines.append("// Generated React Hooks for table operations")
        lines.append(f"// Generated at: {self._get_timestamp()}")
        lines.append("")
        lines.append("import { useState, useEffect } from 'react';")
        lines.append("")
        
        # 共通Hook
        lines.extend(self._generate_common_hooks())
        
        # テーブル別Hook
        tables = metadata.get('tables', {})
        for table_name, table_def in tables.items():
            hook_code = self._generate_table_hook(table_name, table_def)
            lines.extend(hook_code)
            lines.append("")
        
        return "\n".join(lines)
    
    def _generate_common_hooks(self) -> List[str]:
        """共通Hookを生成"""
        return [
            "// Common API response type",
            "interface ApiResponse<T> {",
            "  success: boolean;",
            "  data?: T;",
            "  message?: string;",
            "  error?: string;",
            "}",
            "",
            "// Common hook for API calls",
            "export const useApi = <T>() => {",
            "  const [loading, setLoading] = useState(false);",
            "  const [error, setError] = useState<string | null>(null);",
            "",
            "  const callApi = async (url: string, options: RequestInit = {}): Promise<T | null> => {",
            "    try {",
            "      setLoading(true);",
            "      setError(null);",
            "",
            "      const response = await fetch(url, {",
            "        headers: {",
            "          'Content-Type': 'application/json',",
            "          ...options.headers,",
            "        },",
            "        ...options,",
            "      });",
            "",
            "      if (!response.ok) {",
            "        throw new Error(`HTTP error! status: ${response.status}`);",
            "      }",
            "",
            "      const result: ApiResponse<T> = await response.json();",
            "      ",
            "      if (!result.success) {",
            "        throw new Error(result.error || result.message || 'API call failed');",
            "      }",
            "",
            "      return result.data || null;",
            "    } catch (err) {",
            "      const errorMessage = err instanceof Error ? err.message : 'Unknown error';",
            "      setError(errorMessage);",
            "      return null;",
            "    } finally {",
            "      setLoading(false);",
            "    }",
            "  };",
            "",
            "  return { callApi, loading, error };",
            "};",
            ""
        ]
    
    def _generate_table_hook(self, table_name: str, table_def: Dict[str, Any]) -> List[str]:
        """テーブル用Hookを生成"""
        interface_name = self._to_pascal_case(table_name)
        hook_name = f"use{interface_name}"
        
        lines = [
            f"// Hook for {table_name} table operations", 
            f"export const {hook_name} = () => {{",
            f"  const [records, setRecords] = useState<{interface_name}[]>([]);",
            "  const { callApi, loading, error } = useApi();",
            "",
            "  // Fetch all records",
            "  const fetchRecords = async () => {",
            f"    const data = await callApi<{interface_name}[]>(`/api/config/data/{table_name}`, {{",
            "      method: 'GET',",
            "    }});",
            "    ",
            "    if (data) {",
            "      setRecords(data);",
            "    }",
            "  };",
            "",
            "  // Create new record",
            f"  const createRecord = async (data: {interface_name}Form) => {{",
            f"    const result = await callApi<{interface_name}>(`/api/config/data/{table_name}`, {{",
            "      method: 'POST',",
            "      body: JSON.stringify(data),",
            "    }});",
            "    ",
            "    if (result) {",
            "      setRecords(prev => [...prev, result]);",
            "    }",
            "    ",
            "    return result;",
            "  };",
            "",
            "  // Update existing record",
            f"  const updateRecord = async (id: number, data: {interface_name}Form) => {{",
            f"    const result = await callApi<{interface_name}>(`/api/config/data/{table_name}/${{id}}`, {{",
            "      method: 'PUT',",
            "      body: JSON.stringify(data),",
            "    }});",
            "    ",
            "    if (result) {",
            "      setRecords(prev => prev.map(record => ",
            "        record.id === id ? result : record",
            "      ));",
            "    }",
            "    ",
            "    return result;",
            "  };",
            "",
            "  // Delete record",
            "  const deleteRecord = async (id: number) => {",
            "    const success = await callApi<boolean>(`/api/config/data/{table_name}/${id}`, {",
            "      method: 'DELETE',",
            "    });",
            "    ",
            "    if (success) {",
            "      setRecords(prev => prev.filter(record => record.id !== id));",
            "    }",
            "    ",
            "    return success;",
            "  };",
            "",
            "  return {",
            "    records,",
            "    fetchRecords,",
            "    createRecord,",
            "    updateRecord,", 
            "    deleteRecord,",
            "    loading,",
            "    error,",
            "  };",
            "};"
        ]
        
        return lines
    
    def _is_searchable_column(self, col_def: Dict[str, Any]) -> bool:
        """検索可能カラムかどうか判定"""
        data_type = col_def.get('type', '').upper()
        
        # テキスト系は検索可能
        if any(t in data_type for t in ['VARCHAR', 'TEXT', 'CHAR']):
            return True
        
        # 数値系は検索可能（範囲検索）
        if any(t in data_type for t in ['INT', 'DECIMAL', 'FLOAT']):
            return True
        
        return False
    
    def _is_sortable_column(self, col_def: Dict[str, Any]) -> bool:
        """ソート可能カラムかどうか判定"""
        data_type = col_def.get('type', '').upper()
        
        # TEXT以外はソート可能
        if 'TEXT' in data_type or 'BLOB' in data_type:
            return False
        
        return True
    
    def _get_typescript_type(self, col_def: Dict[str, Any]) -> str:
        """TypeScript型を取得"""
        data_type = col_def.get('type', '').upper()
        
        if any(t in data_type for t in ['INT', 'BIGINT', 'DECIMAL', 'FLOAT']):
            return 'number'
        elif any(t in data_type for t in ['BOOLEAN', 'BOOL']):
            return 'boolean'
        elif any(t in data_type for t in ['DATE', 'DATETIME', 'TIMESTAMP']):
            return 'Date | string'
        else:
            return 'string'
    
    def _to_pascal_case(self, snake_str: str) -> str:
        """スネークケースをパスカルケースに変換"""
        return ''.join(word.capitalize() for word in snake_str.split('_'))
    
    def _get_timestamp(self) -> str:
        """現在時刻の文字列を取得"""
        from datetime import datetime
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")