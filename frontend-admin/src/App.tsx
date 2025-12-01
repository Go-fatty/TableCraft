import { useState, useEffect } from 'react';
import './App.css';
import Header from './components/Layout/Header';
import Sidebar from './components/Layout/Sidebar';
import SqlUploadModal from './components/SqlUpload/SqlUploadModal';
import ConfigPanel from './components/Config/ConfigPanel';
import TableEditPanel from './components/TableEdit/TableEditPanel';
import TableModal from './components/TableEdit/TableModal';
import CustomScreenPanel from './components/CustomScreen/CustomScreenPanel';
import CustomScreenModal from './components/CustomScreen/CustomScreenModal';
import ScriptList from './components/ScriptManagement/ScriptList';
import ScriptModal from './components/ScriptManagement/ScriptModal';
import TableBuilder from './components/TableBuilder/TableBuilder';
import TemplateTableCreator from './components/TableBuilder/TemplateTableCreator';
import { listTables as listTablesApi } from './api/adminApi';

type MenuItem = 'ui-config' | 'script-management' | 'table-builder';

interface TableInfo {
  id: string;
  name: string;
  displayName: string;
  description: string;
  columns?: any[];  // カラム情報
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

function App() {
  const [activeMenu, setActiveMenu] = useState<MenuItem | string>('ui-config');
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [isTableModalOpen, setIsTableModalOpen] = useState(false);
  const [editingTable, setEditingTable] = useState<TableInfo | null>(null);
  const [showTemplateModal, setShowTemplateModal] = useState(false);
  const [isCustomScreenModalOpen, setIsCustomScreenModalOpen] = useState(false);
  const [editingCustomScreen, setEditingCustomScreen] = useState<CustomScreenInfo | null>(null);
  const [isScriptModalOpen, setIsScriptModalOpen] = useState(false);
  const [editingScript, setEditingScript] = useState<Script | null>(null);

  // 初期状態は空
  const [tables, setTables] = useState<TableInfo[]>([]);

  const [customScreens, setCustomScreens] = useState<CustomScreenInfo[]>([]);

  const [scripts, setScripts] = useState<Script[]>([]);

  useEffect(() => {
    loadTablesFromBackend();
  }, []);

  const loadTablesFromBackend = async () => {
    try {
      const response = await listTablesApi();
      if (response.success && response.data) {
        const backendTables = response.data.map((t: any) => ({
          id: t.id ? String(t.id) : t.tableName, // UI設定がない場合はtableNameをidとして使用
          name: t.tableName,
          displayName: t.displayName || t.tableName,
          description: t.description || '',
          columns: t.columns || [],
          enableSearch: t.enableSearch ?? true,
          enableSort: t.enableSort ?? true,
          enablePagination: t.enablePagination ?? true,
          pageSize: t.pageSize || 20,
          allowCreate: t.allowCreate ?? true,
          allowEdit: t.allowEdit ?? true,
          allowDelete: t.allowDelete ?? true,
          allowBulk: t.allowBulk ?? false,
        }));
        setTables(backendTables);
        console.log('✅ テーブル一覧を読み込みました:', backendTables.length, '件');
      }
    } catch (error) {
      console.error('テーブル一覧の取得に失敗しました:', error);
    }
  };

  const handleAddTable = () => {
    setEditingTable(null);
    setIsTableModalOpen(true);
  };

  const handleAddTableFromTemplate = () => {
    setShowTemplateModal(true);
  };

  const handleTemplateModalClose = () => {
    setShowTemplateModal(false);
    loadTablesFromBackend();
  };

  const handleSaveTable = async (table: TableInfo) => {
    try {
      console.log('[App.handleSaveTable] 呼び出されました');
      console.log('[App.handleSaveTable] table:', table);
      
      // IDが存在する場合は更新、存在しない場合は新規作成
      const existingTable = tables.find(t => t.id === table.id);
      
      if (existingTable) {
        // 更新
        console.log('[App.handleSaveTable] テーブル更新:', table);
        
        const requestData = {
          tableName: table.name,
          displayName: table.displayName,
          columns: table.columns || [],
          enableSearch: table.enableSearch,
          enableSort: table.enableSort,
          enablePagination: table.enablePagination,
          pageSize: table.pageSize,
          allowCreate: table.allowCreate,
          allowEdit: table.allowEdit,
          allowDelete: table.allowDelete,
          allowBulk: table.allowBulk
        };
        
        console.log('[App.handleSaveTable] 送信データ:', requestData);
        
        const response = await fetch(`http://localhost:8082/api/admin/tables/update/${table.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            tableName: table.name,
            displayName: table.displayName,
            columns: table.columns || [],
            enableSearch: table.enableSearch,
            enableSort: table.enableSort,
            enablePagination: table.enablePagination,
            pageSize: table.pageSize,
            allowCreate: table.allowCreate,
            allowEdit: table.allowEdit,
            allowDelete: table.allowDelete,
            allowBulk: table.allowBulk
          })
        });

        console.log('[App.handleSaveTable] レスポンス:', response.status);

        if (!response.ok) {
          const errorText = await response.text();
          console.error('[App.handleSaveTable] エラーレスポンス:', errorText);
          throw new Error(`更新に失敗しました (${response.status}): ${errorText}`);
        }

        const result = await response.json();
        console.log('[App.handleSaveTable] 結果:', result);
        
        if (!result.success) {
          throw new Error(result.error || '更新に失敗しました');
        }

        // 成功したらバックエンドから最新データを再取得
        await loadTablesFromBackend();
        console.log('[App.handleSaveTable] ✅ 更新成功、データ再取得完了');
      } else {
        console.log('[App.handleSaveTable] 新規作成');
        // 新規作成
        setTables([...tables, table]);
        // 作成したテーブルの編集画面に遷移
        setActiveMenu(`table-${table.id}`);
      }
    } catch (error) {
      console.error('[App.handleSaveTable] 保存エラー:', error);
      alert(`保存に失敗しました: ${error instanceof Error ? error.message : '不明なエラー'}`);
    }
  };

  const handleDeleteTable = async (tableId: string) => {
    try {
      // ✅ バックエンドAPIを呼んで削除
      const response = await fetch(`http://localhost:8082/api/admin/tables/delete/${tableId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });

      if (!response.ok) {
        throw new Error('削除に失敗しました');
      }

      const result = await response.json();
      if (!result.success) {
        throw new Error(result.error || '削除に失敗しました');
      }

      // ✅ 成功したらフロントエンドの状態も更新
      setTables(tables.filter((t) => t.id !== tableId));
      
      // 削除後、最初のテーブルまたはUI設定に遷移
      if (tables.length > 1) {
        const remainingTables = tables.filter((t) => t.id !== tableId);
        setActiveMenu(`table-${remainingTables[0].id}`);
      } else {
        setActiveMenu('ui-config');
      }
      
      alert('テーブルを削除しました');
    } catch (error) {
      console.error('削除エラー:', error);
      alert(`削除に失敗しました: ${error instanceof Error ? error.message : '不明なエラー'}`);
    }
  };

  const handleAddCustomScreen = () => {
    setEditingCustomScreen(null);
    setIsCustomScreenModalOpen(true);
  };

  const handleSaveCustomScreen = (screen: CustomScreenInfo) => {
    if (editingCustomScreen) {
      setCustomScreens(customScreens.map((s) => (s.id === screen.id ? screen : s)));
    } else {
      setCustomScreens([...customScreens, screen]);
      setActiveMenu(`custom-${screen.id}`);
    }
  };

  const handleDeleteCustomScreen = (screenId: string) => {
    setCustomScreens(customScreens.filter((s) => s.id !== screenId));
    if (customScreens.length > 1) {
      const remainingScreens = customScreens.filter((s) => s.id !== screenId);
      setActiveMenu(`custom-${remainingScreens[0].id}`);
    } else if (tables.length > 0) {
      setActiveMenu(`table-${tables[0].id}`);
    } else {
      setActiveMenu('ui-config');
    }
  };

  const handleAddScript = () => {
    setEditingScript(null);
    setIsScriptModalOpen(true);
  };

  const handleSaveScript = (script: Script) => {
    if (editingScript) {
      setScripts(scripts.map((s) => (s.id === script.id ? script : s)));
    } else {
      setScripts([...scripts, script]);
    }
    setIsScriptModalOpen(false);
  };

  const handleEditScript = (id: string) => {
    const script = scripts.find((s) => s.id === id);
    if (script) {
      setEditingScript(script);
      setIsScriptModalOpen(true);
    }
  };

  const handleDeleteScript = (id: string) => {
    if (confirm('このスクリプトを削除しますか？')) {
      setScripts(scripts.filter((s) => s.id !== id));
    }
  };

  const handleTestScript = (id: string) => {
    const script = scripts.find((s) => s.id === id);
    if (script) {
      alert(`スクリプト "${script.scriptName}" のテスト実行（モック）\n\nバックエンドAPI実装後に実際に実行されます。`);
    }
  };

  const renderContent = () => {
    // テーブル編集画面
    if (activeMenu.startsWith('table-')) {
      const tableId = activeMenu.replace('table-', '');
      const table = tables.find((t) => t.id === tableId);
      if (table) {
        return (
          <TableEditPanel
            tableId={table.id}
            tableName={table.name}
            tableDisplayName={table.displayName}
            tableInfo={table}
            onDeleteTable={handleDeleteTable}
            onSave={handleSaveTable}
          />
        );
      }
    }

    // カスタム画面編集画面
    if (activeMenu.startsWith('custom-')) {
      const screenId = activeMenu.replace('custom-', '');
      const screen = customScreens.find((s) => s.id === screenId);
      if (screen) {
        return (
          <CustomScreenPanel
            screenId={screen.id}
            screenName={screen.name}
            screenDisplayName={screen.displayName}
            screenInfo={screen}
            onDeleteScreen={handleDeleteCustomScreen}
            availableTables={tables}
            availableScripts={scripts}
          />
        );
      }
    }

    // テーブルビルダー画面
    if (activeMenu === 'table-builder') {
      return <TableBuilder onTableSaved={loadTablesFromBackend} />;
    }

    // テンプレートからテーブル作成画面
    if (activeMenu === 'table-builder-template') {
      return <TableBuilder defaultShowTemplateSelector={true} onTableSaved={loadTablesFromBackend} />;
    }

    // スクリプト管理画面
    if (activeMenu === 'script-management') {
      return (
        <ScriptList
          scripts={scripts}
          onEdit={handleEditScript}
          onDelete={handleDeleteScript}
          onTest={handleTestScript}
          onAdd={handleAddScript}
        />
      );
    }

    // その他の設定画面
    switch (activeMenu) {
      case 'ui-config':
        return <ConfigPanel type="ui" title="UI設定" />;
      default:
        return <ConfigPanel type="ui" title="UI設定" />;
    }
  };

  return (
    <div className="admin-container">
      <Header onOpenUploadModal={() => setIsUploadModalOpen(true)} />
      <div className="admin-layout">
        <Sidebar 
          activeMenu={activeMenu} 
          onMenuChange={setActiveMenu} 
          onAddTable={handleAddTable}
          onAddTableFromTemplate={handleAddTableFromTemplate}
          tables={tables}
          customScreens={customScreens}
          onAddCustomScreen={handleAddCustomScreen}
          scripts={scripts}
          onAddScript={handleAddScript}
        />
        <main className="admin-content">
          {renderContent()}
        </main>
      </div>
      <SqlUploadModal
        isOpen={isUploadModalOpen}
        onClose={() => setIsUploadModalOpen(false)}
      />
      <TableModal
        isOpen={isTableModalOpen}
        onClose={() => {
          setIsTableModalOpen(false);
          setEditingTable(null);
        }}
        onSave={handleSaveTable}
        table={editingTable}
        existingTables={tables}
      />
      <CustomScreenModal
        isOpen={isCustomScreenModalOpen}
        onClose={() => {
          setIsCustomScreenModalOpen(false);
          setEditingCustomScreen(null);
        }}
        onSave={handleSaveCustomScreen}
        screen={editingCustomScreen}
        existingScreens={customScreens}
        availableTables={tables}
      />
      <ScriptModal
        isOpen={isScriptModalOpen}
        onClose={() => {
          setIsScriptModalOpen(false);
          setEditingScript(null);
        }}
        onSave={handleSaveScript}
        script={editingScript}
      />
      {showTemplateModal && (
        <TemplateTableCreator 
          onSaved={handleTemplateModalClose}
          onCancel={() => setShowTemplateModal(false)}
        />
      )}
    </div>
  );
}

export default App;

