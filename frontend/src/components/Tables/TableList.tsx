import React, { useState, useEffect } from 'react';
import './TableList.css';

interface TableListProps {
  tableName: string;
  onEdit: (data: Record<string, any>) => void;
}

interface TableRecord {
  [key: string]: any;
}

type ListColumn = {
  name: string;
  label: Record<string, string>;
  type: string;
  sortable: boolean;
  searchable: boolean;
  width: string;
  align: string;
  format?: string;
  foreignKey?: {
    table: string;
    displayColumn: string;
  };
  [key: string]: any;
};

type TableConfig = {
  tables: {
    [tableName: string]: {
      name: string;
      listColumns: ListColumn[];
      metadata: {
        labels: Record<string, string>;
        description: Record<string, string>;
      };
      primaryKey?: {
        type: 'single' | 'composite';
        columns: string[];
      };
    };
  };
  project: {
    defaultLanguage: string;
    supportedLanguages: string[];
  };
};

const TableList: React.FC<TableListProps> = ({ tableName, onEdit }) => {
  const [records, setRecords] = useState<TableRecord[]>([]);
  const [tableConfig, setTableConfig] = useState<TableConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [language, setLanguage] = useState('ja');
  const [sortColumn, setSortColumn] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [foreignKeyData, setForeignKeyData] = useState<Record<string, Record<string, any>>>({});

  useEffect(() => {
    if (tableName) {
      loadTableConfig();
    }
  }, [tableName]);

  useEffect(() => {
    if (tableConfig && tableName) {
      fetchRecords();
      loadForeignKeyData();
    }
  }, [tableConfig, tableName]);

  const loadTableConfig = async () => {
    try {
      setLoading(true);
      setError(null);

      // table-config.json をバックエンドから読み込み
      const response = await fetch('http://localhost:8082/api/sql/config/table-config', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({}),
      });
      if (!response.ok) {
        throw new Error('テーブル設定の読み込みに失敗しました');
      }
      const configData = await response.json();
      setTableConfig(configData);
      setLanguage(configData.project.defaultLanguage || 'ja');

    } catch (err) {
      setError(err instanceof Error ? err.message : '設定ファイルの読み込みに失敗しました');
      console.error('Failed to load table config:', err);
      setLoading(false);
    }
  };

  const loadForeignKeyData = async () => {
    console.log('=== loadForeignKeyData called ===');
    if (!tableConfig || !tableName) return;
    
    const currentTable = tableConfig.tables[tableName];
    if (!currentTable) return;
    
    console.log('Current table:', tableName);
    console.log('List columns:', currentTable.listColumns);
    
    const foreignKeyColumns = currentTable.listColumns.filter(col => col.foreignKey);
    console.log('Foreign key columns found:', foreignKeyColumns);
    
    const foreignData: Record<string, Record<string, any>> = {};
    
    for (const column of foreignKeyColumns) {
      if (column.foreignKey) {
        console.log(`Loading foreign key data for column ${column.name} -> table ${column.foreignKey.table}`);
        try {
          const response = await fetch('http://localhost:8082/api/sql/findAll', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ tableName: column.foreignKey.table }),
          });
          
          if (response.ok) {
            const data = await response.json();
            console.log(`Response for ${column.foreignKey.table}:`, data);
            if (data.success && data.data) {
              const lookupTable: Record<string, any> = {};
              data.data.forEach((record: any) => {
                // IDの大文字小文字両方に対応
                const keyValue = record.id || record.ID;
                if (keyValue !== undefined) {
                  lookupTable[keyValue] = record;
                }
              });
              foreignData[column.foreignKey.table] = lookupTable;
              console.log(`Lookup table for ${column.foreignKey.table}:`, lookupTable);
            }
          } else {
            console.error(`Failed to fetch ${column.foreignKey.table}:`, response.status);
          }
        } catch (err) {
          console.error(`Failed to load foreign key data for ${column.foreignKey.table}:`, err);
        }
      }
    }
    
    console.log('Final foreignData:', foreignData);
    setForeignKeyData(foreignData);
    console.log('=== loadForeignKeyData completed ===');
  };

  const fetchRecords = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await fetch('http://localhost:8082/api/sql/findAll', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ tableName }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      if (data.success && data.data) {
        setRecords(data.data);
      } else {
        throw new Error('データの取得に失敗しました');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '不明なエラーが発生しました');
      console.error('Failed to fetch records:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (record: TableRecord) => {
    if (!window.confirm('このレコードを削除しますか？')) {
      return;
    }

    try {
      const currentTable = tableConfig?.tables[tableName];
      let requestBody: any = { tableName };

      // 複合主キーの判定と処理
      if (currentTable?.primaryKey?.type === 'composite' && currentTable?.primaryKey?.columns) {
        // 複合主キーの場合
        const keyValues: Record<string, any> = {};
        currentTable.primaryKey.columns.forEach((column: string) => {
          const value = record[column] || record[column.toUpperCase()];
          if (value !== undefined) {
            keyValues[column] = value;
          }
        });
        requestBody.keyValues = keyValues;
        console.log('複合主キー削除リクエスト:', requestBody);
      } else {
        // 単一主キーの場合（従来通り）
        const id = record.id || record.ID;
        requestBody.id = id;
        console.log('単一主キー削除リクエスト:', requestBody);
      }
      
      const response = await fetch('http://localhost:8082/api/sql/delete', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('削除エラーレスポンス:', errorText);
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
      }

      const data = await response.json();
      console.log('削除レスポンス:', data);
      
      if (data.success) {
        // レコードリストから削除
        if (currentTable?.primaryKey?.type === 'composite' && currentTable?.primaryKey?.columns) {
          // 複合主キーの場合
          setRecords(records.filter(r => {
            return !currentTable.primaryKey?.columns?.every((col: string) => {
              const recordValue = r[col] || r[col.toUpperCase()];
              const targetValue = record[col] || record[col.toUpperCase()];
              return recordValue === targetValue;
            });
          }));
        } else {
          // 単一主キーの場合
          const id = record.id || record.ID;
          setRecords(records.filter(r => 
            (r.id !== id) && (r.ID !== id)
          ));
        }
        alert('レコードが削除されました');
      } else {
        throw new Error(data.message || '削除に失敗しました');
      }
    } catch (err) {
      alert(err instanceof Error ? err.message : '削除に失敗しました');
      console.error('Failed to delete record:', err);
    }
  };

  const handleSort = (columnName: string) => {
    const currentTable = tableConfig?.tables[tableName];
    if (!currentTable) return;

    const column = currentTable.listColumns.find(col => col.name === columnName);
    if (!column?.sortable) return;

    if (sortColumn === columnName) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortColumn(columnName);
      setSortDirection('asc');
    }
  };

  const getSortedRecords = () => {
    let filtered = records.filter(record =>
      Object.values(record).some(value =>
        String(value).toLowerCase().includes(searchQuery.toLowerCase())
      )
    );

    if (sortColumn) {
      filtered = [...filtered].sort((a, b) => {
        const aVal = a[sortColumn];
        const bVal = b[sortColumn];
        
        if (aVal === null || aVal === undefined) return 1;
        if (bVal === null || bVal === undefined) return -1;
        
        let comparison = 0;
        if (typeof aVal === 'string' && typeof bVal === 'string') {
          comparison = aVal.localeCompare(bVal);
        } else {
          comparison = aVal < bVal ? -1 : aVal > bVal ? 1 : 0;
        }
        
        return sortDirection === 'asc' ? comparison : -comparison;
      });
    }

    return filtered;
  };

  const formatValue = (value: any, column: ListColumn): string => {
    console.log(`formatValue called for column ${column.name}, value:`, value, 'column:', column);
    
    if (value === null || value === undefined) {
      console.log(`  value is null/undefined, returning '-'`);
      return '-';
    }
    
    // 外部キーの解決
    if (column.foreignKey) {
      console.log(`Foreign key processing for ${column.name}`);
      const foreignTable = column.foreignKey.table;
      const displayColumn = column.foreignKey.displayColumn;
      const lookupTable = foreignKeyData[foreignTable];
      
      console.log(`  foreignTable: ${foreignTable}`);
      console.log(`  displayColumn: ${displayColumn}`);
      console.log(`  lookupTable:`, lookupTable);
      console.log(`  value to lookup: ${value}`);
      
      if (lookupTable && lookupTable[value]) {
        // displayColumnも大文字小文字両方試す
        const record = lookupTable[value];
        const displayValue = record[displayColumn] || record[displayColumn.toUpperCase()] || record[displayColumn.toLowerCase()];
        console.log(`  found record:`, record);
        console.log(`  trying displayColumn: ${displayColumn}, ${displayColumn.toUpperCase()}, ${displayColumn.toLowerCase()}`);
        console.log(`  final displayValue: ${displayValue}`);
        return displayValue !== null && displayValue !== undefined ? String(displayValue) : '-';
      } else {
        console.log(`  lookup failed - returning original value: ${value}`);
        return String(value); // フォールバック: 元の値を表示
      }
    }
    
    switch (column.format) {
      case 'boolean':
        return value ? '✓' : '✗';
      case 'decimal':
        return typeof value === 'number' ? value.toFixed(column.decimalPlaces || 2) : String(value);
      default:
        if (typeof value === 'boolean') return value ? '✓' : '✗';
        if (typeof value === 'string' && value.includes('T')) {
          // 日時フォーマット
          try {
            return new Date(value).toLocaleString('ja-JP');
          } catch {
            return String(value);
          }
        }
        return String(value);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="content-loading-spinner"></div>
        <div className="loading-text">データを読み込み中...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <h3 className="error-title">エラーが発生しました</h3>
        <p className="error-message">{error}</p>
        <button onClick={fetchRecords} className="retry-button">
          再読み込み
        </button>
      </div>
    );
  }

  if (!tableConfig || !tableConfig.tables[tableName]) {
    return (
      <div className="error-container">
        <h3 className="error-title">テーブル設定が見つかりません</h3>
        <p className="error-message">テーブル「{tableName}」の設定が存在しません。</p>
      </div>
    );
  }

  const currentTable = tableConfig.tables[tableName];
  const displayColumns = currentTable.listColumns;
  const filteredRecords = getSortedRecords();

  return (
    <div className="table-list">
      <div className="table-list-header">
        <div className="search-section">
          <input
            type="text"
            placeholder="検索..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />
        </div>
        <div className="action-section">
          <button onClick={fetchRecords} className="refresh-button">
            🔄 更新
          </button>
        </div>
      </div>

      <div className="table-container">
        {records.length === 0 ? (
          <div className="empty-records">
            <div className="empty-icon">📝</div>
            <h3>データがありません</h3>
            <p>「新規作成」ボタンから最初のレコードを作成してください。</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                {displayColumns.map((column) => {
                  const displayLabel = column.label[language] || column.label.ja || column.name;
                  const isSorted = sortColumn === column.name;
                  const sortIcon = isSorted 
                    ? (sortDirection === 'asc' ? ' ↑' : ' ↓') 
                    : (column.sortable ? ' ↕' : '');
                  
                  return (
                    <th 
                      key={column.name} 
                      className={column.sortable ? 'sortable' : ''}
                      onClick={() => column.sortable && handleSort(column.name)}
                      style={{ textAlign: column.align as any }}
                    >
                      {displayLabel}{sortIcon}
                    </th>
                  );
                })}
                <th className="actions-column">操作</th>
              </tr>
            </thead>
            <tbody>
              {filteredRecords.map((record, index) => {
                console.log('Rendering record:', record, 'keys:', Object.keys(record));
                return (
                <tr key={record.id || record.ID || index}>
                  {displayColumns.map((column) => {
                    const value = record[column.name] || record[column.name.toUpperCase()];
                    console.log(`Column ${column.name}: trying keys [${column.name}, ${column.name.toUpperCase()}] in record, got value:`, value);
                    return (
                    <td 
                      key={column.name} 
                      title={String(value || '')}
                      style={{ textAlign: column.align as any }}
                    >
                      {formatValue(value, column)}
                    </td>
                    );
                  })}
                  <td className="actions-column">
                    <button
                      onClick={() => {
                        console.log('=== Edit Button Clicked ===');
                        console.log('Record data:', record);
                        console.log('Record keys:', Object.keys(record));
                        console.log('Record values:', Object.values(record));
                        console.log('============================');
                        onEdit(record);
                      }}
                      className="edit-button"
                      title="編集"
                    >
                      ✏️
                    </button>
                    <button
                      onClick={() => handleDelete(record)}
                      className="delete-button"
                      title="削除"
                    >
                      🗑️
                    </button>
                  </td>
                </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      <div className="table-footer">
        <div className="record-count">
          {searchQuery && (
            <span className="search-results">
              {filteredRecords.length} / 
            </span>
          )}
          <span className="total-count">
            合計 {records.length} 件
          </span>
        </div>
      </div>
    </div>
  );
};

export default TableList;