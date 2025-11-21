#!/usr/bin/env python3
"""
SQLファイル生成機能
メタデータからCREATE TABLE文、INDEX、CONSTRAINT文を生成
"""

from pathlib import Path
from typing import Dict, Any, List
from jinja2 import Environment, FileSystemLoader


class SQLGenerator:
    """SQL生成クラス"""
    
    def __init__(self):
        # テンプレートエンジン初期化
        template_dir = Path(__file__).parent.parent / "templates" / "sql"
        self.env = Environment(
            loader=FileSystemLoader(str(template_dir)),
            trim_blocks=True,
            lstrip_blocks=True
        )
    
    def generate(self, metadata: Dict[str, Any], output_path: Path) -> Dict[str, str]:
        """
        メタデータからSQLファイルを生成
        
        Args:
            metadata: パース済みメタデータ
            output_path: 出力ディレクトリパス
            
        Returns:
            生成されたファイルの情報
        """
        results = {}
        
        # CREATE TABLE文生成
        create_sql = self._generate_create_tables(metadata)
        create_file = output_path / "create_tables.sql"
        with open(create_file, 'w', encoding='utf-8') as f:
            f.write(create_sql)
        results['create_tables'] = str(create_file)
        
        # INDEX文生成
        index_sql = self._generate_indexes(metadata)
        if index_sql.strip():
            index_file = output_path / "create_indexes.sql"
            with open(index_file, 'w', encoding='utf-8') as f:
                f.write(index_sql)
            results['indexes'] = str(index_file)
        
        # 外部キー制約生成
        fk_sql = self._generate_foreign_keys(metadata)
        if fk_sql.strip():
            fk_file = output_path / "create_foreign_keys.sql"
            with open(fk_file, 'w', encoding='utf-8') as f:
                f.write(fk_sql)
            results['foreign_keys'] = str(fk_file)
        
        # 統合SQLファイル生成
        combined_sql = self._generate_combined_sql(create_sql, index_sql, fk_sql)
        combined_file = output_path / "table_definitions.sql"
        with open(combined_file, 'w', encoding='utf-8') as f:
            f.write(combined_sql)
        results['combined'] = str(combined_file)
        
        return results
    
    def _generate_create_tables(self, metadata: Dict[str, Any]) -> str:
        """CREATE TABLE文を生成"""
        database_type = metadata.get('database', {}).get('type', 'h2')
        tables = metadata.get('tables', {})
        
        sql_parts = []
        sql_parts.append(f"-- Generated CREATE TABLE statements for {database_type}")
        sql_parts.append(f"-- Project: {metadata.get('project', {}).get('name', 'Unknown')}")
        sql_parts.append(f"-- Generated at: {self._get_timestamp()}")
        sql_parts.append("")
        
        # テーブルを依存関係順にソート
        sorted_tables = self._sort_tables_by_dependencies(tables)
        
        for table_name in sorted_tables:
            table_def = tables[table_name]
            sql = self._generate_table_sql(table_name, table_def, database_type)
            sql_parts.append(sql)
            sql_parts.append("")
        
        return "\n".join(sql_parts)
    
    def _generate_table_sql(self, table_name: str, table_def: Dict[str, Any], database_type: str) -> str:
        """単一テーブルのCREATE TABLE文を生成"""
        columns = table_def.get('columns', {})
        primary_key = table_def.get('primaryKey', {})
        
        # テーブルコメント
        comment_lines = []
        metadata = table_def.get('metadata', {})
        
        if 'labels' in metadata:
            ja_label = metadata['labels'].get('ja')
            en_label = metadata['labels'].get('en')
            if ja_label:
                comment_lines.append(f"-- {ja_label}")
            if en_label and en_label != ja_label:
                comment_lines.append(f"-- {en_label}")
        
        if 'description' in metadata:
            ja_desc = metadata['description'].get('ja')
            if ja_desc:
                comment_lines.append(f"-- {ja_desc}")
        
        sql_lines = comment_lines + [f"CREATE TABLE IF NOT EXISTS {table_name} ("]
        
        # カラム定義
        column_sqls = []
        for col_name, col_def in columns.items():
            col_sql = self._generate_column_sql(col_name, col_def, database_type)
            column_sqls.append(f"    {col_sql}")
        
        # 主キー制約
        if primary_key.get('type') == 'single':
            # 単一主キーはカラム定義に含める（既に処理済み）
            pass
        elif primary_key.get('type') == 'composite':
            # 複合主キーは別途定義
            pk_columns = primary_key.get('columns', [])
            if pk_columns:
                pk_constraint = f"PRIMARY KEY ({', '.join(pk_columns)})"
                column_sqls.append(f"    {pk_constraint}")
        
        sql_lines.append(",\n".join(column_sqls))
        sql_lines.append(");")
        
        return "\n".join(sql_lines)
    
    def _generate_column_sql(self, col_name: str, col_def: Dict[str, Any], database_type: str) -> str:
        """カラム定義SQLを生成"""
        data_type = col_def.get('type', 'VARCHAR')
        constraints = col_def.get('constraints', {})
        
        # データ型とサイズ
        type_sql = self._format_data_type(data_type, col_def, database_type)
        
        parts = [col_name, type_sql]
        
        # AUTO_INCREMENT / IDENTITY
        if constraints.get('autoIncrement', False):
            if database_type.lower() == 'h2':
                parts.append("AUTO_INCREMENT")
            elif database_type.lower() == 'mysql':
                parts.append("AUTO_INCREMENT")
            elif database_type.lower() == 'postgresql':
                # PostgreSQLの場合はSERIALを使用
                if 'BIGINT' in data_type.upper():
                    parts[1] = "BIGSERIAL"
                else:
                    parts[1] = "SERIAL"
        
        # NOT NULL
        if not constraints.get('nullable', True):
            parts.append("NOT NULL")
        
        # PRIMARY KEY（単一主キーの場合）
        if constraints.get('primaryKey', False):
            parts.append("PRIMARY KEY")
        
        # UNIQUE制約
        if constraints.get('unique', False):
            parts.append("UNIQUE")
        
        # DEFAULT値
        default_value = constraints.get('default')
        if default_value is not None:
            if isinstance(default_value, str):
                if default_value.upper() in ['CURRENT_TIMESTAMP', 'NOW()', 'NULL']:
                    parts.append(f"DEFAULT {default_value}")
                else:
                    parts.append(f"DEFAULT '{default_value}'")
            else:
                parts.append(f"DEFAULT {default_value}")
        
        # ON UPDATE（MySQLの場合）
        on_update = constraints.get('onUpdate')
        if on_update and database_type.lower() == 'mysql':
            parts.append(f"ON UPDATE {on_update}")
        
        return " ".join(parts)
    
    def _format_data_type(self, data_type: str, col_def: Dict[str, Any], database_type: str) -> str:
        """データ型の書式設定"""
        data_type_upper = data_type.upper()
        
        # 長さ指定
        if 'length' in col_def:
            return f"{data_type}({col_def['length']})"
        
        # 精度・スケール指定
        if 'precision' in col_def:
            if 'scale' in col_def:
                return f"{data_type}({col_def['precision']},{col_def['scale']})"
            else:
                return f"{data_type}({col_def['precision']})"
        
        # データベース特有の型変換
        if database_type.lower() == 'h2':
            if data_type_upper == 'TEXT':
                return 'LONGTEXT'
        elif database_type.lower() == 'postgresql':
            if data_type_upper == 'LONGTEXT':
                return 'TEXT'
            elif data_type_upper == 'DATETIME':
                return 'TIMESTAMP'
        
        return data_type
    
    def _generate_indexes(self, metadata: Dict[str, Any]) -> str:
        """INDEX文を生成"""
        tables = metadata.get('tables', {})
        sql_parts = []
        sql_parts.append("-- Generated INDEX statements")
        sql_parts.append("")
        
        for table_name, table_def in tables.items():
            columns = table_def.get('columns', {})
            
            for col_name, col_def in columns.items():
                constraints = col_def.get('constraints', {})
                
                # UNIQUE制約用のINDEX
                if constraints.get('unique', False):
                    index_name = f"idx_{table_name}_{col_name}_unique"
                    sql_parts.append(f"CREATE UNIQUE INDEX {index_name} ON {table_name} ({col_name});")
                
                # 外部キー用のINDEX
                if 'foreignKey' in col_def:
                    index_name = f"idx_{table_name}_{col_name}_fk"
                    sql_parts.append(f"CREATE INDEX {index_name} ON {table_name} ({col_name});")
        
        return "\n".join(sql_parts)
    
    def _generate_foreign_keys(self, metadata: Dict[str, Any]) -> str:
        """外部キー制約を生成"""
        tables = metadata.get('tables', {})
        sql_parts = []
        sql_parts.append("-- Generated FOREIGN KEY constraints")
        sql_parts.append("")
        
        for table_name, table_def in tables.items():
            columns = table_def.get('columns', {})
            
            for col_name, col_def in columns.items():
                foreign_key = col_def.get('foreignKey')
                if foreign_key:
                    ref_table = foreign_key.get('table')
                    ref_column = foreign_key.get('column', 'id')
                    on_delete = foreign_key.get('onDelete', 'RESTRICT')
                    on_update = foreign_key.get('onUpdate', 'RESTRICT')
                    
                    constraint_name = f"fk_{table_name}_{col_name}"
                    fk_sql = f"""ALTER TABLE {table_name} 
    ADD CONSTRAINT {constraint_name} 
    FOREIGN KEY ({col_name}) 
    REFERENCES {ref_table} ({ref_column})
    ON DELETE {on_delete} 
    ON UPDATE {on_update};"""
                    
                    sql_parts.append(fk_sql)
                    sql_parts.append("")
        
        return "\n".join(sql_parts)
    
    def _generate_combined_sql(self, create_sql: str, index_sql: str, fk_sql: str) -> str:
        """統合SQLファイルを生成"""
        parts = [create_sql]
        
        if index_sql.strip():
            parts.append(index_sql)
        
        if fk_sql.strip():
            parts.append(fk_sql)
        
        return "\n\n".join(parts)
    
    def _sort_tables_by_dependencies(self, tables: Dict[str, Any]) -> List[str]:
        """外部キー依存関係に基づいてテーブルをソート"""
        # 簡易実装: 外部キーを持たないテーブルを先に、持つテーブルを後に
        tables_without_fk = []
        tables_with_fk = []
        
        for table_name, table_def in tables.items():
            has_fk = False
            columns = table_def.get('columns', {})
            
            for col_def in columns.values():
                if 'foreignKey' in col_def:
                    has_fk = True
                    break
            
            if has_fk:
                tables_with_fk.append(table_name)
            else:
                tables_without_fk.append(table_name)
        
        # より高度な依存関係解析は将来実装
        return tables_without_fk + tables_with_fk
    
    def _get_timestamp(self) -> str:
        """現在時刻の文字列を取得"""
        from datetime import datetime
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")