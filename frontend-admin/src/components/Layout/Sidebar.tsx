import { useState } from 'react';
import './Sidebar.css';

type MenuItem = 'table-config' | 'ui-config' | 'table-builder' | 'script-management';

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

interface SidebarProps {
  activeMenu: MenuItem | string; // string for table-specific items like 'table-users'
  onMenuChange: (menu: MenuItem | string) => void;
  onAddTable: () => void;
  onAddTableFromTemplate: () => void;
  tables: TableInfo[];
  customScreens: CustomScreenInfo[];
  onAddCustomScreen: () => void;
  scripts: Script[];
  onAddScript: () => void;
}

const Sidebar = ({ activeMenu, onMenuChange, onAddTable, onAddTableFromTemplate, tables, customScreens, onAddCustomScreen, scripts, onAddScript }: SidebarProps) => {
  const [expandedTables, setExpandedTables] = useState(true);
  const [expandedCustomScreens, setExpandedCustomScreens] = useState(true);
  const [expandedScripts, setExpandedScripts] = useState(true);

  const toggleTables = () => {
    setExpandedTables(!expandedTables);
  };

  const toggleCustomScreens = () => {
    setExpandedCustomScreens(!expandedCustomScreens);
  };

  const toggleScripts = () => {
    setExpandedScripts(!expandedScripts);
  };

  return (
    <aside className="admin-sidebar">
      <nav className="sidebar-nav">
        <div className="sidebar-section">
          <button
            className={`sidebar-item sidebar-expandable ${expandedTables ? 'expanded' : ''}`}
            onClick={toggleTables}
          >
            <span className="sidebar-icon">📋</span>
            <span className="sidebar-label">テーブル設定</span>
            <span className="expand-icon">{expandedTables ? '▼' : '▶'}</span>
          </button>
          {expandedTables && (
            <div className="sidebar-submenu">
              <button className="sidebar-add-button" onClick={onAddTable}>
                <span className="add-icon">➕</span>
                <span className="add-label">新規テーブル</span>
              </button>
              <button className="sidebar-add-button" onClick={onAddTableFromTemplate}>
                <span className="add-icon">📋</span>
                <span className="add-label">テンプレートから作成</span>
              </button>
              {tables.map((table) => (
                <button
                  key={table.id}
                  className={`sidebar-subitem ${activeMenu === `table-${table.id}` ? 'active' : ''}`}
                  onClick={() => onMenuChange(`table-${table.id}`)}
                >
                  <span className="subitem-icon">📄</span>
                  <span className="subitem-label">{table.displayName}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="sidebar-section">
          <button
            className={`sidebar-item sidebar-expandable ${expandedCustomScreens ? 'expanded' : ''}`}
            onClick={toggleCustomScreens}
          >
            <span className="sidebar-icon">🖥️</span>
            <span className="sidebar-label">カスタム画面設定</span>
            <span className="expand-icon">{expandedCustomScreens ? '▼' : '▶'}</span>
          </button>
          {expandedCustomScreens && (
            <div className="sidebar-submenu">
              <button className="sidebar-add-button" onClick={onAddCustomScreen}>
                <span className="add-icon">➕</span>
                <span className="add-label">新規カスタム画面</span>
              </button>
              {customScreens.map((screen) => (
                <button
                  key={screen.id}
                  className={`sidebar-subitem ${activeMenu === `custom-${screen.id}` ? 'active' : ''}`}
                  onClick={() => onMenuChange(`custom-${screen.id}`)}
                >
                  <span className="subitem-icon">🖼️</span>
                  <span className="subitem-label">{screen.displayName}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="sidebar-section">
          <button
            className={`sidebar-item sidebar-expandable ${expandedScripts ? 'expanded' : ''}`}
            onClick={toggleScripts}
          >
            <span className="sidebar-icon">📜</span>
            <span className="sidebar-label">スクリプト管理</span>
            <span className="expand-icon">{expandedScripts ? '▼' : '▶'}</span>
          </button>
          {expandedScripts && (
            <div className="sidebar-submenu">
              <button className="sidebar-add-button" onClick={onAddScript}>
                <span className="add-icon">➕</span>
                <span className="add-label">新規スクリプト</span>
              </button>
              {scripts.length === 0 ? (
                <div className="sidebar-empty-message">スクリプトがありません</div>
              ) : (
                <button
                  className={`sidebar-subitem ${activeMenu === 'script-management' ? 'active' : ''}`}
                  onClick={() => onMenuChange('script-management')}
                >
                  <span className="subitem-icon">📋</span>
                  <span className="subitem-label">スクリプト一覧 ({scripts.length})</span>
                </button>
              )}
            </div>
          )}
        </div>

        <button
          className={`sidebar-item ${activeMenu === 'ui-config' ? 'active' : ''}`}
          onClick={() => onMenuChange('ui-config')}
        >
          <span className="sidebar-icon">🎨</span>
          <span className="sidebar-label">UI設定</span>
        </button>
      </nav>
    </aside>
  );
};

export default Sidebar;
