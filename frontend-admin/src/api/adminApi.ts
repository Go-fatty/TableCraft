/**
 * 管理画面API クライアント
 * バックエンド管理画面APIとの連携
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082';

// ============================================
// Phase 1: SQL管理API
// ============================================

export interface UploadSqlFileRequest {
  file: File;
  sqlType: 'dump' | 'schema' | 'migration';
  dbmsType: 'mysql' | 'oracle' | 'postgresql';
  uploadedBy?: string;
}

export interface UploadSqlFileResponse {
  success: boolean;
  data?: {
    sqlFileId: number;
    fileName: string;
    fileSize: number;
    parseStatus: string;
    uploadedAt: string;
  };
  error?: string;
}

export interface ParseSqlFileRequest {
  sqlFileId: number;
}

export interface ParseSqlFileResponse {
  success: boolean;
  data?: {
    sqlFileId: number;
    parseStatus: string;
    tableCount: number;
    tables: string[];
    errorMessage?: string;
  };
  error?: string;
}

export interface SqlFileInfo {
  sqlFileId: number;
  fileName: string;
  fileSize: number;
  sqlType: string;
  dbmsType: string;
  parseStatus: string;
  uploadedBy: string;
  uploadedAt: string;
  tables?: string[];  // 解析済みテーブル一覧（オプション）
}

export interface ListSqlFilesResponse {
  success: boolean;
  data?: SqlFileInfo[];
  error?: string;
}

export interface SqlFileDetail extends SqlFileInfo {
  sqlContent: string;
  parseResult?: string;
  errorMessage?: string;
}

export interface GetSqlFileResponse {
  success: boolean;
  data?: SqlFileDetail;
  error?: string;
}

/** @deprecated Use TableDefinition (INFORMATION_SCHEMA based) instead */
export interface LegacyTableDefinition {
  tableId: number;
  tableName: string;
  schemaName?: string;
  tableStructure: string;
  foreignKeys: string;
  indexes: string;
  createdAt: string;
}

export interface GetTableDefinitionsResponse {
  success: boolean;
  data?: LegacyTableDefinition[];
  error?: string;
}

/**
 * SQLファイルをアップロード
 */
export async function uploadSqlFile(request: UploadSqlFileRequest): Promise<UploadSqlFileResponse> {
  const formData = new FormData();
  formData.append('file', request.file);
  formData.append('sqlType', request.sqlType);
  formData.append('dbmsType', request.dbmsType);
  if (request.uploadedBy) {
    formData.append('uploadedBy', request.uploadedBy);
  }

  const response = await fetch(`${API_BASE_URL}/api/admin/sql/upload`, {
    method: 'POST',
    body: formData,
  });

  return response.json();
}

/**
 * SQL解析を実行
 */
export async function parseSqlFile(request: ParseSqlFileRequest): Promise<ParseSqlFileResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/sql/parse`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return response.json();
}

/**
 * アップロード済みSQLファイル一覧を取得
 */
export async function listSqlFiles(): Promise<ListSqlFilesResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/sql/list`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({}),
  });

  return response.json();
}

/**
 * 特定のSQLファイル詳細を取得
 */
export async function getSqlFile(sqlFileId: number): Promise<GetSqlFileResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/sql/get`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ sqlFileId }),
  });

  return response.json();
}

/**
 * SQLファイルを削除
 */
export async function deleteSqlFile(sqlFileId: number): Promise<{ success: boolean; message?: string; error?: string }> {
  const response = await fetch(`${API_BASE_URL}/api/admin/sql/delete`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ sqlFileId }),
  });

  return response.json();
}

/**
 * 全てのSQLファイルとテーブル定義をクリア
 */
export async function clearAllSqlFiles(): Promise<{ success: boolean; message?: string; error?: string }> {
  const response = await fetch(`${API_BASE_URL}/api/admin/sql/clear-all`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  return response.json();
}

/**
 * テーブル定義一覧を取得
 */
export async function getTableDefinitions(sqlFileId: number): Promise<GetTableDefinitionsResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/sql/tables`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ sqlFileId }),
  });

  return response.json();
}

/**
 * 特定のテーブル定義詳細を取得
 */
export async function getTableDetail(sqlFileId: number, tableName: string): Promise<GetSqlFileResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/sql/table-detail`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ sqlFileId, tableName }),
  });

  return response.json();
}

// ============================================
// Phase 2: 設定生成API
// ============================================

export interface GenerateConfigRequest {
  tableIds: number[];
  options?: {
    defaultIcon?: string;
    defaultColor?: string;
    defaultPageSize?: number;
    maxListColumns?: number;
  };
  saveToFile?: boolean;
  filename?: string;
}

export interface GenerateConfigResponse {
  success: boolean;
  data?: any;
  message?: string;
  error?: string;
}

/**
 * table-config.jsonを生成
 */
export async function generateTableConfig(request: GenerateConfigRequest): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/generate-table-config`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return response.json();
}

/**
 * ui-config.jsonを生成
 */
export async function generateUiConfig(request: GenerateConfigRequest): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/generate-ui-config`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return response.json();
}

/**
 * validation-config.jsonを生成
 */
export async function generateValidationConfig(request: Omit<GenerateConfigRequest, 'options'>): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/generate-validation-config`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return response.json();
}

/**
 * 既存の設定ファイルを取得
 */
export async function getConfig(filename: string): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/get`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ filename }),
  });

  return response.json();
}

/**
 * 設定ファイルを更新
 */
export async function updateConfig(filename: string, config: any): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/update`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ filename, config }),
  });

  return response.json();
}

/**
 * UI設定(プロジェクト名)をtable-config.jsonのproject.nameに保存
 */
export async function updateProjectName(projectName: string): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/generate-ui-config`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ projectName }),
  });
  return response.json();
}

/**
 * すべての設定ファイルを一括生成
 */
export async function generateAllConfigs(request: GenerateConfigRequest): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/generate-all`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return response.json();
}

/**
 * テーブル構造をDBに適用
 */
export async function applySchema(tableIds: number[]): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/schema/apply`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ tableIds }),
  });

  return response.json();
}

/**
 * デフォルト設定を読み込み
 */
export async function loadDefaultConfig(filename: string): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/default/${filename}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  return response.json();
}

/**
 * テーブルテンプレート一覧を取得
 */
export async function loadTableTemplates(): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/templates`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  return response.json();
}

/**
 * デフォルト設定で初期化
 */
export async function initializeWithDefaults(): Promise<GenerateConfigResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/initialize`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  return response.json();
}

// ============================================
// Phase 4: テーブル定義管理API（INFORMATION_SCHEMAベース）
// ============================================

/**
 * Column definition for table creation request
 */
export interface ColumnDefinition {
  columnName: string;
  dataType: string;
  nullable: boolean;
  primaryKey: boolean;
  autoIncrement: boolean;
  defaultValue?: string;
  comment?: string;
}

/**
 * Column information from INFORMATION_SCHEMA
 */
export interface ColumnInfo {
  columnName: string;
  dataType: string;
  columnType: string;
  nullable: boolean;
  primaryKey: boolean;
  autoIncrement: boolean;
  defaultValue?: string;
  comment?: string;
}

/**
 * Table definition response (schema + UI settings)
 */
export interface TableDefinition {
  id?: number;
  tableName: string;
  displayName: string;
  description?: string;
  columns: ColumnInfo[];
  
  // UI Settings
  enableSearch?: boolean;
  enableSort?: boolean;
  enablePagination?: boolean;
  pageSize?: number;
  allowCreate?: boolean;
  allowEdit?: boolean;
  allowDelete?: boolean;
  allowBulk?: boolean;
  
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Table creation request
 */
export interface TableCreationRequest {
  tableName: string;
  displayName: string;
  description?: string;
  columns: ColumnDefinition[];
  
  // UI Settings
  enableSearch?: boolean;
  enableSort?: boolean;
  enablePagination?: boolean;
  pageSize?: number;
  allowCreate?: boolean;
  allowEdit?: boolean;
  allowDelete?: boolean;
  allowBulk?: boolean;
}

/**
 * Table UI settings update request
 */
export interface TableUISettingsUpdateRequest {
  displayName?: string;
  description?: string;
  enableSearch?: boolean;
  enableSort?: boolean;
  enablePagination?: boolean;
  pageSize?: number;
  allowCreate?: boolean;
  allowEdit?: boolean;
  allowDelete?: boolean;
  allowBulk?: boolean;
}

export interface TableResponse {
  success: boolean;
  data?: TableDefinition;
  message?: string;
  error?: string;
}

export interface TableListResponse {
  success: boolean;
  data?: TableDefinition[];
  error?: string;
}

// Legacy types for backward compatibility
/** @deprecated Use TableDefinition instead */
export type ManualTableDefinition = TableDefinition;
/** @deprecated Use TableCreationRequest instead */
export type TableDefinitionRequest = TableCreationRequest;
/** @deprecated Use TableResponse instead */
export type ManualTableResponse = TableResponse;
/** @deprecated Use TableListResponse instead */
export type ManualTableListResponse = TableListResponse;

/**
 * テーブル定義を新規作成（INFORMATION_SCHEMAベース）
 */
export async function createTable(request: TableCreationRequest): Promise<TableResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/tables/create`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return response.json();
}

/**
 * テーブル定義一覧を取得（INFORMATION_SCHEMA + UI設定）
 */
export async function listTables(): Promise<TableListResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/tables/list`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({}),
  });

  return response.json();
}

/**
 * 特定のテーブル定義を取得
 */
export async function getTable(tableId: number): Promise<TableResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/tables/get`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ tableId }),
  });

  return response.json();
}

/**
 * テーブルのUI設定を更新（テーブル構造は変更しない）
 */
export async function updateTable(tableId: number, request: TableUISettingsUpdateRequest): Promise<TableResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/tables/update/${tableId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return response.json();
}

/**
 * テーブルを削除（実テーブル + UI設定）
 */
export async function deleteTable(tableId: number): Promise<{ success: boolean; message?: string; error?: string }> {
  const response = await fetch(`${API_BASE_URL}/api/admin/tables/delete/${tableId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  return response.json();
}

// ============================================
// Phase 3: スクリプト管理API
// ============================================

export interface ScriptParameter {
  name: string;
  type: 'string' | 'number' | 'boolean' | 'object';
  required: boolean;
  description?: string;
}

export interface ScriptInfo {
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

export interface ScriptListResponse {
  success: boolean;
  data?: ScriptInfo[];
  message?: string;
  error?: string;
}

export interface ScriptDetailResponse {
  success: boolean;
  data?: ScriptInfo;
  message?: string;
  error?: string;
}

export interface CreateUpdateScriptRequest {
  scriptName: string;
  scriptType: 'javascript' | 'groovy' | 'sql';
  description: string;
  scriptContent: string;
  parameters: ScriptParameter[];
  returnType: string;
  isActive: boolean;
}

/**
 * スクリプト一覧を取得
 */
export async function listScripts(): Promise<ScriptListResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/scripts/list`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({}),
  });

  return response.json();
}

/**
 * スクリプト詳細を取得
 */
export async function getScript(scriptId: string): Promise<ScriptDetailResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/scripts/get`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ scriptId }),
  });

  return response.json();
}

/**
 * 新規スクリプトを作成
 */
export async function createScript(request: CreateUpdateScriptRequest): Promise<ScriptDetailResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/scripts/create`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return response.json();
}

/**
 * スクリプトを更新
 */
export async function updateScript(scriptId: string, request: CreateUpdateScriptRequest): Promise<ScriptDetailResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/scripts/update`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ scriptId, ...request }),
  });

  return response.json();
}

/**
 * スクリプトを削除
 */
export async function deleteScript(scriptId: string): Promise<{ success: boolean; message?: string; error?: string }> {
  const response = await fetch(`${API_BASE_URL}/api/admin/scripts/delete`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ scriptId }),
  });

  return response.json();
}

/**
 * スクリプトをテスト実行
 */
export async function testScript(scriptId: string, parameters?: any): Promise<{ success: boolean; result?: any; error?: string }> {
  const response = await fetch(`${API_BASE_URL}/api/admin/scripts/test`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ scriptId, parameters }),
  });

  return response.json();
}

// ============================================
// テンプレート管理API
// ============================================

export interface TableTemplate {
  name: string;
  description: string;
  columns: ColumnDefinition[];
}

export interface TableTemplatesResponse {
  success: boolean;
  data?: {
    templates: Record<string, TableTemplate>;
  };
  error?: string;
}

/**
 * テーブルテンプレート一覧を取得
 */
export async function getTableTemplates(): Promise<TableTemplatesResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/config/templates`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  return response.json();
}
