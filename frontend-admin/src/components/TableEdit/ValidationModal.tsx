import { useState, useEffect } from 'react';
import './ValidationModal.css';

interface Field {
  id: string;
  name: string;
  displayName: string;
  type: string;
}

interface Validation {
  id: string;
  fieldName: string;
  type: string;
  rule: string;
  errorMessage: string;
}

interface ValidationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (validation: Validation) => void;
  validation: Validation | null;
  availableFields: Field[];
}

const ValidationModal = ({
  isOpen,
  onClose,
  onSave,
  validation,
  availableFields,
}: ValidationModalProps) => {
  const [formData, setFormData] = useState<Validation>({
    id: '',
    fieldName: '',
    type: 'required',
    rule: '',
    errorMessage: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (validation) {
      setFormData(validation);
    } else {
      setFormData({
        id: '',
        fieldName: availableFields.length > 0 ? availableFields[0].name : '',
        type: 'required',
        rule: '',
        errorMessage: '',
      });
    }
    setErrors({});
  }, [validation, isOpen, availableFields]);

  if (!isOpen) return null;

  const validationTypes = [
    { value: 'required', label: '必須チェック', ruleExample: '' },
    { value: 'pattern', label: '正規表現', ruleExample: '^[a-zA-Z0-9]+$' },
    { value: 'length', label: '文字数制限', ruleExample: 'min:2,max:50' },
    { value: 'range', label: '数値範囲', ruleExample: 'min:0,max:100' },
    { value: 'email', label: 'メールアドレス形式', ruleExample: '' },
    { value: 'url', label: 'URL形式', ruleExample: '' },
    { value: 'date', label: '日付形式', ruleExample: 'YYYY-MM-DD' },
    { value: 'custom', label: 'カスタムルール', ruleExample: 'customFunction' },
  ];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;

    setFormData({
      ...formData,
      [name]: value,
    });

    // Clear error for this field
    if (errors[name]) {
      setErrors({ ...errors, [name]: '' });
    }

    // Auto-generate error message template
    if (name === 'type') {
      const type = validationTypes.find((t) => t.value === value);
      if (type && !formData.errorMessage) {
        const field = availableFields.find((f) => f.name === formData.fieldName);
        const displayName = field?.displayName || formData.fieldName;
        let message = '';
        switch (value) {
          case 'required':
            message = `${displayName}は必須項目です`;
            break;
          case 'pattern':
            message = `${displayName}の形式が正しくありません`;
            break;
          case 'length':
            message = `${displayName}の文字数が範囲外です`;
            break;
          case 'range':
            message = `${displayName}の値が範囲外です`;
            break;
          case 'email':
            message = `${displayName}の形式が正しくありません`;
            break;
          case 'url':
            message = `${displayName}の形式が正しくありません`;
            break;
          case 'date':
            message = `${displayName}の日付形式が正しくありません`;
            break;
          default:
            message = `${displayName}の検証に失敗しました`;
        }
        setFormData((prev) => ({ ...prev, errorMessage: message }));
      }
    }
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.fieldName) {
      newErrors.fieldName = '対象フィールドを選択してください';
    }

    if (!formData.errorMessage.trim()) {
      newErrors.errorMessage = 'エラーメッセージは必須です';
    }

    // Type-specific validation
    const needsRule = ['pattern', 'length', 'range', 'date', 'custom'];
    if (needsRule.includes(formData.type) && !formData.rule.trim()) {
      newErrors.rule = 'このバリデーション種別にはルールの指定が必要です';
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

  const selectedType = validationTypes.find((t) => t.value === formData.type);
  const needsRule = ['pattern', 'length', 'range', 'date', 'custom'].includes(formData.type);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content modal-content-medium" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">
            {validation ? 'バリデーション編集' : 'バリデーション追加'}
          </h2>
          <button className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="modal-body">
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">
                対象フィールド <span className="required">*</span>
              </label>
              <select
                name="fieldName"
                className={`form-select ${errors.fieldName ? 'error' : ''}`}
                value={formData.fieldName}
                onChange={handleChange}
              >
                {availableFields.map((field) => (
                  <option key={field.name} value={field.name}>
                    {field.displayName} ({field.name})
                  </option>
                ))}
              </select>
              {errors.fieldName && <span className="error-message">{errors.fieldName}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">
                バリデーション種別 <span className="required">*</span>
              </label>
              <select
                name="type"
                className="form-select"
                value={formData.type}
                onChange={handleChange}
              >
                {validationTypes.map((type) => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>

            {needsRule && (
              <div className="form-group">
                <label className="form-label">
                  ルール <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="rule"
                  className={`form-input ${errors.rule ? 'error' : ''}`}
                  placeholder={`例: ${selectedType?.ruleExample || ''}`}
                  value={formData.rule}
                  onChange={handleChange}
                />
                {errors.rule && <span className="error-message">{errors.rule}</span>}
                <span className="help-text">
                  例: {selectedType?.ruleExample || ''}
                </span>
              </div>
            )}

            <div className="form-group">
              <label className="form-label">
                エラーメッセージ <span className="required">*</span>
              </label>
              <textarea
                name="errorMessage"
                className={`form-textarea ${errors.errorMessage ? 'error' : ''}`}
                placeholder="バリデーションエラー時に表示するメッセージ"
                value={formData.errorMessage}
                onChange={handleChange}
                rows={3}
              />
              {errors.errorMessage && <span className="error-message">{errors.errorMessage}</span>}
            </div>
          </div>

          <div className="info-box">
            <div className="info-title">💡 ルールの記述例</div>
            <ul className="info-list">
              <li><strong>文字数制限:</strong> <code>min:2,max:50</code></li>
              <li><strong>数値範囲:</strong> <code>min:0,max:100</code></li>
              <li><strong>正規表現:</strong> <code>^[\w-\.]+@([\w-]+\.)+[\w-]&#123;2,4&#125;$</code></li>
              <li><strong>日付形式:</strong> <code>YYYY-MM-DD</code> または <code>YYYY/MM/DD</code></li>
            </ul>
          </div>
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>
            キャンセル
          </button>
          <button className="btn btn-primary" onClick={handleSave}>
            {validation ? '更新' : '追加'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ValidationModal;