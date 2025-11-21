import React, { useState, useEffect } from 'react';
import './Sidebar.css';

interface SidebarProps {
  selectedTable: string | null;
  onTableSelect: (tableName: string) => void;
}

interface TableInfo {
  name: string;
  displayName: string;
  icon: string;
  description: string;
  category: string;
  sortOrder: number;
}

type TableConfig = {
  tables: {
    [tableName: string]: {
      name: string;
      metadata: {
        icon: string;
        color: string;
        sortOrder: number;
        category: string;
        labels: Record<string, string>;
        description: Record<string, string>;
      };
    };
  };
  project: {
    name: string;
    defaultLanguage: string;
    supportedLanguages: string[];
  };
};

const Sidebar: React.FC<SidebarProps> = ({ selectedTable, onTableSelect }) => {
  const [tables, setTables] = useState<string[]>([]);
  const [tableConfig, setTableConfig] = useState<TableConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [language, setLanguage] = useState('ja');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      // テーブル一覧とテーブル設定を並行して読み込み
      const [tablesResponse, configResponse] = await Promise.all([
        fetch('http://localhost:8082/api/sql/tables', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({}),
        }),
        fetch('http://localhost:8082/api/sql/config/table-config', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({}),
        })
      ]);

      if (!tablesResponse.ok || !configResponse.ok) {
        throw new Error('データの読み込みに失敗しました');
      }

      const tablesData = await tablesResponse.json();
      const configData = await configResponse.json();

      if (tablesData.tables) {
        setTables(tablesData.tables);
      }

      setTableConfig(configData);
      setLanguage(configData.project.defaultLanguage || 'ja');

    } catch (err) {
      setError(err instanceof Error ? err.message : '不明なエラーが発生しました');
      console.error('Failed to load data:', err);
    } finally {
      setLoading(false);
    }
  };

  const getTableInfo = (tableName: string): TableInfo => {
    if (tableConfig && tableConfig.tables[tableName]) {
      const config = tableConfig.tables[tableName];
      return {
        name: tableName,
        displayName: config.metadata.labels[language] || config.metadata.labels.ja || tableName,
        icon: config.metadata.icon || '📋',
        description: config.metadata.description[language] || config.metadata.description.ja || '',
        category: config.metadata.category || 'other',
        sortOrder: config.metadata.sortOrder || 999,
      };
    }

    // フォールバック
    const fallbackMap: Record<string, Partial<TableInfo>> = {
      users: { displayName: 'ユーザー', icon: '👥', category: 'user_management' },
      categories: { displayName: 'カテゴリー', icon: '📂', category: 'catalog' },
      products: { displayName: '商品', icon: '📦', category: 'catalog' },
      orders: { displayName: '注文', icon: '🛒', category: 'orders' },
      order_details: { displayName: '注文明細', icon: '📋', category: 'orders' },
    };

    const fallback = fallbackMap[tableName] || {};
    return {
      name: tableName,
      displayName: fallback.displayName || tableName,
      icon: fallback.icon || '📋',
      description: '',
      category: fallback.category || 'other',
      sortOrder: 999,
    };
  };

  const getSortedTables = (): TableInfo[] => {
    return tables
      .map(tableName => getTableInfo(tableName))
      .sort((a, b) => {
        // カテゴリ順、その後ソート順、最後に名前順
        if (a.category !== b.category) {
          return a.category.localeCompare(b.category);
        }
        if (a.sortOrder !== b.sortOrder) {
          return a.sortOrder - b.sortOrder;
        }
        return a.displayName.localeCompare(b.displayName);
      });
  };

  const getTablesByCategory = (): Record<string, TableInfo[]> => {
    const sortedTables = getSortedTables();
    const categories: Record<string, TableInfo[]> = {};
    
    sortedTables.forEach(table => {
      const category = table.category;
      if (!categories[category]) {
        categories[category] = [];
      }
      categories[category].push(table);
    });

    return categories;
  };

  const getCategoryDisplayName = (category: string): string => {
    const categoryNames: Record<string, Record<string, string>> = {
      user_management: { ja: 'ユーザー管理', en: 'User Management' },
      catalog: { ja: 'カタログ管理', en: 'Catalog Management' },
      orders: { ja: '注文管理', en: 'Order Management' },
      other: { ja: 'その他', en: 'Others' },
    };

    return categoryNames[category]?.[language] || categoryNames[category]?.ja || category;
  };

  if (loading) {
    return (
      <div className="sidebar">
        <div className="sidebar-header">
          <h2>📊 AutoStack Builder</h2>
        </div>
        <div className="sidebar-loading">
          <div className="loading-spinner"></div>
          <p>読み込み中...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="sidebar">
        <div className="sidebar-header">
          <h2>📊 AutoStack Builder</h2>
        </div>
        <div className="sidebar-error">
          <p>❌ {error}</p>
          <button onClick={loadData} className="retry-button">
            再試行
          </button>
        </div>
      </div>
    );
  }

  const tablesByCategory = getTablesByCategory();
  const projectName = tableConfig?.project.name || 'AutoStack Builder';

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h2>📊 {projectName}</h2>
        <p className="sidebar-subtitle">動的CRUD管理画面</p>
      </div>
      
      {Object.entries(tablesByCategory).map(([category, categoryTables]) => (
        <div key={category} className="sidebar-section">
          <h3 className="section-title">
            {getCategoryDisplayName(category)}
          </h3>
          <ul className="table-list">
            {categoryTables.map((tableInfo) => (
              <li key={tableInfo.name}>
                <button
                  className={`table-button ${selectedTable === tableInfo.name ? 'active' : ''}`}
                  onClick={() => onTableSelect(tableInfo.name)}
                  title={tableInfo.description || `${tableInfo.displayName}テーブル`}
                >
                  <span className="table-icon">{tableInfo.icon}</span>
                  <span className="table-name">{tableInfo.displayName}</span>
                  <span className="table-count">({tableInfo.name})</span>
                </button>
              </li>
            ))}
          </ul>
        </div>
      ))}

      <div className="sidebar-footer">
        <p className="version-info">v1.0.0</p>
        <p className="footer-text">設定駆動型CRUD</p>
      </div>
    </div>
  );
};

export default Sidebar;