import { useState } from 'react';
import Sidebar from './components/Layout/Sidebar';
import MainContent from './components/Layout/MainContent';
import TableList from './components/Tables/TableList';
import DynamicForm from './components/Forms/DynamicForm';
import './App.css';

function App() {
  const [selectedTable, setSelectedTable] = useState<string | null>(null);
  const [currentView, setCurrentView] = useState<'list' | 'create' | 'edit'>('list');
  const [editData, setEditData] = useState<Record<string, any> | undefined>(undefined);

  const handleTableSelect = (tableName: string) => {
    setSelectedTable(tableName);
    setCurrentView('list');
    setEditData(undefined);
  };

  const handleViewChange = (view: 'list' | 'create' | 'edit') => {
    setCurrentView(view);
    if (view === 'create') {
      setEditData(undefined);
    }
  };

  const handleEdit = (data: Record<string, any>) => {
    console.log('=== handleEdit called ===');
    console.log('Received data:', data);
    console.log('Data keys:', Object.keys(data));
    console.log('Setting editData and changing view to edit');
    setEditData(data);
    setCurrentView('edit');
    console.log('=========================');
  };

  const handleFormSubmit = async (data: Record<string, any>) => {
    if (!selectedTable) return;

    console.log('=== handleFormSubmit Debug Info ===');
    console.log('selectedTable:', selectedTable);
    console.log('currentView:', currentView);
    console.log('editData:', editData);
    console.log('received data:', data);

    try {
      let url: string;
      let payload: any;

      // 編集モードかつeditDataにidがある場合は編集処理
      const isEditMode = currentView === 'edit' && editData && (editData.id || editData.ID);
      console.log('isEditMode:', isEditMode, 'editData.id:', editData?.id, 'editData.ID:', editData?.ID);

      if (isEditMode) {
        // 編集の場合
        url = 'http://localhost:8082/api/sql/update';
        payload = { 
          tableName: selectedTable, 
          id: editData.id || editData.ID, 
          data 
        };
        console.log('Edit mode - payload:', payload);
      } else {
        // 新規作成の場合
        url = 'http://localhost:8082/api/sql/create';
        // data からid/IDを除外（自動生成されるため）
        const { id, ID, ...dataWithoutId } = data;
        payload = { 
          tableName: selectedTable, 
          data: dataWithoutId 
        };
        console.log('Create mode - payload:', payload);
      }

      console.log('Final URL:', url);
      console.log('Final payload:', JSON.stringify(payload, null, 2));

      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        // エラーレスポンスの内容を取得
        const errorData = await response.json().catch(() => ({}));
        console.log('Error response:', errorData);
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorData.error || 'Unknown error'}`);
      }

      const result = await response.json();
      
      if (result.success) {
        alert(`${isEditMode ? '更新' : '登録'}が完了しました`);
        setCurrentView('list');
        setEditData(undefined);
      } else {
        throw new Error(result.error || '操作に失敗しました');
      }
    } catch (error) {
      console.error('Submit error:', error);
      alert(`エラーが発生しました: ${error instanceof Error ? error.message : '不明なエラー'}`);
    }
  };

  const handleFormCancel = () => {
    setCurrentView('list');
    setEditData(undefined);
  };

  return (
    <div className="app">
      <Sidebar 
        selectedTable={selectedTable}
        onTableSelect={handleTableSelect}
      />
      
      <MainContent 
        selectedTable={selectedTable}
        currentView={currentView}
        onViewChange={handleViewChange}
      >
        {!selectedTable ? (
          <div className="empty-state">
            <div className="empty-state-icon">📦</div>
            <h3>TableCraft へようこそ</h3>
            <p>
              左側のサイドバーからテーブルを選択して、動的CRUD操作を開始してください。
              <br />
              JSONメタデータから自動生成されたテーブルの管理が行えます。
            </p>
          </div>
        ) : currentView === 'list' ? (
          <TableList 
            tableName={selectedTable}
            onEdit={handleEdit}
          />
        ) : (
          <DynamicForm
            tableName={selectedTable}
            editData={editData}
            onSubmit={handleFormSubmit}
            onCancel={handleFormCancel}
          />
        )}
      </MainContent>
    </div>
  );
}

export default App
