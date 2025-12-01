-- ========================================
-- Phase 0: データベース初期化
-- ========================================
-- 目的: INFORMATION_SCHEMAベースの新設計への移行準備
-- 実行日: 2025-12-01
-- 
-- 手順:
-- 1. 既存データのバックアップテーブル作成
-- 2. 新しいtable_ui_settingsテーブル作成
-- 3. 既存データからUI設定をマイグレーション
-- ========================================

-- ========================================
-- STEP 1: 既存データのバックアップ
-- ========================================

-- manual_table_definitionsのバックアップ
CREATE TABLE IF NOT EXISTS manual_table_definitions_backup
AS SELECT * FROM manual_table_definitions;

-- parsed_table_definitionsのバックアップ
CREATE TABLE IF NOT EXISTS parsed_table_definitions_backup
AS SELECT * FROM parsed_table_definitions;

SELECT 
    CONCAT('Backup completed: manual_table_definitions (', COUNT(*), ' rows)') AS status
FROM manual_table_definitions_backup;

SELECT 
    CONCAT('Backup completed: parsed_table_definitions (', COUNT(*), ' rows)') AS status
FROM parsed_table_definitions_backup;

-- ========================================
-- STEP 2: 新しいテーブル作成
-- ========================================

-- UI settings table (1:1 with actual MySQL tables)
CREATE TABLE IF NOT EXISTS table_ui_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL UNIQUE COLLATE utf8mb4_unicode_ci,
    display_name VARCHAR(255) NOT NULL COLLATE utf8mb4_unicode_ci,
    description TEXT COLLATE utf8mb4_unicode_ci,
    
    enable_search BOOLEAN DEFAULT TRUE,
    enable_sort BOOLEAN DEFAULT TRUE,
    enable_pagination BOOLEAN DEFAULT TRUE,
    page_size INT DEFAULT 20,
    
    allow_create BOOLEAN DEFAULT TRUE,
    allow_edit BOOLEAN DEFAULT TRUE,
    allow_delete BOOLEAN DEFAULT TRUE,
    allow_bulk BOOLEAN DEFAULT FALSE,
    
    created_by VARCHAR(100) COLLATE utf8mb4_unicode_ci,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_table_name (table_name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Table created: table_ui_settings' AS status;

-- ========================================
-- STEP 3: データマイグレーション
-- ========================================

-- manual_table_definitionsからUI設定を抽出してマイグレーション
INSERT INTO table_ui_settings (
    table_name,
    display_name,
    description,
    enable_search,
    enable_sort,
    enable_pagination,
    page_size,
    allow_create,
    allow_edit,
    allow_delete,
    allow_bulk,
    created_by,
    created_at,
    updated_at
)
SELECT 
    table_name,
    display_name,
    NULL AS description,
    COALESCE(enable_search, TRUE),
    COALESCE(enable_sort, TRUE),
    COALESCE(enable_pagination, TRUE),
    COALESCE(page_size, 20),
    COALESCE(allow_create, TRUE),
    COALESCE(allow_edit, TRUE),
    COALESCE(allow_delete, TRUE),
    COALESCE(allow_bulk, FALSE),
    created_by,
    created_at,
    updated_at
FROM manual_table_definitions
WHERE table_name NOT IN (SELECT table_name FROM table_ui_settings)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    enable_search = VALUES(enable_search),
    enable_sort = VALUES(enable_sort),
    enable_pagination = VALUES(enable_pagination),
    page_size = VALUES(page_size),
    allow_create = VALUES(allow_create),
    allow_edit = VALUES(allow_edit),
    allow_delete = VALUES(allow_delete),
    allow_bulk = VALUES(allow_bulk),
    updated_at = VALUES(updated_at);

SELECT 
    CONCAT('Migration completed: ', ROW_COUNT(), ' UI settings migrated') AS status;

-- ========================================
-- STEP 4: 検証クエリ
-- ========================================

-- Migration result verification
SELECT 
    '=== Migration Result Summary ===' AS summary;

SELECT 
    'manual_table_definitions' AS source_table,
    COUNT(*) AS record_count
FROM manual_table_definitions_backup
UNION ALL
SELECT 
    'parsed_table_definitions' AS source_table,
    COUNT(*) AS record_count
FROM parsed_table_definitions_backup
UNION ALL
SELECT 
    'table_ui_settings (新)' AS source_table,
    COUNT(*) AS record_count
FROM table_ui_settings;

-- UI settings verification (first 5 rows)
SELECT 
    '=== Migrated UI Settings (Sample 5 rows) ===' AS sample_data;

SELECT 
    table_name,
    display_name,
    enable_search,
    enable_sort,
    page_size,
    allow_create,
    allow_edit,
    allow_delete
FROM table_ui_settings
ORDER BY created_at DESC
LIMIT 5;

-- ========================================
-- Completion message
-- ========================================
SELECT 
    'Phase 0 Completed Successfully' AS message,
    'Backup, new table creation, and data migration completed.' AS details,
    'Next: Phase 1 (Backend - New Service Creation)' AS next_step;
