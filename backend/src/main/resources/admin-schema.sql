-- ============================================
-- TableCraft Admin Panel Tables
-- ============================================

-- SQL File Upload Management Table
CREATE TABLE IF NOT EXISTS uploaded_sql_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    sql_type VARCHAR(50) NOT NULL,
    dbms_type VARCHAR(50) NOT NULL,
    sql_content LONGTEXT NOT NULL,
    parse_status VARCHAR(50) NOT NULL DEFAULT 'pending',
    parse_result LONGTEXT,
    error_message TEXT,
    uploaded_by VARCHAR(100),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_uploaded_at (uploaded_at),
    INDEX idx_parse_status (parse_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Parsed Table Definitions
CREATE TABLE IF NOT EXISTS parsed_table_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sql_file_id BIGINT NOT NULL,
    table_name VARCHAR(255) NOT NULL,
    table_structure LONGTEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sql_file_id) REFERENCES uploaded_sql_files(id) ON DELETE CASCADE,
    UNIQUE KEY uk_sql_file_table (sql_file_id, table_name),
    INDEX idx_table_name (table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Config Generation History
CREATE TABLE IF NOT EXISTS config_generation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_type VARCHAR(50) NOT NULL,
    config_content LONGTEXT NOT NULL,
    table_ids VARCHAR(500),
    generated_by VARCHAR(100),
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    applied BOOLEAN DEFAULT FALSE,
    applied_at TIMESTAMP NULL,
    INDEX idx_config_type (config_type),
    INDEX idx_generated_at (generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Custom Scripts Management
CREATE TABLE IF NOT EXISTS custom_scripts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    script_name VARCHAR(255) NOT NULL UNIQUE,
    script_type VARCHAR(50) NOT NULL,
    description TEXT,
    script_content LONGTEXT NOT NULL,
    parameters JSON,
    return_type VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    version INT DEFAULT 1,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_script_type (script_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Manual Table Definitions (for Table Builder)
CREATE TABLE IF NOT EXISTS manual_table_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    columns TEXT NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_table_name (table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
