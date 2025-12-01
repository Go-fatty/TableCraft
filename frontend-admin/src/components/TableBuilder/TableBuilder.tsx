import { useState, useEffect } from 'react';
import type { ManualTableDefinition, TableDefinitionRequest } from '../../api/adminApi';
import {
  createTable,
  listTables,
  updateTable,
  deleteTable,
} from '../../api/adminApi';
import TableEditorModal from './TableEditorModal';
import './TableBuilder.css';

interface TableBuilderProps {
  defaultShowTemplateSelector?: boolean;
  onTableSaved?: () => void;
  onCancel?: () => void;
}

const TableBuilder = ({ defaultShowTemplateSelector = false, onTableSaved, onCancel }: TableBuilderProps) => {
  const [tables, setTables] = useState<ManualTableDefinition[]>([]);
  const [loading, setLoading] = useState(true);
  const [showEditor, setShowEditor] = useState(defaultShowTemplateSelector);
  const [editingTable, setEditingTable] = useState<ManualTableDefinition | undefined>(undefined);
  const [showTemplateSelector, setShowTemplateSelector] = useState(defaultShowTemplateSelector);

  useEffect(() => {
    loadTables();
  }, []);

  const loadTables = async () => {
    setLoading(true);
    try {
      const response = await listTables();
      setTables(response.data || []);
    } catch (error) {
      console.error('テーブル一覧の取得に失敗しました:', error);
      alert('テーブル一覧の取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTable = async (request: TableDefinitionRequest) => {
    try {
      const response = await createTable(request);
      
      if (!response.success) {
        alert('エラー: ' + (response.error || 'テーブルの作成に失敗しました'));
        throw new Error(response.error || 'Failed to create table');
      }
      
      alert('テーブルを作成しました');
      setShowEditor(false);
      setShowTemplateSelector(false);
      loadTables();
      if (onTableSaved) {
        onTableSaved();
      }
    } catch (error: any) {
      console.error('テーブルの作成に失敗しました:', error);
      
      let errorMessage = 'テーブルの作成に失敗しました';
      if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      if (errorMessage.includes('already exists')) {
        alert('エラー: 同じ名前のテーブルが既に存在します。\n別のテーブル名を使用してください。');
      } else {
        alert('エラー: ' + errorMessage);
      }
      
      throw error;
    }
  };

  const handleUpdateTable = async (request: TableDefinitionRequest) => {
    if (!editingTable?.id) return;

    try {
      await updateTable(editingTable.id, request);
      alert('テーブルを更新しました');
      setShowEditor(false);
      setEditingTable(undefined);
      loadTables();
      if (onTableSaved) {
        onTableSaved();
      }
    } catch (error) {
      console.error('テーブルの更新に失敗しました:', error);
      alert('テーブルの更新に失敗しました');
      throw error;
    }
  };

  const handleDeleteTable = async (table: ManualTableDefinition) => {
    if (!window.confirm(`テーブル「${table.displayName || table.tableName}」を削除してもよろしいですか?\n\n⚠️ この操作は取り消せません。`)) {
      return;
    }

    if (!table.id) {
      alert('テーブルIDが不正です');
      return;
    }

    try {
      await deleteTable(table.id);
      alert('テーブルを削除しました');
      loadTables();
    } catch (error) {
      console.error('テーブルの削除に失敗しました:', error);
      alert('テーブルの削除に失敗しました');
    }
  };

  const handleEdit = (table: ManualTableDefinition) => {
    setEditingTable(table);
    setShowEditor(true);
  };

  const handleNewTable = () => {
    setEditingTable(undefined);
    setShowTemplateSelector(false);
    setShowEditor(true);
  };

  const handleNewFromTemplate = () => {
    setEditingTable(undefined);
    setShowTemplateSelector(true);
    setShowEditor(true);
  };

  return (
    <div className="table-builder">
      <div className="builder-header">
        <h2>テーブルビルダー</h2>
        <div className="header-actions">
          <button className="btn btn-primary" onClick={handleNewTable}>
            + 新規テーブル作成
          </button>
          <button className="btn btn-secondary" onClick={handleNewFromTemplate}>
            📋 テンプレートから作成
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading-message">読み込み中...</div>
      ) : tables.length === 0 ? (
        <div className="empty-state">
          <p>テーブルが登録されていません。</p>
          <p>「+ 新規テーブル作成」ボタンをクリックして、最初のテーブルを作成してください。</p>
        </div>
      ) : (
        <div className="tables-grid">
          {tables.map((table) => (
            <div key={table.id} className="table-card">
              <div className="card-header">
                <h3>{table.displayName || table.tableName}</h3>
                <div className="card-actions">
                  <button
                    className="btn-icon"
                    onClick={() => handleEdit(table)}
                    title="編集"
                  >
                    ✏️
                  </button>
                  <button
                    className="btn-icon btn-danger"
                    onClick={() => handleDeleteTable(table)}
                    title="削除"
                  >
                    🗑️
                  </button>
                </div>
              </div>

              <div className="card-body">
                <div className="table-info">
                  <span className="info-label">テーブル名:</span>
                  <span className="info-value">{table.tableName}</span>
                </div>

                <div className="columns-summary">
                  <span className="info-label">カラム数:</span>
                  <span className="info-value">{table.columns.length}</span>
                </div>

                <div className="columns-list">
                  {table.columns.map((col, index) => (
                    <div key={index} className="column-item">
                      <span className="column-name">{col.name}</span>
                      <span className="column-type">{col.type}</span>
                      {col.primary && <span className="badge badge-primary">PK</span>}
                      {col.autoIncrement && <span className="badge badge-info">AI</span>}
                    </div>
                  ))}
                </div>

                <div className="card-footer">
                  {table.createdAt && (
                    <small>作成日: {new Date(table.createdAt).toLocaleString('ja-JP')}</small>
                  )}
                  {table.updatedAt && (
                    <small>更新日: {new Date(table.updatedAt).toLocaleString('ja-JP')}</small>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {showEditor && (
        <TableEditorModal
          table={editingTable}
          showTemplateSelector={showTemplateSelector}
          onSave={editingTable ? handleUpdateTable : handleCreateTable}
          onCancel={() => {
            setShowEditor(false);
            setEditingTable(undefined);
            setShowTemplateSelector(false);
            if (defaultShowTemplateSelector && onCancel) {
              onCancel();
            }
          }}
        />
      )}
    </div>
  );
};

export default TableBuilder;
