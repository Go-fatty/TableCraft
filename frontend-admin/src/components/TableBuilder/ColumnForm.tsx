import { useState } from 'react';
import type { ColumnDefinition } from '../../api/adminApi';
import './ColumnForm.css';

interface ColumnFormProps {
  onAdd: (column: ColumnDefinition) => void;
  onCancel: () => void;
  existingColumn?: ColumnDefinition;
}

const ColumnForm = ({ onAdd, onCancel, existingColumn }: ColumnFormProps) => {
  const [columnName, setColumnName] = useState(existingColumn?.columnName || '');
  const [dataType, setDataType] = useState(existingColumn?.dataType || 'VARCHAR(255)');
  const [nullable, setNullable] = useState(existingColumn?.nullable ?? true);
  const [primaryKey, setPrimaryKey] = useState(existingColumn?.primaryKey || false);
  const [autoIncrement, setAutoIncrement] = useState(existingColumn?.autoIncrement || false);
  const [defaultValue, setDefaultValue] = useState(existingColumn?.defaultValue || '');
  const [comment, setComment] = useState(existingColumn?.comment || '');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!columnName.trim()) {
      alert('カラム名を入力してください');
      return;
    }

    onAdd({
      columnName: columnName.trim(),
      dataType,
      nullable,
      primaryKey,
      autoIncrement,
      defaultValue: defaultValue.trim() || undefined,
      comment: comment.trim() || undefined,
    });

    // フォームリセット
    setColumnName('');
    setDataType('VARCHAR(255)');
    setNullable(true);
    setPrimaryKey(false);
    setAutoIncrement(false);
    setDefaultValue('');
    setComment('');
  };

  return (
    <div className="column-form-overlay">
      <div className="column-form-modal">
        <h3>カラム追加</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <label>
              カラム名 <span className="required">*</span>
              <input
                type="text"
                value={columnName}
                onChange={(e) => setColumnName(e.target.value)}
                placeholder="例: user_name"
                required
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              データ型 <span className="required">*</span>
              <select value={dataType} onChange={(e) => setDataType(e.target.value)}>
                <option value="VARCHAR(255)">VARCHAR(255)</option>
                <option value="VARCHAR(100)">VARCHAR(100)</option>
                <option value="VARCHAR(50)">VARCHAR(50)</option>
                <option value="TEXT">TEXT</option>
                <option value="INT">INT</option>
                <option value="BIGINT">BIGINT</option>
                <option value="DECIMAL(10,2)">DECIMAL(10,2)</option>
                <option value="BOOLEAN">BOOLEAN</option>
                <option value="DATE">DATE</option>
                <option value="DATETIME">DATETIME</option>
                <option value="TIMESTAMP">TIMESTAMP</option>
              </select>
            </label>
          </div>

          <div className="form-row form-checkboxes">
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={nullable}
                onChange={(e) => setNullable(e.target.checked)}
              />
              NULL許可
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={primaryKey}
                onChange={(e) => {
                  setPrimaryKey(e.target.checked);
                  if (e.target.checked) {
                    setNullable(false); // PKはNOT NULL
                  }
                }}
              />
              PRIMARY KEY
            </label>

            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={autoIncrement}
                onChange={(e) => setAutoIncrement(e.target.checked)}
                disabled={!primaryKey}
              />
              AUTO_INCREMENT
            </label>
          </div>

          <div className="form-row">
            <label>
              デフォルト値
              <input
                type="text"
                value={defaultValue}
                onChange={(e) => setDefaultValue(e.target.value)}
                placeholder="例: 0, CURRENT_TIMESTAMP"
                disabled={autoIncrement}
              />
              <small>AUTO_INCREMENTが有効な場合は設定できません</small>
            </label>
          </div>

          <div className="form-row">
            <label>
              コメント
              <input
                type="text"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="例: ユーザー名"
              />
            </label>
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={onCancel}>
              キャンセル
            </button>
            <button type="submit" className="btn btn-primary">
              {existingColumn ? '更新' : '追加'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ColumnForm;
