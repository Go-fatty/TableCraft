import { useState } from 'react';
import './ScriptList.css';

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

interface ScriptListProps {
  scripts: Script[];
  onEdit: (id: string) => void;
  onDelete: (id: string) => void;
  onTest: (id: string) => void;
  onAdd: () => void;
}

const ScriptList = ({ scripts, onEdit, onDelete, onTest, onAdd }: ScriptListProps) => {
  const [filter, setFilter] = useState<string>('all');

  const filteredScripts = filter === 'all' 
    ? scripts 
    : scripts.filter(s => s.scriptType === filter);

  const getScriptTypeBadgeClass = (type: string) => {
    switch (type) {
      case 'javascript': return 'badge-javascript';
      case 'groovy': return 'badge-groovy';
      case 'sql': return 'badge-sql';
      default: return 'badge-default';
    }
  };

  return (
    <div className="panel">
      <div className="panel-header-with-actions">
        <div>
          <h2 className="panel-title">スクリプト管理</h2>
          <p className="panel-subtitle">カスタムアクションで実行するスクリプトを管理します</p>
        </div>
        <div className="panel-header-actions">
          <button className="btn btn-primary" onClick={onAdd}>
            ➕ 新規スクリプト
          </button>
        </div>
      </div>

      <div className="script-filters">
        <label className="filter-label">種類でフィルター:</label>
        <select 
          className="filter-select" 
          value={filter} 
          onChange={(e) => setFilter(e.target.value)}
        >
          <option value="all">すべて</option>
          <option value="javascript">JavaScript</option>
          <option value="groovy">Groovy</option>
          <option value="sql">SQL</option>
        </select>
        <span className="script-count">{filteredScripts.length} 件のスクリプト</span>
      </div>

      <div className="script-table-container">
        <table className="script-table">
          <thead>
            <tr>
              <th>スクリプト名</th>
              <th>種類</th>
              <th>説明</th>
              <th>バージョン</th>
              <th>状態</th>
              <th>最終更新</th>
              <th>アクション</th>
            </tr>
          </thead>
          <tbody>
            {filteredScripts.length === 0 ? (
              <tr>
                <td colSpan={7} className="empty-message">
                  スクリプトがありません。「新規スクリプト」ボタンから作成してください。
                </td>
              </tr>
            ) : (
              filteredScripts.map((script) => (
                <tr key={script.id}>
                  <td className="script-name">{script.scriptName}</td>
                  <td>
                    <span className={`badge ${getScriptTypeBadgeClass(script.scriptType)}`}>
                      {script.scriptType}
                    </span>
                  </td>
                  <td className="script-description">{script.description}</td>
                  <td className="script-version">v{script.version}</td>
                  <td>
                    <span className={script.isActive ? 'status-active' : 'status-inactive'}>
                      {script.isActive ? '✓ 有効' : '✕ 無効'}
                    </span>
                  </td>
                  <td className="script-date">
                    {new Date(script.updatedAt).toLocaleDateString('ja-JP')}
                  </td>
                  <td className="script-actions">
                    <button
                      className="action-btn btn-edit"
                      onClick={() => onEdit(script.id)}
                      title="編集"
                    >
                      ✏️
                    </button>
                    <button
                      className="action-btn btn-test"
                      onClick={() => onTest(script.id)}
                      title="テスト実行"
                    >
                      ▶️
                    </button>
                    <button
                      className="action-btn btn-delete"
                      onClick={() => onDelete(script.id)}
                      title="削除"
                    >
                      🗑️
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ScriptList;
