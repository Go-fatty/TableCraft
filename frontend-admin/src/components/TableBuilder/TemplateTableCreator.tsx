import { useState } from 'react';
import type { TableCreationRequest } from '../../api/adminApi';
import { createTable } from '../../api/adminApi';
import TableEditorModal from './TableEditorModal';

interface TemplateTableCreatorProps {
  onSaved: () => void;
  onCancel: () => void;
}

const TemplateTableCreator = ({ onSaved, onCancel }: TemplateTableCreatorProps) => {
  const handleCreateTable = async (request: TableCreationRequest) => {
    try {
      const response = await createTable(request);
      
      if (!response.success) {
        alert('エラー: ' + (response.error || 'テーブルの作成に失敗しました'));
        throw new Error(response.error || 'Failed to create table');
      }
      
      alert('テーブルを作成しました');
      onSaved();
    } catch (error: any) {
      console.error('テーブルの作成に失敗しました:', error);
      
      // エラーメッセージを抽出
      let errorMessage = 'テーブルの作成に失敗しました';
      if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      // 重複エラーの場合は特別なメッセージ
      if (errorMessage.includes('already exists')) {
        alert('エラー: 同じ名前のテーブルが既に存在します。\n別のテーブル名を使用してください。');
      } else {
        alert('エラー: ' + errorMessage);
      }
      
      throw error;
    }
  };

  return (
    <TableEditorModal
      showTemplateSelector={true}
      onSave={handleCreateTable}
      onCancel={onCancel}
    />
  );
};

export default TemplateTableCreator;
