import { useState, useEffect } from 'react';
import type { ColumnDefinition, TableDefinition, TableCreationRequest, TableTemplate } from '../../api/adminApi';
import { getTableTemplates } from '../../api/adminApi';
import ColumnForm from './ColumnForm';
import './TableEditorModal.css';

interface TableEditorModalProps {
  table?: TableDefinition;
  showTemplateSelector?: boolean;
  onSave: (request: TableCreationRequest) => Promise<void>;
  onCancel: () => void;
}

const TableEditorModal = ({ table, showTemplateSelector = false, onSave, onCancel }: TableEditorModalProps) => {
  const [tableName, setTableName] = useState(table?.tableName || '');
  const [displayName, setDisplayName] = useState(table?.displayName || '');
  const [columns, setColumns] = useState<ColumnDefinition[]>(table?.columns || []);
  const [showColumnForm, setShowColumnForm] = useState(false);
  const [editingColumnIndex, setEditingColumnIndex] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [templates, setTemplates] = useState<Record<string, TableTemplate>>({});
  const [selectedTemplate, setSelectedTemplate] = useState<string>('');

  useEffect(() => {
    loadTemplates();
  }, []);

  const loadTemplates = async () => {
    try {
      const response = await getTableTemplates();
      if (response.success && response.data) {
        // response.dataがtemplatesプロパティを持っている場合と、直接テンプレートオブジェクトの場合の両方に対応
        const templatesData = response.data.templates || response.data;
        setTemplates(templatesData);
      }
    } catch (error) {
      console.error('テンプレートの取得に失敗しました:', error);
    }
  };

  const handleTemplateChange = (templateKey: string) => {
    setSelectedTemplate(templateKey);
    if (templateKey && templates[templateKey]) {
      const template = templates[templateKey];
      console.log('🔍 Selected template:', templateKey);
      console.log('🔍 Template columns:', template.columns);
      setColumns(template.columns);
      if (!displayName && template.name) {
        setDisplayName(template.name);
      }
    }
  };

  const handleAddColumn = (column: ColumnDefinition) => {
    if (editingColumnIndex !== null) {
      // 編集モード
      const updatedColumns = [...columns];
      updatedColumns[editingColumnIndex] = column;
      setColumns(updatedColumns);
      setEditingColumnIndex(null);
    } else {
      // 新規追加
      setColumns([...columns, column]);
    }
    setShowColumnForm(false);
  };

  const handleEditColumn = (index: number) => {
    setEditingColumnIndex(index);
    setShowColumnForm(true);
  };

  const handleDeleteColumn = (index: number) => {
    if (window.confirm('このカラムを削除してもよろしいですか?')) {
      setColumns(columns.filter((_, i) => i !== index));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!tableName.trim()) {
      alert('テーブル名を入力してください');
      return;
    }

    if (columns.length === 0) {
      alert('少なくとも1つのカラムを追加してください');
      return;
    }

    const hasPrimaryKey = columns.some((col) => col.primaryKey);
    if (!hasPrimaryKey) {
      alert('PRIMARY KEYを設定したカラムを追加してください');
      return;
    }

    setSaving(true);
    try {
      await onSave({
        tableName: tableName.trim(),
        displayName: displayName.trim() || tableName.trim(),
        columns,
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="table-editor-overlay">
      <div className="table-editor-modal">
        <h2>{table ? 'テーブル編集' : '新規テーブル作成'}</h2>

        <form onSubmit={handleSubmit}>
          <div className="editor-section">
            <h3>基本情報</h3>
            
            {showTemplateSelector && Object.keys(templates).length > 0 && (
              <div className="form-row">
                <label>
                  テンプレートを選択
                  <select
                    value={selectedTemplate}
                    onChange={(e) => handleTemplateChange(e.target.value)}
                    className="template-select"
                  >
                    <option value="">-- テンプレートを選択 --</option>
                    {Object.entries(templates).map(([key, template]) => (
                      <option key={key} value={key}>
                        {template.name} - {template.description}
                      </option>
                    ))}
                  </select>
                </label>
                <small>※テンプレートを選択すると、カラム定義が自動的に入力されます</small>
              </div>
            )}
            
            <div className="form-row">
              <label>
                テーブル名 <span className="required">*</span>
                <input
                  type="text"
                  value={tableName}
                  onChange={(e) => setTableName(e.target.value)}
                  placeholder="例: users"
                  required
                  disabled={!!table} // 既存テーブルは名前変更不可
                />
                {table && <small>※既存テーブルの名前は変更できません</small>}
              </label>
            </div>

            <div className="form-row">
              <label>
                表示名
                <input
                  type="text"
                  value={displayName}
                  onChange={(e) => setDisplayName(e.target.value)}
                  placeholder="例: ユーザー"
                />
              </label>
            </div>
          </div>

          <div className="editor-section">
            <div className="section-header">
              <h3>カラム定義</h3>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={() => {
                  setEditingColumnIndex(null);
                  setShowColumnForm(true);
                }}
              >
                + カラム追加
              </button>
            </div>

            {columns.length === 0 ? (
              <p className="empty-message">カラムが追加されていません。「+ カラム追加」ボタンをクリックしてカラムを追加してください。</p>
            ) : (
              <div className="columns-table">
                <table>
                  <thead>
                    <tr>
                      <th>カラム名</th>
                      <th>データ型</th>
                      <th>NULL</th>
                      <th>PK</th>
                      <th>AI</th>
                      <th>デフォルト値</th>
                      <th>コメント</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    {columns.map((col, index) => (
                      <tr key={index}>
                        <td className="col-name">{col.columnName}</td>
                        <td>{col.dataType}</td>
                        <td>{col.nullable ? '○' : '×'}</td>
                        <td>{col.primaryKey ? '✓' : ''}</td>
                        <td>{col.autoIncrement ? '✓' : ''}</td>
                        <td className="col-default">{col.defaultValue || '-'}</td>
                        <td className="col-comment">{col.comment || '-'}</td>
                        <td className="col-actions">
                          <button
                            type="button"
                            className="btn-icon"
                            onClick={() => handleEditColumn(index)}
                            title="編集"
                          >
                            ✏️
                          </button>
                          <button
                            type="button"
                            className="btn-icon btn-danger"
                            onClick={() => handleDeleteColumn(index)}
                            title="削除"
                          >
                            🗑️
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={saving}>
              キャンセル
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? '保存中...' : '保存'}
            </button>
          </div>
        </form>

        {showColumnForm && (
          <ColumnForm
            existingColumn={editingColumnIndex !== null ? columns[editingColumnIndex] : undefined}
            onAdd={handleAddColumn}
            onCancel={() => {
              setShowColumnForm(false);
              setEditingColumnIndex(null);
            }}
          />
        )}
      </div>
    </div>
  );
};

export default TableEditorModal;
