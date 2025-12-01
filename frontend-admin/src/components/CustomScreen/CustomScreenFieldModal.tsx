import { useState, useEffect } from 'react';
import '../TableEdit/FieldModal.css';

interface CustomScreenField {
  id: string;
  fieldType?: 'data' | 'button';
  sourceTable: string;
  sourceField: string;
  displayName: string;
  required: boolean;
  editable: boolean;
  visible: boolean;
  order: number;
  buttonAction?: {
    scriptId: string;
    actionType: 'onClick' | 'onSave' | 'onLoad';
    params: Record<string, any>;
  };
}

interface TableInfo {
  id: string;
  name: string;
  displayName: string;
}

interface TableField {
  name: string;
  displayName: string;
  type: string;
}

interface Script {
  id: string;
  scriptName: string;
  scriptType: string;
  description: string;
}

interface CustomScreenFieldModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (field: CustomScreenField) => void;
  field: CustomScreenField | null;
  existingFields: CustomScreenField[];
  targetTables: string[];
  availableTables: TableInfo[];
  availableScripts: Script[];
}

const CustomScreenFieldModal = ({
  isOpen,
  onClose,
  onSave,
  field,
  existingFields,
  targetTables,
  availableTables,
  availableScripts,
}: CustomScreenFieldModalProps) => {
  const [formData, setFormData] = useState<CustomScreenField>({
    id: '',
    fieldType: 'data',
    sourceTable: '',
    sourceField: '',
    displayName: '',
    required: false,
    editable: true,
    visible: true,
    order: 0,
    buttonAction: undefined,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [availableFields, setAvailableFields] = useState<TableField[]>([]);

  const loadFieldsForTable = async (tableId: string) => {
    try {
      // TODO: バックエンドからテーブルのカラム情報を取得
      // const response = await adminApi.getTable(tableId);
      // if (response.success && response.data.columns) {
      //   const fields = response.data.columns.map(col => ({
      //     name: col.name,
      //     displayName: col.comment || col.name,
      //     type: col.type
      //   }));
      //   setAvailableFields(fields);
      // }
      
      // 暫定: 空配列を設定（バックエンドAPI実装待ち）
      setAvailableFields([]);
    } catch (error) {
      console.error('フィールド情報の取得に失敗:', error);
      setAvailableFields([]);
    }
  };

  useEffect(() => {
    if (field) {
      setFormData(field);
      loadFieldsForTable(field.sourceTable);
    } else {
      setFormData({
        id: '',
        fieldType: 'data',
        sourceTable: targetTables[0] || '',
        sourceField: '',
        displayName: '',
        required: false,
        editable: true,
        visible: true,
        order: 0,
        buttonAction: undefined,
      });
      if (targetTables.length > 0) {
        loadFieldsForTable(targetTables[0]);
      }
    }
    setErrors({});
  }, [field, isOpen, targetTables]);

  if (!isOpen) return null;

  const handleTableChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const tableId = e.target.value;
    setFormData({
      ...formData,
      sourceTable: tableId,
      sourceField: '',
      displayName: '',
    });
    loadFieldsForTable(tableId);
    if (errors.sourceTable) {
      setErrors({ ...errors, sourceTable: '' });
    }
  };

  const handleFieldChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const fieldName = e.target.value;
    const selectedField = availableFields.find((f) => f.name === fieldName);
    
    setFormData({
      ...formData,
      sourceField: fieldName,
      displayName: selectedField?.displayName || fieldName,
    });
    
    if (errors.sourceField) {
      setErrors({ ...errors, sourceField: '' });
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
    if (errors[name]) {
      setErrors({ ...errors, [name]: '' });
    }
  };

  const handleFieldTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const fieldType = e.target.value as 'data' | 'button';
    setFormData({
      ...formData,
      fieldType,
      sourceField: fieldType === 'button' ? '' : formData.sourceField,
      buttonAction: fieldType === 'button' ? {
        scriptId: '',
        actionType: 'onClick',
        params: {},
      } : undefined,
    });
  };

  const handleScriptChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const scriptId = e.target.value;
    setFormData({
      ...formData,
      buttonAction: {
        ...formData.buttonAction!,
        scriptId,
      },
    });
  };

  const handleActionTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const actionType = e.target.value as 'onClick' | 'onSave' | 'onLoad';
    setFormData({
      ...formData,
      buttonAction: {
        ...formData.buttonAction!,
        actionType,
      },
    });
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.displayName.trim()) {
      newErrors.displayName = '表示名は必須です';
    }

    if (formData.fieldType === 'data') {
      if (!formData.sourceTable) {
        newErrors.sourceTable = '元テーブルを選択してください';
      }

      if (!formData.sourceField) {
        newErrors.sourceField = '元フィールドを選択してください';
      } else if (!field) {
        const duplicate = existingFields.find(
          (f) => f.sourceTable === formData.sourceTable && f.sourceField === formData.sourceField
        );
        if (duplicate) {
          newErrors.sourceField = 'このフィールドは既に追加されています';
        }
      }
    } else if (formData.fieldType === 'button') {
      if (!formData.buttonAction?.scriptId) {
        newErrors.scriptId = 'スクリプトを選択してください';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (!validate()) return;
    onSave(formData);
    onClose();
  };

  const selectedTable = availableTables.find((t) => t.id === formData.sourceTable);
  const selectedField = availableFields.find((f) => f.name === formData.sourceField);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">{field ? 'フィールド編集' : 'フィールド追加'}</h2>
          <button className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="modal-body">
          <div className="form-group">
            <label className="form-label">
              フィールドタイプ <span className="required">*</span>
            </label>
            <select
              className="form-select"
              value={formData.fieldType}
              onChange={handleFieldTypeChange}
            >
              <option value="data">データフィールド</option>
              <option value="button">ボタン</option>
            </select>
            <span className="help-text">
              {formData.fieldType === 'data' 
                ? 'テーブルのデータを表示・編集するフィールド' 
                : 'クリック時にスクリプトを実行するボタン'}
            </span>
          </div>

          {formData.fieldType === 'data' ? (
            <>
              <div className="form-group">
                <label className="form-label">
                  元テーブル <span className="required">*</span>
                </label>
                <select
                  className={`form-select ${errors.sourceTable ? 'error' : ''}`}
                  value={formData.sourceTable}
                  onChange={handleTableChange}
                  disabled={!!field}
                >
                  <option value="">-- テーブルを選択 --</option>
                  {targetTables.map((tableId) => {
                    const table = availableTables.find((t) => t.id === tableId);
                    return table ? (
                      <option key={table.id} value={table.id}>
                        {table.displayName} ({table.name})
                      </option>
                    ) : null;
                  })}
                </select>
                {errors.sourceTable && <span className="error-message">{errors.sourceTable}</span>}
                <span className="help-text">データ取得元のテーブルを選択</span>
              </div>

              <div className="form-group">
                <label className="form-label">
                  元フィールド <span className="required">*</span>
                </label>
                <select
                  className={`form-select ${errors.sourceField ? 'error' : ''}`}
                  value={formData.sourceField}
                  onChange={handleFieldChange}
                  disabled={!formData.sourceTable || !!field}
                >
                  <option value="">-- フィールドを選択 --</option>
                  {availableFields.map((f) => (
                    <option key={f.name} value={f.name}>
                      {f.displayName} ({f.name}) - {f.type}
                    </option>
                  ))}
                </select>
                {errors.sourceField && <span className="error-message">{errors.sourceField}</span>}
                <span className="help-text">
                  {formData.sourceTable
                    ? `${selectedTable?.displayName} テーブルのフィールドから選択`
                    : 'まず元テーブルを選択してください'}
                </span>
              </div>

              {selectedField && (
                <div className="field-info-box">
                  <div className="field-info-item">
                    <span className="field-info-label">データ型:</span>
                    <span className="type-badge">{selectedField.type}</span>
                  </div>
                  <div className="field-info-item">
                    <span className="field-info-label">元の表示名:</span>
                    <span>{selectedField.displayName}</span>
                  </div>
                </div>
              )}
            </>
          ) : (
            <>
              <div className="form-group">
                <label className="form-label">
                  実行スクリプト <span className="required">*</span>
                </label>
                <select
                  className={`form-select ${errors.scriptId ? 'error' : ''}`}
                  value={formData.buttonAction?.scriptId || ''}
                  onChange={handleScriptChange}
                >
                  <option value="">-- スクリプトを選択 --</option>
                  {availableScripts.map((script) => (
                    <option key={script.id} value={script.id}>
                      {script.scriptName} - {script.description}
                    </option>
                  ))}
                </select>
                {errors.scriptId && <span className="error-message">{errors.scriptId}</span>}
                <span className="help-text">ボタンクリック時に実行するスクリプト</span>
              </div>

              <div className="form-group">
                <label className="form-label">実行タイミング</label>
                <select
                  className="form-select"
                  value={formData.buttonAction?.actionType || 'onClick'}
                  onChange={handleActionTypeChange}
                >
                  <option value="onClick">クリック時</option>
                  <option value="onSave">保存時</option>
                  <option value="onLoad">読み込み時</option>
                </select>
                <span className="help-text">スクリプトを実行するタイミング</span>
              </div>

              <div className="info-box" style={{ background: '#fef3c7', borderColor: '#fcd34d', color: '#92400e' }}>
                <strong>⚠️ 注意:</strong> スクリプト実行機能は現在モックです。
                バックエンドAPI実装後に実際に動作します。
              </div>
            </>
          )}

          <div className="form-group">
            <label className="form-label">
              表示名 <span className="required">*</span>
            </label>
            <input
              type="text"
              name="displayName"
              className={`form-input ${errors.displayName ? 'error' : ''}`}
              placeholder={formData.fieldType === 'button' ? 'ボタンのラベル' : '画面に表示される名前'}
              value={formData.displayName}
              onChange={handleChange}
            />
            {errors.displayName && <span className="error-message">{errors.displayName}</span>}
            <span className="help-text">
              {formData.fieldType === 'button' 
                ? 'ボタンに表示されるラベル' 
                : 'このカスタム画面での表示名（元の表示名から変更可能）'}
            </span>
          </div>

          {formData.fieldType === 'data' && (
            <div className="checkbox-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="required"
                  checked={formData.required}
                  onChange={handleChange}
                />
                <span>必須項目</span>
              </label>

              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="editable"
                  checked={formData.editable}
                  onChange={handleChange}
                />
                <span>編集可能</span>
              </label>

              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="visible"
                  checked={formData.visible}
                  onChange={handleChange}
                />
                <span>表示</span>
              </label>
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>
            キャンセル
          </button>
          <button className="btn btn-primary" onClick={handleSave}>
            {field ? '更新' : '追加'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CustomScreenFieldModal;
