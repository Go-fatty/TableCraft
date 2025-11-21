import React, { useState, useEffect } from 'react';
import './MainContent.css';

interface MainContentProps {
  children: React.ReactNode;
  selectedTable: string | null;
  currentView: 'list' | 'create' | 'edit';
  onViewChange: (view: 'list' | 'create' | 'edit') => void;
}

type TableConfig = {
  tables: {
    [tableName: string]: {
      name: string;
      metadata: {
        labels: Record<string, string>;
        description: Record<string, string>;
      };
    };
  };
  project: {
    defaultLanguage: string;
    supportedLanguages: string[];
  };
};

const MainContent: React.FC<MainContentProps> = ({ 
  children, 
  selectedTable, 
  currentView, 
  onViewChange 
}) => {
  const [tableConfig, setTableConfig] = useState<TableConfig | null>(null);
  const [language, setLanguage] = useState('ja');

  useEffect(() => {
    loadTableConfig();
  }, []);

  const loadTableConfig = async () => {
    try {
      const response = await fetch('http://localhost:8082/api/sql/config/table-config', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({}),
      });
      if (response.ok) {
        const configData = await response.json();
        setTableConfig(configData);
        setLanguage(configData.project.defaultLanguage || 'ja');
      }
    } catch (err) {
      console.error('Failed to load table config:', err);
    }
  };

  const getTableDisplayName = (tableName: string): string => {
    if (tableConfig && tableConfig.tables[tableName]) {
      const tableInfo = tableConfig.tables[tableName];
      return tableInfo.metadata.labels[language] || tableInfo.metadata.labels.ja || tableName;
    }
    
    // フォールバック：静的な表示名マッピング
    const displayNames: Record<string, string> = {
      users: 'ユーザー管理',
      categories: 'カテゴリ管理',
      products: '商品管理',
      orders: '注文管理',
      order_details: '注文明細管理',
    };
    
    return displayNames[tableName] || `${tableName} 管理`;
  };

  return (
    <div className="main-content">
      <header className="content-header">
        <div className="header-info">
          {selectedTable ? (
            <>
              <h1 className="page-title">
                {currentView === 'create' && '➕ 新規登録 - '}
                {currentView === 'edit' && '📝 編集 - '}
                {getTableDisplayName(selectedTable)}
              </h1>
              <p className="page-subtitle">
                テーブル: <code>{selectedTable}</code>
                {currentView === 'list' && ' - データ一覧'}
                {currentView === 'create' && ' - 新しいレコードを作成'}
                {currentView === 'edit' && ' - 既存レコードを編集'}
              </p>
            </>
          ) : (
            <>
              <h1 className="page-title">AutoStack Builder</h1>
              <p className="page-subtitle">
                左側のサイドバーからテーブルを選択してください
              </p>
            </>
          )}
        </div>
        {selectedTable && (
          <div className="header-actions">
            {currentView === 'list' && (
              <button 
                className="create-button" 
                onClick={() => onViewChange('create')}
              >
                ➕ 新規登録
              </button>
            )}
            {(currentView === 'create' || currentView === 'edit') && (
              <button 
                className="back-button" 
                onClick={() => onViewChange('list')}
              >
                ← 一覧に戻る
              </button>
            )}
            <button className="refresh-button" onClick={() => window.location.reload()}>
              🔄 更新
            </button>
          </div>
        )}
      </header>
      
      <main className="content-body">
        {children}
      </main>
    </div>
  );
};

export default MainContent;