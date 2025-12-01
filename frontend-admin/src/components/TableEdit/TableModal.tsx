import { useState, useEffect } from 'react';
import './TableModal.css';

interface TableInfo {
  id: string;
  name: string;
  displayName: string;
  description: string;
  enableSearch: boolean;
  enableSort: boolean;
  enablePagination: boolean;
  pageSize: number;
  allowCreate: boolean;
  allowEdit: boolean;
  allowDelete: boolean;
  allowBulk: boolean;
}

interface TableModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (table: TableInfo) => void;
  table: TableInfo | null;
  existingTables: TableInfo[];
}

const TableModal = ({ isOpen, onClose, onSave, table, existingTables }: TableModalProps) => {
  const [formData, setFormData] = useState<TableInfo>({
    id: '',
    name: '',
    displayName: '',
    description: '',
    enableSearch: true,
    enableSort: true,
    enablePagination: true,
    pageSize: 20,
    allowCreate: true,
    allowEdit: true,
    allowDelete: true,
    allowBulk: false,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [activeTab, setActiveTab] = useState<'basic' | 'features' | 'permissions'>('basic');

  useEffect(() => {
    if (table) {
      setFormData(table);
    } else {
      setFormData({
        id: '',
        name: '',
        displayName: '',
        description: '',
        enableSearch: true,
        enableSort: true,
        enablePagination: true,
        pageSize: 20,
        allowCreate: true,
        allowEdit: true,
        allowDelete: true,
        allowBulk: false,
      });
    }
    setErrors({});
    setActiveTab('basic');
  }, [table, isOpen]);

  if (!isOpen) return null;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;

    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : type === 'number' ? Number(value) : value,
    });

    if (errors[name]) {
      setErrors({ ...errors, [name]: '' });
    }
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'テーブル名は必須です';
    } else if (!/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(formData.name)) {
      newErrors.name = 'テーブル名は英数字とアンダースコアのみ使用できます';
    } else if (!table) {
      const duplicate = existingTables.find((t) => t.name === formData.name);
      if (duplicate) {
        newErrors.name = 'このテーブル名は既に使用されています';
      }
    }

    if (!formData.displayName.trim()) {
      newErrors.displayName = '表示名は必須です';
    }

    if (formData.enablePagination && (formData.pageSize < 1 || formData.pageSize > 1000)) {
      newErrors.pageSize = 'ページサイズは1〜1000の範囲で指定してください';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (!validate()) {
      if (errors.name || errors.displayName) {
        setActiveTab('basic');
      } else if (errors.pageSize) {
        setActiveTab('features');
      }
      return;
    }

    if (!table) {
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
            {table ? 'テーブル編集' : '新規テーブル作成'}
          </h2>
          <button className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="modal-tabs">
          <button
            className={`modal-tab ${activeTab === 'basic' ? 'active' : ''}`}
            onClick={() => setActiveTab('basic')}
          >
            📋 基本情報
          </button>
          <button
            className={`modal-tab ${activeTab === 'features' ? 'active' : ''}`}
            onClick={() => setActiveTab('features')}
          >
            ⚙️ 機能設定
          </button>
          <button
            className={`modal-tab ${activeTab === 'permissions' ? 'active' : ''}`}
            onClick={() => setActiveTab('permissions')}
          >
            🔒 操作権限
          </button>
        </div>

        <div className="modal-body">
          {activeTab === 'basic' && (
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">
                  テーブル名 <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="name"
                  className={`form-input ${errors.name ? 'error' : ''}`}
                  placeholder="例: users"
                  value={formData.name}
                  onChange={handleChange}
                  disabled={!!table}
                />
                {errors.name && <span className="error-message">{errors.name}</span>}
                <span className="help-text">データベースのテーブル名（英数字・アンダースコア）</span>
              </div>

              <div className="form-group">
                <label className="form-label">
                  表示名 <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="displayName"
                  className={`form-input ${errors.displayName ? 'error' : ''}`}
                  placeholder="例: ユーザー"
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
                  placeholder="テーブルの説明を入力"
                  value={formData.description}
                  onChange={handleChange}
                  rows={4}
                />
                <span className="help-text">このテーブルの用途や内容を記述します</span>
              </div>
            </div>
          )}

          {activeTab === 'features' && (
            <div className="settings-grid">
              <div className="settings-section">
                <h4 className="settings-section-title">表示機能</h4>
                <div className="checkbox-list">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      name="enableSearch"
                      className="form-checkbox"
                      checked={formData.enableSearch}
                      onChange={handleChange}
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
                      checked={formData.enableSort}
                      onChange={handleChange}
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
                      checked={formData.enablePagination}
                      onChange={handleChange}
                    />
                    <div className="checkbox-content">
                      <span className="checkbox-text">ページネーションを有効化</span>
                      <span className="checkbox-description">データを複数ページに分割して表示</span>
                    </div>
                  </label>
                </div>
              </div>

              {formData.enablePagination && (
                <div className="settings-section">
                  <h4 className="settings-section-title">ページネーション設定</h4>
                  <div className="form-group">
                    <label className="form-label">1ページあたりの表示件数</label>
                    <input
                      type="number"
                      name="pageSize"
                      className={`form-input ${errors.pageSize ? 'error' : ''}`}
                      value={formData.pageSize}
                      onChange={handleChange}
                      min="1"
                      max="1000"
                    />
                    {errors.pageSize && <span className="error-message">{errors.pageSize}</span>}
                    <span className="help-text">推奨: 10〜100件</span>
                  </div>
                </div>
              )}
            </div>
          )}

          {activeTab === 'permissions' && (
            <div className="settings-grid">
              <div className="settings-section">
                <h4 className="settings-section-title">基本操作</h4>
                <div className="checkbox-list">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      name="allowCreate"
                      className="form-checkbox"
                      checked={formData.allowCreate}
                      onChange={handleChange}
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
                      checked={formData.allowEdit}
                      onChange={handleChange}
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
                      checked={formData.allowDelete}
                      onChange={handleChange}
                    />
                    <div className="checkbox-content">
                      <span className="checkbox-text">削除を許可</span>
                      <span className="checkbox-description">「削除」ボタンを表示</span>
                    </div>
                  </label>
                </div>
              </div>

              <div className="settings-section">
                <h4 className="settings-section-title">拡張操作</h4>
                <div className="checkbox-list">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      name="allowBulk"
                      className="form-checkbox"
                      checked={formData.allowBulk}
                      onChange={handleChange}
                    />
                    <div className="checkbox-content">
                      <span className="checkbox-text">一括操作を許可</span>
                      <span className="checkbox-description">複数選択して一括編集・削除</span>
                    </div>
                  </label>
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>
            キャンセル
          </button>
          <button className="btn btn-primary" onClick={handleSave}>
            {table ? '更新' : '作成'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default TableModal;