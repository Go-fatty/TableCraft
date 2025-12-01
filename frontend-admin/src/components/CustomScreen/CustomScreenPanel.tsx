import { useState } from 'react';
import './CustomScreenPanel.css';
import CustomScreenFieldModal from './CustomScreenFieldModal';

interface CustomScreenField {
  id: string;
  sourceTable: string;
  sourceField: string;
  displayName: string;
  required: boolean;
  editable: boolean;
  visible: boolean;
  order: number;
}

interface TableInfo {
  id: string;
  name: string;
  displayName: string;
}

interface CustomScreenInfo {
  id: string;
  name: string;
  displayName: string;
  description: string;
  targetTables: string[];
}

interface Script {
  id: string;
  scriptName: string;
  scriptType: string;
  description: string;
}

interface CustomScreenPanelProps {
  screenId: string;
  screenName: string;
  screenDisplayName: string;
  screenInfo: CustomScreenInfo;
  onDeleteScreen: (screenId: string) => void;
  availableTables: TableInfo[];
  availableScripts: Script[];
}

const CustomScreenPanel = ({
  screenId,
  screenName,
  screenDisplayName,
  screenInfo,
  onDeleteScreen,
  availableTables,
  availableScripts,
}: CustomScreenPanelProps) => {
  const [activeTab, setActiveTab] = useState<'screen' | 'fields'>('screen');
  const [screenSettings, setScreenSettings] = useState<CustomScreenInfo>(screenInfo);
  const [hasChanges, setHasChanges] = useState(false);
  const [isFieldModalOpen, setIsFieldModalOpen] = useState(false);
  const [editingField, setEditingField] = useState<CustomScreenField | null>(null);

  // カスタム画面フィールド（今後バックエンドから取得予定）
  const [fields, setFields] = useState<CustomScreenField[]>([]);

  const handleScreenSettingChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setScreenSettings({
      ...screenSettings,
      [name]: value,
    });
    setHasChanges(true);
  };

  const handleTargetTablesChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selected = Array.from(e.target.selectedOptions).map((option) => option.value);
    setScreenSettings({
      ...screenSettings,
      targetTables: selected,
    });
    setHasChanges(true);
  };

  const handleDeleteScreen = () => {
    if (
      confirm(
        `カスタム画面「${screenDisplayName}」を削除しますか？\n\nこの操作は取り消せません。`
      )
    ) {
      onDeleteScreen(screenId);
    }
  };

  const handleAddField = () => {
    setEditingField(null);
    setIsFieldModalOpen(true);
  };

  const handleEditField = (field: CustomScreenField) => {
    setEditingField(field);
    setIsFieldModalOpen(true);
  };

  const handleDeleteField = (id: string) => {
    if (confirm('このフィールドを削除しますか？')) {
      setFields(fields.filter((f) => f.id !== id));
    }
  };

  const handleSaveField = (field: CustomScreenField) => {
    if (editingField) {
      setFields(fields.map((f) => (f.id === field.id ? field : f)));
    } else {
      const maxOrder = fields.length > 0 ? Math.max(...fields.map((f) => f.order)) : 0;
      setFields([...fields, { ...field, id: `${Date.now()}`, order: maxOrder + 1 }]);
    }
    setIsFieldModalOpen(false);
  };



  return (
    <div className="panel">
      <div className="panel-header-with-actions">
        <div>
          <h2 className="panel-title">
            {screenDisplayName} ({screenName})
          </h2>
          <p className="panel-subtitle">複数テーブルを組み合わせたカスタム画面を設定します</p>
        </div>
        <div className="panel-header-actions">
          <button className="btn btn-danger" onClick={handleDeleteScreen}>
            🗑️ 画面削除
          </button>
        </div>
      </div>

      <div className="panel-tabs">
        <button
          className={`panel-tab ${activeTab === 'screen' ? 'active' : ''}`}
          onClick={() => setActiveTab('screen')}
        >
          ⚙️ 画面設定
        </button>
        <button
          className={`panel-tab ${activeTab === 'fields' ? 'active' : ''}`}
          onClick={() => setActiveTab('fields')}
        >
          🔧 フィールド設定 ({fields.length})
        </button>
      </div>

      {activeTab === 'screen' ? (
        <div className="panel-section">
          <div className="section-header">
            <h3 className="section-title">カスタム画面基本情報</h3>
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
                  <label className="form-label">画面名</label>
                  <input
                    type="text"
                    className="form-input"
                    value={screenSettings.name}
                    disabled
                  />
                  <span className="help-text">画面名は変更できません</span>
                </div>

                <div className="form-group">
                  <label className="form-label">表示名</label>
                  <input
                    type="text"
                    name="displayName"
                    className="form-input"
                    value={screenSettings.displayName}
                    onChange={handleScreenSettingChange}
                  />
                </div>

                <div className="form-group form-group-full">
                  <label className="form-label">説明</label>
                  <textarea
                    name="description"
                    className="form-textarea"
                    value={screenSettings.description}
                    onChange={handleScreenSettingChange}
                    rows={3}
                  />
                </div>
              </div>
            </div>

            <div className="form-section">
              <h4 className="form-section-title">対象テーブル</h4>
              <div className="form-group">
                <label className="form-label">
                  この画面で使用するテーブル（複数選択可）
                </label>
                <select
                  multiple
                  className="form-select-multiple"
                  value={screenSettings.targetTables}
                  onChange={handleTargetTablesChange}
                  size={8}
                >
                  {availableTables.map((table) => (
                    <option key={table.id} value={table.id}>
                      {table.displayName} ({table.name})
                    </option>
                  ))}
                </select>
                <span className="help-text">
                  Ctrl/Cmd キーを押しながらクリックで複数選択
                </span>
              </div>

              <div className="selected-tables-display">
                <h5 className="selected-tables-title">選択中のテーブル:</h5>
                {screenSettings.targetTables.length > 0 ? (
                  <div className="selected-tables-list">
                    {screenSettings.targetTables.map((tableId) => {
                      const table = availableTables.find((t) => t.id === tableId);
                      return table ? (
                        <span key={tableId} className="selected-table-badge">
                          📋 {table.displayName}
                        </span>
                      ) : null;
                    })}
                  </div>
                ) : (
                  <p className="no-selection">テーブルが選択されていません</p>
                )}
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className="panel-section">
          <div className="section-header">
            <h3 className="section-title">フィールド一覧</h3>
            <button className="btn btn-primary" onClick={handleAddField}>
              ➕ フィールド追加
            </button>
          </div>

          {screenSettings.targetTables.length === 0 ? (
            <div className="empty-state">
              <p className="empty-state-icon">📋</p>
              <p className="empty-state-title">対象テーブルを選択してください</p>
              <p className="empty-state-description">
                フィールドを追加する前に、画面設定タブで対象テーブルを選択してください。
              </p>
            </div>
          ) : (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>順序</th>
                    <th>元テーブル</th>
                    <th>元フィールド</th>
                    <th>表示名</th>
                    <th>必須</th>
                    <th>編集可能</th>
                    <th>表示</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {fields
                    .sort((a, b) => a.order - b.order)
                    .map((field) => {
                      const sourceTable = availableTables.find((t) => t.id === field.sourceTable);
                      return (
                        <tr key={field.id}>
                          <td>
                            <span className="order-badge">{field.order}</span>
                          </td>
                          <td>
                            <span className="table-badge">
                              {sourceTable?.displayName || field.sourceTable}
                            </span>
                          </td>
                          <td>
                            <code className="field-code">{field.sourceField}</code>
                          </td>
                          <td>{field.displayName}</td>
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
                            <span className={`bool-badge ${field.visible ? 'true' : 'false'}`}>
                              {field.visible ? '✓' : '✗'}
                            </span>
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
                      );
                    })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      <CustomScreenFieldModal
        isOpen={isFieldModalOpen}
        onClose={() => setIsFieldModalOpen(false)}
        onSave={handleSaveField}
        field={editingField}
        existingFields={fields}
        targetTables={screenSettings.targetTables}
        availableTables={availableTables}
        availableScripts={availableScripts}
      />
    </div>
  );
};

export default CustomScreenPanel;
