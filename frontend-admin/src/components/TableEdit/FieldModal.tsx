import { useState, useEffect } from 'react';
import './FieldModal.css';

interface Field {
  id: string;
  name: string;
  displayName: string;
  type: string;
  required: boolean;
  editable: boolean;
  visible: boolean;
}

interface FieldModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (field: Field) => void;
  field: Field | null;
  existingFields: Field[];
}

const FieldModal = ({ isOpen, onClose, onSave, field, existingFields }: FieldModalProps) => {
  const [formData, setFormData] = useState<Field>({
    id: '',
    name: '',
    displayName: '',
    type: 'VARCHAR',
    required: false,
    editable: true,
    visible: true,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (field) {
      setFormData(field);
    } else {
      setFormData({
        id: '',
        name: '',
        displayName: '',
        type: 'VARCHAR',
        required: false,
        editable: true,
        visible: true,
      });
    }
    setErrors({});
  }, [field, isOpen]);

  if (!isOpen) return null;

  const dataTypes = [
    'VARCHAR',
    'INT',
    'BIGINT',
    'DECIMAL',
    'BOOLEAN',
    'DATE',
    'DATETIME',
    'TIMESTAMP',
    'TEXT',
    'JSON',
  ];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;

    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });

    // Clear error for this field
    if (errors[name]) {
      setErrors({ ...errors, [name]: '' });
    }
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'フィールド名は必須です';
    } else if (!/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(formData.name)) {
      newErrors.name = 'フィールド名は英数字とアンダースコアのみ使用できます';
    } else if (!field) {
      // 新規作成時のみ重複チェック
      const duplicate = existingFields.find((f) => f.name === formData.name);
      if (duplicate) {
        newErrors.name = 'このフィールド名は既に使用されています';
      }
    }

    if (!formData.displayName.trim()) {
      newErrors.displayName = '表示名は必須です';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (!validate()) {
      return;
    }

    onSave(formData);
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content modal-content-medium" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">
            {field ? 'フィールド編集' : 'フィールド追加'}
          </h2>
          <button className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="modal-body">
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">
                フィールド名 <span className="required">*</span>
              </label>
              <input
                type="text"
                name="name"
                className={`form-input ${errors.name ? 'error' : ''}`}
                placeholder="例: user_name"
                value={formData.name}
                onChange={handleChange}
                disabled={!!field} // 編集時は変更不可
              />
              {errors.name && <span className="error-message">{errors.name}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">
                表示名 <span className="required">*</span>
              </label>
              <input
                type="text"
                name="displayName"
                className={`form-input ${errors.displayName ? 'error' : ''}`}
                placeholder="例: ユーザー名"
                value={formData.displayName}
                onChange={handleChange}
              />
              {errors.displayName && <span className="error-message">{errors.displayName}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">
                データ型 <span className="required">*</span>
              </label>
              <select
                name="type"
                className="form-select"
                value={formData.type}
                onChange={handleChange}
              >
                {dataTypes.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-section">
            <h4 className="form-section-title">表示・編集設定</h4>
            <div className="checkbox-grid">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="required"
                  className="form-checkbox"
                  checked={formData.required}
                  onChange={handleChange}
                />
                <span className="checkbox-text">必須項目</span>
              </label>

              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="editable"
                  className="form-checkbox"
                  checked={formData.editable}
                  onChange={handleChange}
                />
                <span className="checkbox-text">編集可能</span>
              </label>

              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="visible"
                  className="form-checkbox"
                  checked={formData.visible}
                  onChange={handleChange}
                />
                <span className="checkbox-text">一覧に表示</span>
              </label>
            </div>
          </div>
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

export default FieldModal;