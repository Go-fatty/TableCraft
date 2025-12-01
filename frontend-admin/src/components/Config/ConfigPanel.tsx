import { useState, useEffect } from 'react';
import './ConfigPanel.css';
import * as adminApi from '../../api/adminApi';
import TableBuilder from '../TableBuilder/TableBuilder';

interface ConfigPanelProps {
  type: 'table' | 'field' | 'validation' | 'ui';
  title: string;
}

const ConfigPanel = ({ type, title }: ConfigPanelProps) => {
  const [projectName, setProjectName] = useState<string>('TableCraft Project');
  const [uploadedFiles, setUploadedFiles] = useState<adminApi.SqlFileInfo[]>([]);
  const [selectedTables, setSelectedTables] = useState<number[]>([]);
  const [generatedConfig, setGeneratedConfig] = useState<any>(null);
  const [showPreview, setShowPreview] = useState(false);
  const [inputMode, setInputMode] = useState<'sql' | 'manual'>('sql'); // SQLアップロードか手動入力か

  // UI設定とValidation設定は現在の入力値を参照

  // アップロード済みSQLファイル一覧を取得
  useEffect(() => {
    loadUploadedFiles();
  }, []);

  // 一括保存イベントリスナー
  useEffect(() => {
    const handleSaveAll = async () => {
      console.log('[ConfigPanel] イベント受信 - type:', type);
      await saveCurrentConfig();
    };

    window.addEventListener('save-all-configs', handleSaveAll);
    return () => {
      window.removeEventListener('save-all-configs', handleSaveAll);
    };
  }, [type, inputMode, selectedTables, generatedConfig, projectName]);

  const saveCurrentConfig = async () => {
    console.log(`[ConfigPanel] saveCurrentConfig called - type: ${type}, inputMode: ${inputMode}, projectName: ${projectName}`);
    
    try {
      // UI設定の場合
      if (type === 'ui') {
        console.log('[ConfigPanel] UI設定を保存中... projectName:', projectName);
        const response = await adminApi.updateProjectName(projectName);
        console.log('[ConfigPanel] UI設定保存結果:', response);
        return;
      }

      // Validation設定の場合
      if (type === 'validation') {
        console.log('[ConfigPanel] Validation設定を保存中...');
        const response = await adminApi.updateConfig('validation-config.json', {
          rules: {}
        });
        console.log('[ConfigPanel] Validation設定保存結果:', response);
        return;
      }

      // Table設定 & 手動入力モードの場合
      if (type === 'table' && inputMode === 'manual') {
        console.log('[ConfigPanel] Table設定: 手動入力モード - TableBuilder経由で保存済み、スキップ');
        return; // TableBuilderコンポーネント内で保存処理完結
      }

      // Table設定 & SQLアップロードモードの場合
      if (type === 'table' && selectedTables.length === 0) {
        console.log('[ConfigPanel] Table設定: テーブル未選択、スキップ');
        return; // テーブル未選択の場合はスキップ
      }

      // Table設定 & SQLアップロードモード - API経由で生成
      if (type === 'table' && selectedTables.length > 0) {
        console.log(`[ConfigPanel] Table設定を生成中... selectedTables:`, selectedTables);
        
        // ⚠️ データクリア確認ダイアログ
        const confirmed = window.confirm(
          '⚠️ 重要な警告 ⚠️\n\n' +
          'テーブル設定を保存すると、以下の処理が実行されます:\n\n' +
          '1. table-config.json を更新\n' +
          '2. データベース上のテーブルを作成/更新\n' +
          '3. 既存テーブルの場合、全データをクリア（TRUNCATE）\n\n' +
          '【注意】既存データは完全に削除されます。\n' +
          '必要に応じて事前にバックアップを取得してください。\n\n' +
          '続行しますか？'
        );

        if (!confirmed) {
          console.log('[ConfigPanel] ユーザーがキャンセルしました');
          return;
        }

        const request: adminApi.GenerateConfigRequest = {
          tableIds: selectedTables,
          saveToFile: true,
        };
        
        const response = await adminApi.generateTableConfig(request);
        console.log(`[ConfigPanel] Table設定保存結果:`, response);
        
        if (response.success && response.data) {
          setGeneratedConfig(response.data);
        }

        // ✅ DB スキーマ適用を実行
        console.log('[ConfigPanel] DBスキーマ適用を開始...');
        const schemaResponse = await adminApi.applySchema(selectedTables);
        console.log('[ConfigPanel] DBスキーマ適用結果:', schemaResponse);
        
        if (schemaResponse.success) {
          console.log('✅ DBスキーマ適用成功:', schemaResponse.data);
          
          // 結果の詳細を整形
          const data = schemaResponse.data as any;
          const resultSummary = data.results?.map((r: any) => 
            `  • ${r.tableName}: ${r.message}`
          ).join('\n') || '';
          
          alert(
            `✅ 設定保存とDBスキーマ適用が完了しました。\n\n` +
            `【処理結果】\n` +
            `成功: ${data.successCount}件\n` +
            `失敗: ${data.failureCount}件\n\n` +
            `【詳細】\n${resultSummary}`
          );
        } else {
          console.error('❌ DBスキーマ適用失敗:', schemaResponse.error);
          alert(`❌ 設定保存は成功しましたが、DBスキーマ適用でエラーが発生しました:\n${schemaResponse.error}`);
        }
      }
    } catch (error) {
      console.error(`[ConfigPanel] ${type}設定保存エラー:`, error);
      alert(`設定保存中にエラーが発生しました:\n${error}`);
    }
  };

  const loadUploadedFiles = async () => {
    try {
      const response = await adminApi.listSqlFiles();
      if (response.success && response.data) {
        setUploadedFiles(response.data);
      }
    } catch (error) {
      console.error('Failed to load uploaded files:', error);
    }
  };

  const renderTableConfig = () => (
    <div className="config-content">
      <div className="config-grid">
        <div className="config-card">
          <h4 className="config-card-title">テーブル情報</h4>
          <div className="form-group">
            <label className="form-label">テーブル名</label>
            <input type="text" className="form-input" placeholder="例: users" />
          </div>
          <div className="form-group">
            <label className="form-label">表示名</label>
            <input type="text" className="form-input" placeholder="例: ユーザー" />
          </div>
          <div className="form-group">
            <label className="form-label">説明</label>
            <textarea
              className="form-textarea"
              placeholder="テーブルの説明を入力"
              rows={3}
            />
          </div>
        </div>

        <div className="config-card">
          <h4 className="config-card-title">表示設定</h4>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              検索機能を有効化
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              ソート機能を有効化
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              ページネーションを有効化
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">1ページあたりの表示件数</label>
            <input type="number" className="form-input" defaultValue={20} />
          </div>
        </div>

        <div className="config-card">
          <h4 className="config-card-title">操作設定</h4>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              新規作成を許可
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              編集を許可
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              削除を許可
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              一括操作を許可
            </label>
          </div>
        </div>
      </div>
    </div>
  );

  const renderFieldConfig = () => (
    <div className="config-content">
      <div className="config-grid">
        <div className="config-card">
          <h4 className="config-card-title">フィールド基本情報</h4>
          <div className="form-group">
            <label className="form-label">フィールド名</label>
            <select className="form-select">
              <option>フィールドを選択...</option>
              <option>id</option>
              <option>name</option>
              <option>email</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">表示名</label>
            <input type="text" className="form-input" placeholder="例: ユーザー名" />
          </div>
          <div className="form-group">
            <label className="form-label">データ型</label>
            <select className="form-select">
              <option>VARCHAR</option>
              <option>INT</option>
              <option>DATE</option>
              <option>BOOLEAN</option>
            </select>
          </div>
        </div>

        <div className="config-card">
          <h4 className="config-card-title">表示制御</h4>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              一覧画面に表示
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              詳細画面に表示
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              編集可能
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              必須項目
            </label>
          </div>
        </div>

        <div className="config-card">
          <h4 className="config-card-title">入力制御</h4>
          <div className="form-group">
            <label className="form-label">入力タイプ</label>
            <select className="form-select">
              <option>テキスト</option>
              <option>数値</option>
              <option>日付</option>
              <option>選択</option>
              <option>チェックボックス</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">プレースホルダー</label>
            <input type="text" className="form-input" placeholder="入力のヒント" />
          </div>
          <div className="form-group">
            <label className="form-label">最大文字数</label>
            <input type="number" className="form-input" />
          </div>
        </div>
      </div>
    </div>
  );

  const renderValidationConfig = () => (
    <div className="config-content">
      <div className="config-grid">
        <div className="config-card">
          <h4 className="config-card-title">基本バリデーション</h4>
          <div className="form-group">
            <label className="form-label">対象フィールド</label>
            <select className="form-select">
              <option>フィールドを選択...</option>
              <option>email</option>
              <option>phone</option>
              <option>age</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              必須チェック
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              メールアドレス形式
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              数値のみ
            </label>
          </div>
        </div>

        <div className="config-card">
          <h4 className="config-card-title">範囲チェック</h4>
          <div className="form-group">
            <label className="form-label">最小値</label>
            <input type="number" className="form-input" />
          </div>
          <div className="form-group">
            <label className="form-label">最大値</label>
            <input type="number" className="form-input" />
          </div>
          <div className="form-group">
            <label className="form-label">最小文字数</label>
            <input type="number" className="form-input" />
          </div>
          <div className="form-group">
            <label className="form-label">最大文字数</label>
            <input type="number" className="form-input" />
          </div>
        </div>

        <div className="config-card">
          <h4 className="config-card-title">カスタムバリデーション</h4>
          <div className="form-group">
            <label className="form-label">正規表現パターン</label>
            <input
              type="text"
              className="form-input"
              placeholder="例: ^[0-9]{3}-[0-9]{4}$"
            />
          </div>
          <div className="form-group">
            <label className="form-label">エラーメッセージ</label>
            <input
              type="text"
              className="form-input"
              placeholder="例: 郵便番号の形式が正しくありません"
            />
          </div>
        </div>
      </div>
    </div>
  );

  const renderUiConfig = () => (
    <div className="config-content">
      <div className="config-grid">
        <div className="config-card">
          <h4 className="config-card-title">プロジェクト設定</h4>
          <div className="form-group">
            <label className="form-label">
              プロジェクト名 <span className="required">*</span>
            </label>
            <input
              type="text"
              className="form-input"
              placeholder="例: TableCraft Project"
              value={projectName}
              onChange={(e) => setProjectName(e.target.value)}
            />
            <small className="help-text">
              管理画面のタイトルや生成されるJSON設定ファイルに使用されます
            </small>
          </div>
          <div className="form-group">
            <label className="form-label">プロジェクト説明</label>
            <textarea
              className="form-textarea"
              placeholder="プロジェクトの概要を入力してください"
              rows={3}
            />
            <small className="help-text">
              プロジェクトの目的や概要を記述します（オプション）
            </small>
          </div>
          <div className="form-group">
            <label className="form-label">デフォルト言語</label>
            <select className="form-select">
              <option value="ja">日本語</option>
              <option value="en">English</option>
            </select>
          </div>
        </div>

        <div className="config-card">
          <h4 className="config-card-title">レイアウト設定</h4>
          <div className="form-group">
            <label className="form-label">レイアウトタイプ</label>
            <select className="form-select">
              <option>テーブル</option>
              <option>カード</option>
              <option>リスト</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">カラム数（カード表示時）</label>
            <select className="form-select">
              <option>2</option>
              <option>3</option>
              <option>4</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              コンパクト表示
            </label>
          </div>
        </div>

        <div className="config-card">
          <h4 className="config-card-title">テーマ設定</h4>
          <div className="form-group">
            <label className="form-label">プライマリカラー</label>
            <input type="color" className="form-input" defaultValue="#4a90e2" />
          </div>
          <div className="form-group">
            <label className="form-label">セカンダリカラー</label>
            <input type="color" className="form-input" defaultValue="#6c757d" />
          </div>
          <div className="form-group">
            <label className="form-label">フォントサイズ</label>
            <select className="form-select">
              <option>小</option>
              <option>中</option>
              <option>大</option>
            </select>
          </div>
        </div>

        <div className="config-card">
          <h4 className="config-card-title">ボタン設定</h4>
          <div className="form-group">
            <label className="form-label">
              <input type="checkbox" className="form-checkbox" />
              アイコンを表示
            </label>
          </div>
          <div className="form-group">
            <label className="form-label">ボタンサイズ</label>
            <select className="form-select">
              <option>小</option>
              <option>中</option>
              <option>大</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">ボタン配置</label>
            <select className="form-select">
              <option>左寄せ</option>
              <option>中央</option>
              <option>右寄せ</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  );

  const renderContent = () => {
    // Table設定 & 手動入力モードの場合はTableBuilderを表示
    if (type === 'table' && inputMode === 'manual') {
      return <TableBuilder />;
    }

    // その他の設定タイプは既存のフォーム表示
    switch (type) {
      case 'table':
        return renderTableConfig();
      case 'field':
        return renderFieldConfig();
      case 'validation':
        return renderValidationConfig();
      case 'ui':
        return renderUiConfig();
      default:
        return null;
    }
  };

  const handleDownload = () => {
    if (!generatedConfig) return;

    const filename = type === 'ui' ? 'ui-config.json' : 
                    type === 'validation' ? 'validation-config.json' : 
                    'table-config.json';
    
    const blob = new Blob([JSON.stringify(generatedConfig, null, 2)], {
      type: 'application/json',
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleTableSelection = (tableId: number) => {
    setSelectedTables(prev => {
      if (prev.includes(tableId)) {
        return prev.filter(id => id !== tableId);
      } else {
        return [...prev, tableId];
      }
    });
  };

  const renderTableSelector = () => (
    <div className="config-card">
      <h4 className="config-card-title">テーブル選択</h4>
      {uploadedFiles.length === 0 ? (
        <div className="alert alert-info">
          SQLファイルをアップロードしてください
        </div>
      ) : (
        <div className="table-list">
          {uploadedFiles.map(file => (
            <div key={file.sqlFileId}>
              <div className="file-header">
                <strong>{file.fileName}</strong>
                {file.tables && file.tables.length > 0 && (
                  <span className="badge">{file.tables.length} テーブル</span>
                )}
              </div>
              {file.tables && file.tables.length > 0 && (
                <div className="table-checkboxes">
                  {file.tables.map((table: string) => (
                    <label key={`${file.sqlFileId}-${table}`} className="form-label checkbox-label">
                      <input
                        type="checkbox"
                        className="form-checkbox"
                        checked={selectedTables.includes(file.sqlFileId)}
                        onChange={() => handleTableSelection(file.sqlFileId)}
                      />
                      {table}
                    </label>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );

  const renderPreview = () => {
    if (!showPreview || !generatedConfig) return null;

    return (
      <div className="config-preview">
        <h4 className="config-card-title">生成されたJSON設定</h4>
        <pre className="json-preview">
          {JSON.stringify(generatedConfig, null, 2)}
        </pre>
        <div className="btn-group">
          <button className="btn btn-secondary" onClick={() => setShowPreview(false)}>
            閉じる
          </button>
          <button className="btn btn-success" onClick={handleDownload}>
            ダウンロード
          </button>
        </div>
      </div>
    );
  };

  return (
    <div className="panel">
      <h2 className="panel-title">{title}</h2>

      {/* Table設定のみ入力モード切替を表示 */}
      {type === 'table' && (
        <div className="panel-section">
          <div className="mode-selector">
            <button 
              className={`mode-btn ${inputMode === 'sql' ? 'active' : ''}`}
              onClick={() => setInputMode('sql')}
            >
              SQLアップロード
            </button>
            <button 
              className={`mode-btn ${inputMode === 'manual' ? 'active' : ''}`}
              onClick={() => setInputMode('manual')}
            >
              手動入力
            </button>
          </div>
        </div>
      )}

      {/* Table設定 & SQLモードの場合のみテーブルセレクター表示 */}
      {type === 'table' && inputMode === 'sql' && (
        <div className="panel-section">
          {renderTableSelector()}
        </div>
      )}

      {/* Table設定 & 手動入力モード、またはUI/Validation設定の場合はフォーム表示 */}
      {((type === 'table' && inputMode === 'manual') || type !== 'table') && renderContent()}

      {renderPreview()}

      <div className="info-box">
        <p>💡 設定を入力後、画面上部の「一括保存」ボタンで保存してください</p>
      </div>
    </div>
  );
};

export default ConfigPanel;
