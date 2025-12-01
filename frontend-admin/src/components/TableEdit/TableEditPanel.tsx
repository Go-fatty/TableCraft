import { useState, useEffect } from 'react';
import './TableEditPanel.css';
import FieldModal from './FieldModal';
import ValidationModal from './ValidationModal';

interface Field {
  id: string;
  name: string;
  displayName: string;
  type: string;
  required: boolean;
  editable: boolean;
  visible: boolean;
}

interface Validation {
  id: string;
  fieldName: string;
  type: string;
  rule: string;
  errorMessage: string;
}

interface TableInfo {
  id: string;
  name: string;
  displayName: string;
  description: string;
  columns?: any[];  // バックエンドから取得したカラム情報
  enableSearch: boolean;
  enableSort: boolean;
  enablePagination: boolean;
  pageSize: number;
  allowCreate: boolean;
  allowEdit: boolean;
  allowDelete: boolean;
  allowBulk: boolean;
}

interface TableEditPanelProps {
  tableId: string;
  tableName: string;
  tableDisplayName: string;
  tableInfo: TableInfo;
  onDeleteTable: (tableId: string) => void;
  onSave: (tableInfo: TableInfo) => void;
}

const TableEditPanel = ({ tableId, tableName, tableDisplayName, tableInfo, onDeleteTable, onSave }: TableEditPanelProps) => {
  const [isFieldModalOpen, setIsFieldModalOpen] = useState(false);
  const [isValidationModalOpen, setIsValidationModalOpen] = useState(false);
  const [editingField, setEditingField] = useState<Field | null>(null);
  const [editingValidation, setEditingValidation] = useState<Validation | null>(null);
  const [activeTab, setActiveTab] = useState<'table' | 'fields' | 'validations'>('table');
  const [tableSettings, setTableSettings] = useState<TableInfo>(tableInfo);
  const [hasChanges, setHasChanges] = useState(false);

  // バックエンドから取得したカラム情報をフィールドに変換
  const initialFields: Field[] = (tableInfo.columns || []).map((col: any, index: number) => ({
    id: String(index + 1),
    name: col.name,
    displayName: col.comment || col.name,
    type: col.type,
    required: !col.nullable,
    editable: !col.primary && !col.autoIncrement,
    visible: col.visible !== undefined ? col.visible : true,
  }));

  const [fields, setFields] = useState<Field[]>(initialFields);

  // tableInfoが変更されたときにfieldsを更新
  useEffect(() => {
    const newFields: Field[] = (tableInfo.columns || []).map((col: any, index: number) => ({
      id: String(index + 1),
      name: col.name,
      displayName: col.comment || col.name,
      type: col.type,
      required: !col.nullable,
      editable: !col.primary && !col.autoIncrement,
      visible: col.visible !== undefined ? col.visible : true,
    }));
    setFields(newFields);
  }, [tableInfo.columns]);

  // 一括保存イベントリスナー
  useEffect(() => {
    const handleSaveAll = () => {
      console.log('[TableEditPanel] save-all-configs イベント受信');
      
      // fieldsをcolumns形式に変換（バックエンドのColumnRequest形式に合わせる）
      const updatedColumns = fields.map(field => ({
        name: field.name,
        type: field.type,
        nullable: !field.required,
        primary: field.name.toLowerCase() === 'id',
        autoIncrement: field.name.toLowerCase() === 'id',
        defaultValue: null,
        comment: field.displayName,
        visible: field.visible,
        sortable: true,
        filterable: true
      }));

      const updatedTableInfo = {
        ...tableInfo,
        columns: updatedColumns,
      };

      console.log('[TableEditPanel] テーブル保存:', updatedTableInfo);
      onSave(updatedTableInfo);
    };

    window.addEventListener('save-all-configs', handleSaveAll);
    return () => window.removeEventListener('save-all-configs', handleSaveAll);
  }, [fields, tableInfo, onSave]);

  // バリデーション設定（今後バックエンドから取得予定）
  const [validations, setValidations] = useState<Validation[]>([]);

  const handleAddField = () => {
    setEditingField(null);
    setIsFieldModalOpen(true);
  };

  const handleEditField = (field: Field) => {
    setEditingField(field);
    setIsFieldModalOpen(true);
  };

  const handleDeleteField = (id: string) => {
    if (confirm('このフィールドを削除しますか？')) {
      setFields(fields.filter((f) => f.id !== id));
    }
  };

  const handleSaveField = (field: Field) => {
    if (editingField) {
      setFields(fields.map((f) => (f.id === field.id ? field : f)));
    } else {
      setFields([...fields, { ...field, id: `${Date.now()}` }]);
    }
    setIsFieldModalOpen(false);
  };

  const handleAddValidation = () => {
    setEditingValidation(null);
    setIsValidationModalOpen(true);
  };

  const handleEditValidation = (validation: Validation) => {
    setEditingValidation(validation);
    setIsValidationModalOpen(true);
  };

  const handleDeleteValidation = (id: string) => {
    if (confirm('このバリデーションを削除しますか？')) {
      setValidations(validations.filter((v) => v.id !== id));
    }
  };

  const handleSaveValidation = (validation: Validation) => {
    if (editingValidation) {
      setValidations(validations.map((v) => (v.id === validation.id ? validation : v)));
    } else {
      setValidations([...validations, { ...validation, id: `${Date.now()}` }]);
    }
    setIsValidationModalOpen(false);
  };

  const handleTableSettingChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    
    setTableSettings({
      ...tableSettings,
      [name]: type === 'checkbox' ? checked : type === 'number' ? Number(value) : value,
    });
    setHasChanges(true);
  };

  const handleDeleteTable = () => {
    if (confirm(`テーブル「${tableDisplayName}」を削除しますか？\n\nこの操作は取り消せません。`)) {
      onDeleteTable(tableId);
    }
  };

  const handleToggleVisible = (fieldId: string) => {
    setFields(fields.map(f => 
      f.id === fieldId ? { ...f, visible: !f.visible } : f
    ));
    setHasChanges(true);
  };



  return (
    <div className="panel">
      <div className="panel-header-with-actions">
        <div>
          <h2 className="panel-title">
            {tableDisplayName} ({tableName})
          </h2>
          <p className="panel-subtitle">テーブルのフィールドとバリデーションを設定します</p>
        </div>
        <div className="panel-header-actions">
          <button className="btn btn-danger" onClick={handleDeleteTable}>
            🗑️ テーブル削除
          </button>
        </div>
      </div>

      <div className="panel-tabs">
        <button
          className={`panel-tab ${activeTab === 'table' ? 'active' : ''}`}
          onClick={() => setActiveTab('table')}
        >
          ⚙️ テーブル設定
        </button>
        <button
          className={`panel-tab ${activeTab === 'fields' ? 'active' : ''}`}
          onClick={() => setActiveTab('fields')}
        >
          🔧 フィールド設定 ({fields.length})
        </button>
        <button
          className={`panel-tab ${activeTab === 'validations' ? 'active' : ''}`}
          onClick={() => setActiveTab('validations')}
        >
          ✓ バリデーション設定 ({validations.length})
        </button>
      </div>

      {activeTab === 'table' ? (
        <div className="panel-section">
          <div className="section-header">
            <h3 className="section-title">テーブル基本設定</h3>
          </div>
          {hasChanges && (
            <div className="info-box" style={{ marginBottom: '16px' }}>
              <p>💡 変更があります。画面上部の「一括保存」ボタンで保存してください</p>
            </div>
          )}

          <div className="table-settings-form">
            <div className="form-section">
              <h4 className="form-section-title">基本情報</h4>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">テーブル名</label>
                  <input
                    type="text"
                    className="form-input"
                    value={tableSettings.name}
                    disabled
                  />
                  <span className="help-text">テーブル名は変更できません</span>
                </div>

                <div className="form-group">
                  <label className="form-label">表示名</label>
                  <input
                    type="text"
                    name="displayName"
                    className="form-input"
                    value={tableSettings.displayName}
                    onChange={handleTableSettingChange}
                  />
                </div>

                <div className="form-group form-group-full">
                  <label className="form-label">説明</label>
                  <textarea
                    name="description"
                    className="form-textarea"
                    value={tableSettings.description}
                    onChange={handleTableSettingChange}
                    rows={3}
                  />
                </div>
              </div>
            </div>

            <div className="form-section">
              <h4 className="form-section-title">表示機能</h4>
              <div className="checkbox-list">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="enableSearch"
                    className="form-checkbox"
                    checked={tableSettings.enableSearch}
                    onChange={handleTableSettingChange}
                  />
                  <div className="checkbox-content">
                    <span className="checkbox-text">検索機能を有効化</span>
                    <span className="checkbox-description">テーブル上部に検索ボックスを表示</span>
                  </div>
                </label>

                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="enableSort"
                    className="form-checkbox"
                    checked={tableSettings.enableSort}
                    onChange={handleTableSettingChange}
                  />
                  <div className="checkbox-content">
                    <span className="checkbox-text">ソート機能を有効化</span>
                    <span className="checkbox-description">カラムヘッダーをクリックして並び替え</span>
                  </div>
                </label>

                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="enablePagination"
                    className="form-checkbox"
                    checked={tableSettings.enablePagination}
                    onChange={handleTableSettingChange}
                  />
                  <div className="checkbox-content">
                    <span className="checkbox-text">ページネーションを有効化</span>
                    <span className="checkbox-description">データを複数ページに分割して表示</span>
                  </div>
                </label>
              </div>

              {tableSettings.enablePagination && (
                <div className="form-group" style={{ marginTop: '16px' }}>
                  <label className="form-label">1ページあたりの表示件数</label>
                  <input
                    type="number"
                    name="pageSize"
                    className="form-input"
                    value={tableSettings.pageSize}
                    onChange={handleTableSettingChange}
                    min="1"
                    max="1000"
                  />
                  <span className="help-text">推奨: 10〜100件</span>
                </div>
              )}
            </div>

            <div className="form-section">
              <h4 className="form-section-title">操作権限</h4>
              <div className="checkbox-list">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="allowCreate"
                    className="form-checkbox"
                    checked={tableSettings.allowCreate}
                    onChange={handleTableSettingChange}
                  />
                  <div className="checkbox-content">
                    <span className="checkbox-text">新規作成を許可</span>
                    <span className="checkbox-description">「新規作成」ボタンを表示</span>
                  </div>
                </label>

                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="allowEdit"
                    className="form-checkbox"
                    checked={tableSettings.allowEdit}
                    onChange={handleTableSettingChange}
                  />
                  <div className="checkbox-content">
                    <span className="checkbox-text">編集を許可</span>
                    <span className="checkbox-description">「編集」ボタンを表示</span>
                  </div>
                </label>

                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="allowDelete"
                    className="form-checkbox"
                    checked={tableSettings.allowDelete}
                    onChange={handleTableSettingChange}
                  />
                  <div className="checkbox-content">
                    <span className="checkbox-text">削除を許可</span>
                    <span className="checkbox-description">「削除」ボタンを表示</span>
                  </div>
                </label>

                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="allowBulk"
                    className="form-checkbox"
                    checked={tableSettings.allowBulk}
                    onChange={handleTableSettingChange}
                  />
                  <div className="checkbox-content">
                    <span className="checkbox-text">一括操作を許可</span>
                    <span className="checkbox-description">複数選択して一括編集・削除</span>
                  </div>
                </label>
              </div>
            </div>
          </div>
        </div>
      ) : activeTab === 'fields' ? (
        <div className="panel-section">
          <div className="section-header">
            <h3 className="section-title">フィールド一覧</h3>
            <button className="btn btn-primary" onClick={handleAddField}>
              ➕ フィールド追加
            </button>
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>フィールド名</th>
                  <th>表示名</th>
                  <th>データ型</th>
                  <th>必須</th>
                  <th>編集可能</th>
                  <th>表示</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {fields.map((field) => (
                  <tr key={field.id}>
                    <td>
                      <code className="field-code">{field.name}</code>
                    </td>
                    <td>{field.displayName}</td>
                    <td>
                      <span className="type-badge">{field.type}</span>
                    </td>
                    <td>
                      <span className={`bool-badge ${field.required ? 'true' : 'false'}`}>
                        {field.required ? '✓' : '✗'}
                      </span>
                    </td>
                    <td>
                      <span className={`bool-badge ${field.editable ? 'true' : 'false'}`}>
                        {field.editable ? '✓' : '✗'}
                      </span>
                    </td>
                    <td>
                      <input
                        type="checkbox"
                        checked={field.visible}
                        onChange={() => handleToggleVisible(field.id)}
                        style={{ cursor: 'pointer', width: '18px', height: '18px' }}
                      />
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button
                          className="btn-icon-small btn-primary-icon"
                          onClick={() => handleEditField(field)}
                          title="編集"
                        >
                          ✏️
                        </button>
                        <button
                          className="btn-icon-small btn-danger-icon"
                          onClick={() => handleDeleteField(field.id)}
                          title="削除"
                        >
                          🗑️
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="panel-section">
          <div className="section-header">
            <h3 className="section-title">バリデーション一覧</h3>
            <button className="btn btn-primary" onClick={handleAddValidation}>
              ➕ バリデーション追加
            </button>
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>対象フィールド</th>
                  <th>バリデーション種別</th>
                  <th>ルール</th>
                  <th>エラーメッセージ</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {validations.map((validation) => (
                  <tr key={validation.id}>
                    <td>
                      <code className="field-code">{validation.fieldName}</code>
                    </td>
                    <td>
                      <span className="type-badge">{validation.type}</span>
                    </td>
                    <td>
                      <code className="rule-code">{validation.rule}</code>
                    </td>
                    <td className="error-message-cell">{validation.errorMessage}</td>
                    <td>
                      <div className="action-buttons">
                        <button
                          className="btn-icon-small btn-primary-icon"
                          onClick={() => handleEditValidation(validation)}
                          title="編集"
                        >
                          ✏️
                        </button>
                        <button
                          className="btn-icon-small btn-danger-icon"
                          onClick={() => handleDeleteValidation(validation.id)}
                          title="削除"
                        >
                          🗑️
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <FieldModal
        isOpen={isFieldModalOpen}
        onClose={() => setIsFieldModalOpen(false)}
        onSave={handleSaveField}
        field={editingField}
        existingFields={fields}
      />

      <ValidationModal
        isOpen={isValidationModalOpen}
        onClose={() => setIsValidationModalOpen(false)}
        onSave={handleSaveValidation}
        validation={editingValidation}
        availableFields={fields}
      />
    </div>
  );
};

export default TableEditPanel;