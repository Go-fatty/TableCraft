import { useState, useEffect } from 'react';
import './SqlUploadModal.css';
import * as adminApi from '../../api/adminApi';

interface UploadedFile {
  id: string;
  name: string;
  size: number;
  uploadedAt: Date;
  status: 'pending' | 'parsing' | 'completed' | 'error';
  tables?: string[];
  errorMessage?: string;
}

interface SqlUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const SqlUploadModal = ({ isOpen, onClose }: SqlUploadModalProps) => {
  const [files, setFiles] = useState<File[]>([]);
  const [dragOver, setDragOver] = useState(false);
  const [activeTab, setActiveTab] = useState<'upload' | 'list'>('list');
  const [uploadedFiles, setUploadedFiles] = useState<adminApi.SqlFileInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState<Map<string, string>>(new Map());

  // アップロード済みファイル一覧を取得
  useEffect(() => {
    if (isOpen) {
      loadUploadedFiles();
    }
  }, [isOpen]);

  const loadUploadedFiles = async () => {
    try {
      setLoading(true);
      const response = await adminApi.listSqlFiles();
      if (response.success && response.data) {
        setUploadedFiles(response.data);
      }
    } catch (error) {
      console.error('Failed to load uploaded files:', error);
      alert('ファイル一覧の取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = event.target.files;
    if (selectedFiles) {
      setFiles([...files, ...Array.from(selectedFiles).filter(f => f.name.endsWith('.sql'))]);
    }
  };

  const handleFiles = (fileList: File[]) => {
    const sqlFiles = fileList.filter((file) => file.name.endsWith('.sql'));
    setFiles([...files, ...sqlFiles]);
  };

  const handleDrop = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setDragOver(false);
    const droppedFiles = Array.from(event.dataTransfer.files);
    handleFiles(droppedFiles);
  };

  const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = () => {
    setDragOver(false);
  };

  const handleRemoveFile = (id: string) => {
    setFiles(files.filter((file) => file.id !== id));
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const handleUploadAll = () => {
    if (files.length === 0) {
      alert('アップロードするファイルがありません');
      return;
    }
    alert(`${files.length}件のファイルをアップロードします（バックエンド実装後に動作します）`);
    // ここでバックエンドにアップロード処理を実装
    setFiles([]);
    setActiveTab('list');
  };

  const handleDeleteUploadedFile = (id: string) => {
    if (confirm('このファイルを削除しますか？')) {
      alert('ファイルを削除しました（バックエンド実装後に動作します）');
    }
  };

  const handleReparse = (id: string) => {
    const file = uploadedFiles.find((f) => f.id === id);
    if (file) {
      alert(`${file.name} を再解析します（バックエンド実装後に動作します）`);
    }
  };

  const handleViewDetails = (id: string) => {
    const file = uploadedFiles.find((f) => f.id === id);
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

  const renderUploadTab = () => (
    <>
      <div
        className={`upload-area ${dragOver ? 'drag-over' : ''}`}
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onClick={() => document.getElementById('modal-file-input')?.click()}
      >
        <div className="upload-icon">📁</div>
        <div className="upload-text">
          クリックしてファイルを選択、またはドラッグ&ドロップ
        </div>
        <div className="upload-hint">
          対応ファイル形式: .sql (CREATE TABLE文を含むSQLファイル)
        </div>
        <input
          id="modal-file-input"
          type="file"
          accept=".sql"
          multiple
          style={{ display: 'none' }}
          onChange={handleFileSelect}
        />
      </div>

      {files.length > 0 && (
        <div className="modal-section">
          <h3 className="section-title">選択済みファイル ({files.length}件)</h3>
          <div className="file-list">
            {files.map((file) => (
              <div key={file.id} className="file-item">
                <div className="file-info">
                  <span className="file-name">{file.name}</span>
                  <span className="file-size">{formatFileSize(file.size)}</span>
                </div>
                <div className="file-actions">
                  <button
                    className="btn btn-danger btn-icon"
                    onClick={() => handleRemoveFile(file.id)}
                  >
                    削除
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="alert alert-info">
        <strong>💡 ヒント:</strong> アップロードされたSQLファイルは自動的に解析され、
        テーブル構造が抽出されます。解析結果を元に各種設定ファイルを生成できます。
      </div>
    </>
  );

  const renderListTab = () => (
    <>
      {uploadedFiles.length === 0 ? (
        <div className="empty-state-modal">
          <div className="empty-icon">📂</div>
          <div className="empty-text">アップロード済みのSQLファイルがありません</div>
          <div className="empty-hint">
            「新規アップロード」タブからファイルをアップロードしてください
          </div>
        </div>
      ) : (
        <>
          <div className="files-grid-modal">
            {uploadedFiles.map((file) => (
              <div key={file.id} className="file-card-compact">
                <div className="file-card-header-compact">
                  <div className="file-icon-compact">📄</div>
                  <div className="file-info-compact">
                    <div className="file-name-compact">{file.name}</div>
                    <div className="file-meta-compact">
                      {formatFileSize(file.size)} • {file.uploadedAt.toLocaleString('ja-JP', {
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </div>
                  </div>
                  {getStatusBadge(file.status)}
                </div>
                {file.tables && file.tables.length > 0 && (
                  <div className="file-tables-compact">
                    <div className="tables-label-compact">テーブル:</div>
                    <div className="tables-list-compact">
                      {file.tables.map((table) => (
                        <span key={table} className="table-tag-compact">
                          {table}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
                <div className="file-card-footer-compact">
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
                    onClick={() => handleDeleteUploadedFile(file.id)}
                  >
                    削除
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="stats-section">
            <div className="stats-grid-modal">
              <div className="stat-item-modal">
                <div className="stat-value-modal">{uploadedFiles.length}</div>
                <div className="stat-label-modal">総ファイル数</div>
              </div>
              <div className="stat-item-modal">
                <div className="stat-value-modal">
                  {uploadedFiles.filter((f) => f.status === 'completed').length}
                </div>
                <div className="stat-label-modal">解析完了</div>
              </div>
              <div className="stat-item-modal">
                <div className="stat-value-modal">
                  {uploadedFiles.reduce((sum, f) => sum + (f.tables?.length || 0), 0)}
                </div>
                <div className="stat-label-modal">テーブル数</div>
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content modal-content-large" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">SQLファイル管理</h2>
          <button className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="modal-tabs">
          <button
            className={`modal-tab ${activeTab === 'list' ? 'active' : ''}`}
            onClick={() => setActiveTab('list')}
          >
            📋 アップロード済み ({uploadedFiles.length})
          </button>
          <button
            className={`modal-tab ${activeTab === 'upload' ? 'active' : ''}`}
            onClick={() => setActiveTab('upload')}
          >
            📤 新規アップロード
          </button>
        </div>

        <div className="modal-body">
          {activeTab === 'upload' ? renderUploadTab() : renderListTab()}
        </div>

        {activeTab === 'upload' && (
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={onClose}>
              キャンセル
            </button>
            <button
              className="btn btn-primary"
              onClick={handleUploadAll}
              disabled={files.length === 0}
            >
              アップロード ({files.length}件)
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default SqlUploadModal;
