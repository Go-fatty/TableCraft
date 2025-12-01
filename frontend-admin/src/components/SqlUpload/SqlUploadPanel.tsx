import { useState } from 'react';
import './SqlUploadPanel.css';

interface UploadedFile {
  id: string;
  name: string;
  size: number;
  uploadedAt: Date;
  status: 'pending' | 'parsing' | 'completed' | 'error';
  tables?: string[];
}

const SqlUploadPanel = () => {
  // SQLファイル一覧（SqlUploadModalで管理・更新）
  const [files, setFiles] = useState<UploadedFile[]>([]);

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const getStatusBadge = (status: UploadedFile['status']) => {
    switch (status) {
      case 'pending':
        return <span className="status-badge status-pending">待機中</span>;
      case 'parsing':
        return <span className="status-badge status-parsing">解析中...</span>;
      case 'completed':
        return <span className="status-badge status-completed">完了</span>;
      case 'error':
        return <span className="status-badge status-error">エラー</span>;
      default:
        return null;
    }
  };

  const handleDeleteFile = (id: string) => {
    if (confirm('このファイルを削除しますか？')) {
      setFiles(files.filter((file) => file.id !== id));
      alert('ファイルを削除しました（バックエンド実装後に動作します）');
    }
  };

  const handleReparse = (id: string) => {
    const file = files.find((f) => f.id === id);
    if (file) {
      alert(`${file.name} を再解析します（バックエンド実装後に動作します）`);
    }
  };

  const handleViewDetails = (id: string) => {
    const file = files.find((f) => f.id === id);
    if (file) {
      const details = `
ファイル名: ${file.name}
サイズ: ${formatFileSize(file.size)}
アップロード日時: ${file.uploadedAt.toLocaleString('ja-JP')}
ステータス: ${file.status}
検出されたテーブル: ${file.tables?.join(', ') || 'なし'}
      `;
      alert(details);
    }
  };

  return (
    <div className="panel">
      <h2 className="panel-title">アップロード済みSQLファイル</h2>

      <div className="panel-section">
        <div className="alert alert-info">
          <strong>💡 使い方:</strong> 右上の「📤 SQLアップロード」ボタンから新しいSQLファイルをアップロードできます。
          アップロードされたファイルは自動的に解析され、テーブル構造が抽出されます。
        </div>
      </div>

      {files.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📂</div>
          <div className="empty-text">アップロード済みのSQLファイルがありません</div>
          <div className="empty-hint">
            右上の「SQLアップロード」ボタンからファイルをアップロードしてください
          </div>
        </div>
      ) : (
        <div className="panel-section">
          <div className="files-grid">
            {files.map((file) => (
              <div key={file.id} className="file-card">
                <div className="file-card-header">
                  <div className="file-icon">📄</div>
                  <div className="file-card-title">{file.name}</div>
                </div>
                <div className="file-card-body">
                  <div className="file-meta">
                    <div className="meta-item">
                      <span className="meta-label">サイズ:</span>
                      <span className="meta-value">{formatFileSize(file.size)}</span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">アップロード:</span>
                      <span className="meta-value">
                        {file.uploadedAt.toLocaleString('ja-JP', {
                          year: 'numeric',
                          month: '2-digit',
                          day: '2-digit',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">ステータス:</span>
                      {getStatusBadge(file.status)}
                    </div>
                  </div>
                  {file.tables && file.tables.length > 0 && (
                    <div className="file-tables">
                      <div className="tables-label">検出されたテーブル:</div>
                      <div className="tables-list">
                        {file.tables.map((table) => (
                          <span key={table} className="table-tag">
                            {table}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
                <div className="file-card-footer">
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => handleViewDetails(file.id)}
                  >
                    詳細
                  </button>
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => handleReparse(file.id)}
                  >
                    再解析
                  </button>
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={() => handleDeleteFile(file.id)}
                  >
                    削除
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="panel-section">
        <h3 className="section-title">統計情報</h3>
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-value">{files.length}</div>
            <div className="stat-label">総ファイル数</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">
              {files.filter((f) => f.status === 'completed').length}
            </div>
            <div className="stat-label">解析完了</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">
              {files.reduce((sum, f) => sum + (f.tables?.length || 0), 0)}
            </div>
            <div className="stat-label">検出テーブル数</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">
              {formatFileSize(files.reduce((sum, f) => sum + f.size, 0))}
            </div>
            <div className="stat-label">総サイズ</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SqlUploadPanel;
