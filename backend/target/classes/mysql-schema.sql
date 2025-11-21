-- TableCraft MySQL Database Schema
-- 複合キー対応バージョン

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age INT,
    phone VARCHAR(20),
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_created_date (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order INT DEFAULT 0,
    INDEX idx_categories_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category_id BIGINT,
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_products_category (category_id),
    INDEX idx_products_price (price),
    INDEX idx_products_active (is_active),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Order details table (複合主キー)
CREATE TABLE IF NOT EXISTS order_details (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, product_id),
    INDEX idx_order_details_order (order_id),
    INDEX idx_order_details_product (product_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inventory logs table (3つの複合キー)
CREATE TABLE IF NOT EXISTS inventory_logs (
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    quantity INT NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    PRIMARY KEY (warehouse_id, product_id, log_date),
    INDEX idx_inventory_logs_product (product_id),
    INDEX idx_inventory_logs_date (log_date),
    INDEX idx_inventory_logs_operation (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sales matrix table (4つの複合キー)
CREATE TABLE IF NOT EXISTS sales_matrix (
    region_id INT NOT NULL,
    year_num INT NOT NULL,
    quarter_num INT NOT NULL,
    product_category_id INT NOT NULL,
    total_sales DECIMAL(15,2) NOT NULL,
    sales_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (region_id, year_num, quarter_num, product_category_id),
    INDEX idx_sales_matrix_year (year_num),
    INDEX idx_sales_matrix_quarter (quarter_num),
    INDEX idx_sales_matrix_sales (total_sales)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Detailed analytics table (5つの複合キー)
CREATE TABLE IF NOT EXISTS detailed_analytics (
    tenant_id INT NOT NULL,
    year_num INT NOT NULL,
    month_num INT NOT NULL,
    department_id INT NOT NULL,
    metric_type_id INT NOT NULL,
    metric_value DECIMAL(20,4) NOT NULL,
    calculation_method VARCHAR(50),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, year_num, month_num, department_id, metric_type_id),
    INDEX idx_analytics_tenant_year (tenant_id, year_num),
    INDEX idx_analytics_department (department_id),
    INDEX idx_analytics_metric_type (metric_type_id),
    INDEX idx_analytics_updated (last_updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;