import { useState } from 'react';
import './Header.css';
import * as adminApi from '../../api/adminApi';

interface HeaderProps {
  onOpenUploadModal: () => void;
}

const Header = ({ onOpenUploadModal }: HeaderProps) => {
  const [saving, setSaving] = useState(false);
  const [generating, setGenerating] = useState(false);
  
  const handleNavigateToApp = () => {
    // 業務画面へ移動（ポート5173）
    window.location.href = 'http://localhost:5173';
  };

  const handleGenerateApiDocs = async () => {
    setGenerating(true);
    try {
      const response = await fetch('http://localhost:8082/api/admin/docs/generate', {
        method: 'POST',
      });
      
      if (!response.ok) {
        throw new Error('APIドキュメント生成に失敗しました');
      }
      
      const result = await response.json();
      console.log('[Header] APIドキュメント生成成功:', result);
      
      // Swagger UIを別タブで開く
      window.open('http://localhost:8082/swagger-ui.html', '_blank');
    } catch (error) {
      console.error('[Header] APIドキュメント生成エラー:', error);
      alert('❌ APIドキュメント生成中にエラーが発生しました');
    } finally {
      setGenerating(false);
    }
  };

  const handleSaveAll = async () => {
    if (!confirm('すべての設定を一括保存しますか？\n\n※現在の画面の設定内容が保存されます。')) {
      return;
    }

    console.log('[Header] 一括保存開始');
    setSaving(true);
    try {
      // イベントを発火して各ConfigPanelに保存を要求
      console.log('[Header] save-all-configsイベントを発火');
      const event = new CustomEvent('save-all-configs');
      window.dispatchEvent(event);
      
      // 保存処理が完了するまで待機
      console.log('[Header] 保存処理完了待機中...');
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      console.log('[Header] ✅ 一括保存完了');
      alert('✅ すべての設定を保存しました\n\n業務画面をリロードして確認してください。');
    } catch (error) {
      console.error('[Header] Save error:', error);
      alert('❌ 保存中にエラーが発生しました');
    } finally {
      setSaving(false);
    }
  };

  return (
    <header className="admin-header">
      <div className="header-left">
        <h1 className="header-title">TableCraft 管理画面</h1>
      </div>
      <div className="header-right">
        <button className="btn btn-primary" onClick={onOpenUploadModal}>
          📤 SQLアップロード
        </button>
        <button 
          className="btn btn-info" 
          onClick={handleGenerateApiDocs}
          disabled={generating}
        >
          {generating ? '📖 生成中...' : '📖 APIドキュメント'}
        </button>
        <button 
          className="btn btn-warning" 
          onClick={handleSaveAll}
          disabled={saving}
        >
          {saving ? '💾 保存中...' : '💾 一括保存'}
        </button>
        <button className="btn btn-success" onClick={handleNavigateToApp}>
          業務画面へ移動 →
        </button>
      </div>
    </header>
  );
};

export default Header;
