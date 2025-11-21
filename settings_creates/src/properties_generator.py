#!/usr/bin/env python3
"""
Propertiesファイル生成機能
メタデータから多言語対応のプロパティファイルを生成
"""

from pathlib import Path
from typing import Dict, Any, List
import re


class PropertiesGenerator:
    """Properties生成クラス"""
    
    def generate(self, metadata: Dict[str, Any], output_path: Path) -> Dict[str, str]:
        """
        メタデータからPropertiesファイルを生成
        
        Args:
            metadata: パース済みメタデータ
            output_path: 出力ディレクトリパス
            
        Returns:
            生成されたファイルの情報
        """
        results = {}
        
        project = metadata.get('project', {})
        supported_languages = project.get('supportedLanguages', ['ja', 'en'])
        
        # 各言語のプロパティファイル生成
        for language in supported_languages:
            properties_content = self._generate_properties_for_language(metadata, language)
            
            filename = f"messages_{language}.properties"
            file_path = output_path / filename
            
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(properties_content)
            
            results[language] = str(file_path)
        
        # デフォルト言語のプロパティファイルも作成
        default_lang = project.get('defaultLanguage', 'ja')
        if default_lang in supported_languages:
            default_content = self._generate_properties_for_language(metadata, default_lang)
            default_file = output_path / "messages.properties"
            
            with open(default_file, 'w', encoding='utf-8') as f:
                f.write(default_content)
            
            results['default'] = str(default_file)
        
        return results
    
    def _generate_properties_for_language(self, metadata: Dict[str, Any], language: str) -> str:
        """指定言語のプロパティファイル内容を生成"""
        lines = []
        
        # ヘッダーコメント
        project_name = metadata.get('project', {}).get('name', 'CRUD System')
        lines.append(f"# {project_name} - {language.upper()} Messages")
        lines.append(f"# Generated at: {self._get_timestamp()}")
        lines.append("")
        
        # システム共通メッセージ
        lines.extend(self._generate_system_messages(language))
        lines.append("")
        
        # テーブル関連メッセージ
        tables = metadata.get('tables', {})
        for table_name, table_def in tables.items():
            lines.extend(self._generate_table_messages(table_name, table_def, language))
            lines.append("")
        
        # UI関連メッセージ
        lines.extend(self._generate_ui_messages(language))
        lines.append("")
        
        # バリデーションメッセージ
        lines.extend(self._generate_validation_messages(language))
        
        return "\n".join(lines)
    
    def _generate_system_messages(self, language: str) -> List[str]:
        """システム共通メッセージを生成"""
        messages = {
            'ja': {
                'app.title': 'CRUDシステム管理',
                'app.version': 'バージョン',
                'app.loading': '読み込み中...',
                'app.error': 'エラーが発生しました',
                'app.success': '操作が完了しました',
                'app.confirm': '実行してもよろしいですか？',
                'app.cancel': 'キャンセル',
                'app.ok': 'OK',
                'app.yes': 'はい',
                'app.no': 'いいえ',
                'app.close': '閉じる',
                'app.save': '保存',
                'app.delete': '削除',
                'app.edit': '編集',
                'app.create': '新規作成',
                'app.search': '検索',
                'app.filter': 'フィルター',
                'app.sort': '並び替え',
                'app.refresh': '更新'
            },
            'en': {
                'app.title': 'CRUD System Management',
                'app.version': 'Version',
                'app.loading': 'Loading...',
                'app.error': 'An error occurred',
                'app.success': 'Operation completed successfully',
                'app.confirm': 'Are you sure you want to proceed?',
                'app.cancel': 'Cancel',
                'app.ok': 'OK',
                'app.yes': 'Yes',
                'app.no': 'No',
                'app.close': 'Close',
                'app.save': 'Save',
                'app.delete': 'Delete',
                'app.edit': 'Edit',
                'app.create': 'Create New',
                'app.search': 'Search',
                'app.filter': 'Filter',
                'app.sort': 'Sort',
                'app.refresh': 'Refresh'
            }
        }
        
        lang_messages = messages.get(language, messages['en'])
        
        lines = ["# System Messages"]
        for key, value in lang_messages.items():
            lines.append(f"{key}={self._escape_property_value(value)}")
        
        return lines
    
    def _generate_table_messages(self, table_name: str, table_def: Dict[str, Any], language: str) -> List[str]:
        """テーブル関連メッセージを生成"""
        lines = [f"# Table: {table_name}"]
        
        metadata = table_def.get('metadata', {})
        
        # テーブル名とラベル
        table_label = metadata.get('labels', {}).get(language, table_name)
        table_desc = metadata.get('description', {}).get(language, '')
        
        lines.append(f"table.{table_name}.label={self._escape_property_value(table_label)}")
        if table_desc:
            lines.append(f"table.{table_name}.description={self._escape_property_value(table_desc)}")
        
        # CRUD操作ラベル
        crud_messages = self._get_crud_messages(table_name, table_label, language)
        lines.extend(crud_messages)
        
        # カラムラベル
        columns = table_def.get('columns', {})
        for col_name, col_def in columns.items():
            col_label = col_def.get('labels', {}).get(language, col_name)
            lines.append(f"table.{table_name}.column.{col_name}.label={self._escape_property_value(col_label)}")
            
            # プレースホルダー
            ui = col_def.get('ui', {})
            placeholder = ui.get('placeholder', {})
            if isinstance(placeholder, dict) and language in placeholder:
                placeholder_text = placeholder[language]
                lines.append(f"table.{table_name}.column.{col_name}.placeholder={self._escape_property_value(placeholder_text)}")
            
            # ヘルプテキスト
            help_text = col_def.get('helpText', {})
            if isinstance(help_text, dict) and language in help_text:
                help_msg = help_text[language]
                lines.append(f"table.{table_name}.column.{col_name}.help={self._escape_property_value(help_msg)}")
        
        return lines
    
    def _get_crud_messages(self, table_name: str, table_label: str, language: str) -> List[str]:
        """CRUD操作メッセージを生成"""
        if language == 'ja':
            messages = {
                f"table.{table_name}.action.create": f"{table_label}を新規作成",
                f"table.{table_name}.action.edit": f"{table_label}を編集",
                f"table.{table_name}.action.delete": f"{table_label}を削除",
                f"table.{table_name}.action.view": f"{table_label}を表示",
                f"table.{table_name}.action.list": f"{table_label}一覧",
                f"table.{table_name}.message.create.success": f"{table_label}を作成しました",
                f"table.{table_name}.message.update.success": f"{table_label}を更新しました",
                f"table.{table_name}.message.delete.success": f"{table_label}を削除しました",
                f"table.{table_name}.message.delete.confirm": f"この{table_label}を削除してもよろしいですか？",
                f"table.{table_name}.message.not.found": f"{table_label}が見つかりません",
                f"table.{table_name}.count": f"{table_label}件数"
            }
        else:  # en
            messages = {
                f"table.{table_name}.action.create": f"Create {table_label}",
                f"table.{table_name}.action.edit": f"Edit {table_label}",
                f"table.{table_name}.action.delete": f"Delete {table_label}",
                f"table.{table_name}.action.view": f"View {table_label}",
                f"table.{table_name}.action.list": f"{table_label} List",
                f"table.{table_name}.message.create.success": f"{table_label} created successfully",
                f"table.{table_name}.message.update.success": f"{table_label} updated successfully",
                f"table.{table_name}.message.delete.success": f"{table_label} deleted successfully",
                f"table.{table_name}.message.delete.confirm": f"Are you sure you want to delete this {table_label}?",
                f"table.{table_name}.message.not.found": f"{table_label} not found",
                f"table.{table_name}.count": f"{table_label} Count"
            }
        
        lines = []
        for key, value in messages.items():
            lines.append(f"{key}={self._escape_property_value(value)}")
        
        return lines
    
    def _generate_ui_messages(self, language: str) -> List[str]:
        """UI関連メッセージを生成"""
        lines = ["# UI Messages"]
        
        ui_messages = {
            'ja': {
                'ui.pagination.previous': '前のページ',
                'ui.pagination.next': '次のページ',
                'ui.pagination.first': '最初のページ',
                'ui.pagination.last': '最後のページ',
                'ui.pagination.page': 'ページ',
                'ui.pagination.of': '/',
                'ui.pagination.total': '件',
                'ui.pagination.show': '表示件数',
                'ui.sort.asc': '昇順',
                'ui.sort.desc': '降順',
                'ui.filter.clear': 'フィルター解除',
                'ui.filter.apply': 'フィルター適用',
                'ui.export.csv': 'CSV出力',
                'ui.export.excel': 'Excel出力',
                'ui.export.pdf': 'PDF出力',
                'ui.form.required': '必須項目',
                'ui.form.optional': '任意項目',
                'ui.form.save.draft': '下書き保存',
                'ui.form.reset': 'リセット'
            },
            'en': {
                'ui.pagination.previous': 'Previous',
                'ui.pagination.next': 'Next',
                'ui.pagination.first': 'First',
                'ui.pagination.last': 'Last',
                'ui.pagination.page': 'Page',
                'ui.pagination.of': 'of',
                'ui.pagination.total': 'total',
                'ui.pagination.show': 'Show',
                'ui.sort.asc': 'Ascending',
                'ui.sort.desc': 'Descending',
                'ui.filter.clear': 'Clear Filter',
                'ui.filter.apply': 'Apply Filter',
                'ui.export.csv': 'Export CSV',
                'ui.export.excel': 'Export Excel',
                'ui.export.pdf': 'Export PDF',
                'ui.form.required': 'Required',
                'ui.form.optional': 'Optional',
                'ui.form.save.draft': 'Save Draft',
                'ui.form.reset': 'Reset'
            }
        }
        
        lang_messages = ui_messages.get(language, ui_messages['en'])
        
        for key, value in lang_messages.items():
            lines.append(f"{key}={self._escape_property_value(value)}")
        
        return lines
    
    def _generate_validation_messages(self, language: str) -> List[str]:
        """バリデーションメッセージを生成"""
        lines = ["# Validation Messages"]
        
        validation_messages = {
            'ja': {
                'validation.required': 'この項目は必須です',
                'validation.min.length': '最低{0}文字以上入力してください',
                'validation.max.length': '{0}文字以内で入力してください',
                'validation.min.value': '{0}以上の値を入力してください',
                'validation.max.value': '{0}以下の値を入力してください',
                'validation.pattern.email': '有効なメールアドレスを入力してください',
                'validation.pattern.phone': '有効な電話番号を入力してください',
                'validation.pattern.url': '有効なURLを入力してください',
                'validation.unique': 'この値は既に使用されています',
                'validation.date.invalid': '有効な日付を入力してください',
                'validation.number.invalid': '有効な数値を入力してください',
                'validation.file.size': 'ファイルサイズが大きすぎます',
                'validation.file.type': '対応していないファイル形式です'
            },
            'en': {
                'validation.required': 'This field is required',
                'validation.min.length': 'Please enter at least {0} characters',
                'validation.max.length': 'Please enter no more than {0} characters',
                'validation.min.value': 'Please enter a value of {0} or greater',
                'validation.max.value': 'Please enter a value of {0} or less',
                'validation.pattern.email': 'Please enter a valid email address',
                'validation.pattern.phone': 'Please enter a valid phone number',
                'validation.pattern.url': 'Please enter a valid URL',
                'validation.unique': 'This value is already in use',
                'validation.date.invalid': 'Please enter a valid date',
                'validation.number.invalid': 'Please enter a valid number',
                'validation.file.size': 'File size is too large',
                'validation.file.type': 'Unsupported file format'
            }
        }
        
        lang_messages = validation_messages.get(language, validation_messages['en'])
        
        for key, value in lang_messages.items():
            lines.append(f"{key}={self._escape_property_value(value)}")
        
        return lines
    
    def _escape_property_value(self, value: str) -> str:
        """プロパティ値をエスケープ"""
        if not value:
            return ""
        
        # 改行文字をエスケープ
        value = value.replace('\n', '\\n')
        value = value.replace('\r', '\\r')
        
        # バックスラッシュをエスケープ
        value = value.replace('\\', '\\\\')
        
        # 等号とコロンをエスケープ
        value = value.replace('=', '\\=')
        value = value.replace(':', '\\:')
        
        # 先頭のスペースをエスケープ
        if value.startswith(' '):
            value = '\\ ' + value[1:]
        
        return value
    
    def _get_timestamp(self) -> str:
        """現在時刻の文字列を取得"""
        from datetime import datetime
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")