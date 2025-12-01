import { useState, useEffect } from 'react';
import '../TableEdit/TableModal.css';

interface CustomScreenInfo {
  id: string;
  name: string;
  displayName: string;
  description: string;
  targetTables: string[];
}

interface TableInfo {
  id: string;
  name: string;
  displayName: string;
}

interface CustomScreenModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (screen: CustomScreenInfo) => void;
  screen: CustomScreenInfo | null;
  existingScreens: CustomScreenInfo[];
  availableTables: TableInfo[];
}

const CustomScreenModal = ({
  isOpen,
  onClose,
  onSave,
  screen,
  existingScreens,
  availableTables,
}: CustomScreenModalProps) => {
  const [formData, setFormData] = useState<CustomScreenInfo>({
    id: '',
    name: '',
    displayName: '',
    description: '',
    targetTables: [],
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (screen) {
      setFormData(screen);
    } else {
      setFormData({
        id: '',
        name: '',
        displayName: '',
        description: '',
        targetTables: [],
      });
    }
    setErrors({});
  }, [screen, isOpen]);

  if (!isOpen) return null;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
    if (errors[name]) {
      setErrors({ ...errors, [name]: '' });
    }
  };

  const handleTargetTablesChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selected = Array.from(e.target.selectedOptions).map((option) => option.value);
    setFormData({
      ...formData,
      targetTables: selected,
    });
    if (errors.targetTables) {
      setErrors({ ...errors, targetTables: '' });
    }
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = '画面名は必須です';
    } else if (!/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(formData.name)) {
      newErrors.name = '画面名は英数字とアンダースコアのみ使用できます';
    } else if (!screen) {
      const duplicate = existingScreens.find((s) => s.name === formData.name);
      if (duplicate) {
        newErrors.name = 'この画面名は既に使用されています';
      }
    }

    if (!formData.displayName.trim()) {
      newErrors.displayName = '表示名は必須です';
    }

    if (formData.targetTables.length === 0) {
      newErrors.targetTables = '少なくとも1つのテーブルを選択してください';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (!validate()) return;

    if (!screen) {
      formData.id = formData.name;
    }

    onSave(formData);
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content modal-content-large" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">
            {screen ? 'カスタム画面編集' : '新規カスタム画面作成'}
          </h2>
          <button className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="modal-body">
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">
                画面名 <span className="required">*</span>
              </label>
              <input
                type="text"
                name="name"
                className={`form-input ${errors.name ? 'error' : ''}`}
                placeholder="例: user_product_form"
                value={formData.name}
                onChange={handleChange}
                disabled={!!screen}
              />
              {errors.name && <span className="error-message">{errors.name}</span>}
              <span className="help-text">画面識別子（英数字・アンダースコア）</span>
            </div>

            <div className="form-group">
              <label className="form-label">
                表示名 <span className="required">*</span>
              </label>
              <input
                type="text"
                name="displayName"
                className={`form-input ${errors.displayName ? 'error' : ''}`}
                placeholder="例: ユーザー・商品登録"
                value={formData.displayName}
                onChange={handleChange}
              />
              {errors.displayName && <span className="error-message">{errors.displayName}</span>}
              <span className="help-text">画面に表示される名前（日本語可）</span>
            </div>

            <div className="form-group form-group-full">
              <label className="form-label">説明</label>
              <textarea
                name="description"
                className="form-textarea"
                placeholder="カスタム画面の説明を入力"
                value={formData.description}
                onChange={handleChange}
                rows={3}
              />
              <span className="help-text">この画面の用途や内容を記述します</span>
            </div>

            <div className="form-group form-group-full">
              <label className="form-label">
                対象テーブル <span className="required">*</span>
              </label>
              <select
                multiple
                className={`form-select-multiple ${errors.targetTables ? 'error' : ''}`}
                value={formData.targetTables}
                onChange={handleTargetTablesChange}
                size={8}
              >
                {availableTables.map((table) => (
                  <option key={table.id} value={table.id}>
                    {table.displayName} ({table.name})
                  </option>
                ))}
              </select>
              {errors.targetTables && <span className="error-message">{errors.targetTables}</span>}
              <span className="help-text">
                この画面で使用するテーブルを選択（Ctrl/Cmd + クリックで複数選択）
              </span>

              {formData.targetTables.length > 0 && (
                <div className="selected-preview">
                  <strong>選択中:</strong>{' '}
                  {formData.targetTables
                    .map((id) => {
                      const table = availableTables.find((t) => t.id === id);
                      return table?.displayName;
                    })
                    .join(', ')}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>
            キャンセル
          </button>
          <button className="btn btn-primary" onClick={handleSave}>
            {screen ? '更新' : '作成'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CustomScreenModal;
