import { useState, useEffect } from 'react';
import './ScriptModal.css';

interface ScriptParameter {
  name: string;
  type: 'string' | 'number' | 'boolean' | 'object';
  required: boolean;
  description?: string;
}

interface Script {
  id: string;
  scriptName: string;
  scriptType: 'javascript' | 'groovy' | 'sql';
  description: string;
  scriptContent: string;
  parameters: ScriptParameter[];
  returnType: string;
  isActive: boolean;
  version: number;
  createdAt: string;
  updatedAt: string;
}

interface ScriptModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (script: Script) => void;
  script: Script | null;
}

const ScriptModal = ({ isOpen, onClose, onSave, script }: ScriptModalProps) => {
  const [formData, setFormData] = useState<Script>({
    id: '',
    scriptName: '',
    scriptType: 'javascript',
    description: '',
    scriptContent: '',
    parameters: [],
    returnType: 'void',
    isActive: true,
    version: 1,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (script) {
      setFormData(script);
    } else {
      setFormData({
        id: '',
        scriptName: '',
        scriptType: 'javascript',
        description: '',
        scriptContent: getDefaultTemplate('javascript'),
        parameters: [],
        returnType: 'void',
        isActive: true,
        version: 1,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      });
    }
    setErrors({});
  }, [script, isOpen]);

  const getDefaultTemplate = (type: string): string => {
    switch (type) {
      case 'javascript':
        return `/**
 * カスタムスクリプト
 * @param {Object} context - 実行コンテキスト
 * @param {Object} context.record - レコードデータ
 * @param {Array} context.selectedRecords - 選択されたレコード
 * @param {Object} context.params - 追加パラメータ
 * @returns {Object} 処理結果
 */
function execute(context) {
  const { record, selectedRecords, params } = context;
  
  // ここにロジックを記述
  console.log('Record:', record);
  
  return {
    success: true,
    message: '処理が完了しました'
  };
}`;

      case 'groovy':
        return `import groovy.json.JsonSlurper

/**
 * Groovyスクリプト
 */
def execute(Map context) {
    def record = context.record
    def params = context.params
    
    // ここにロジックを記述
    println "Record: \${record}"
    
    return [
        success: true,
        message: '処理が完了しました'
    ]
}`;

      case 'sql':
        return `-- SQLスクリプト
-- パラメータ: :id, :name, :value

UPDATE products 
SET stock_count = stock_count + :value
WHERE id = :id;

SELECT * FROM products WHERE id = :id;`;

      default:
        return '';
    }
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });

    if (name === 'scriptType') {
      setFormData({
        ...formData,
        scriptType: value as 'javascript' | 'groovy' | 'sql',
        scriptContent: getDefaultTemplate(value),
      });
    }
  };

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, checked } = e.target;
    setFormData({
      ...formData,
      [name]: checked,
    });
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.scriptName.trim()) {
      newErrors.scriptName = 'スクリプト名は必須です';
    }

    if (!formData.description.trim()) {
      newErrors.description = '説明は必須です';
    }

    if (!formData.scriptContent.trim()) {
      newErrors.scriptContent = 'スクリプトコードは必須です';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    const scriptToSave: Script = {
      ...formData,
      id: formData.id || `script_${Date.now()}`,
      updatedAt: new Date().toISOString(),
    };

    onSave(scriptToSave);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content script-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{script ? 'スクリプト編集' : '新規スクリプト作成'}</h2>
          <button className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">
                  スクリプト名 <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="scriptName"
                  className={`form-input ${errors.scriptName ? 'input-error' : ''}`}
                  value={formData.scriptName}
                  onChange={handleInputChange}
                  placeholder="例: calculate_discount"
                />
                {errors.scriptName && <span className="error-message">{errors.scriptName}</span>}
              </div>

              <div className="form-group">
                <label className="form-label">
                  スクリプトタイプ <span className="required">*</span>
                </label>
                <select
                  name="scriptType"
                  className="form-input"
                  value={formData.scriptType}
                  onChange={handleInputChange}
                >
                  <option value="javascript">JavaScript</option>
                  <option value="groovy">Groovy</option>
                  <option value="sql">SQL</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                説明 <span className="required">*</span>
              </label>
              <textarea
                name="description"
                className={`form-textarea ${errors.description ? 'input-error' : ''}`}
                value={formData.description}
                onChange={handleInputChange}
                placeholder="このスクリプトの目的と動作を説明してください"
                rows={3}
              />
              {errors.description && <span className="error-message">{errors.description}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">
                スクリプトコード <span className="required">*</span>
              </label>
              <textarea
                name="scriptContent"
                className={`form-textarea code-textarea ${errors.scriptContent ? 'input-error' : ''}`}
                value={formData.scriptContent}
                onChange={handleInputChange}
                placeholder="スクリプトコードを入力してください"
                rows={15}
                style={{ fontFamily: 'monospace', fontSize: '0.9rem' }}
              />
              {errors.scriptContent && (
                <span className="error-message">{errors.scriptContent}</span>
              )}
            </div>

            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">戻り値の型</label>
                <input
                  type="text"
                  name="returnType"
                  className="form-input"
                  value={formData.returnType}
                  onChange={handleInputChange}
                  placeholder="例: object, void, string"
                />
              </div>

              <div className="form-group">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    name="isActive"
                    checked={formData.isActive}
                    onChange={handleCheckboxChange}
                  />
                  <span>スクリプトを有効化</span>
                </label>
              </div>
            </div>

            <div className="info-box">
              <strong>💡 ヒント:</strong> スクリプトは保存後にテスト実行できます。
              実際のデータを使用せず、安全にテストできます。
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              キャンセル
            </button>
            <button type="submit" className="btn btn-primary">
              {script ? '更新' : '作成'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ScriptModal;
