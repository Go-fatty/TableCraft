-- Generated CREATE TABLE statements for h2
-- Project: AutoStack Builder Sample
-- Generated at: 2025-11-18 13:09:57

-- ユーザー
-- Users
-- システム利用者の管理
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age INT,
    phone VARCHAR(20),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- カテゴリー
-- Categories
-- 商品カテゴリーの管理
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description LONGTEXT,
    sort_order INT DEFAULT '0'
);

-- 商品
-- Products
-- 商品情報の管理
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category_id BIGINT,
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT '0',
    is_active BOOLEAN NOT NULL DEFAULT 'true'
);

-- 注文明細
-- Order Details
-- 注文明細情報（複合主キーのサンプル）
CREATE TABLE IF NOT EXISTS order_details (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT '1',
    unit_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, product_id)
);


-- Generated INDEX statements

CREATE UNIQUE INDEX idx_users_email_unique ON users (email);
CREATE INDEX idx_products_category_id_fk ON products (category_id);
CREATE INDEX idx_order_details_order_id_fk ON order_details (order_id);
CREATE INDEX idx_order_details_product_id_fk ON order_details (product_id);

-- Generated FOREIGN KEY constraints

ALTER TABLE products 
    ADD CONSTRAINT fk_products_category_id 
    FOREIGN KEY (category_id) 
    REFERENCES categories (id)
    ON DELETE SET NULL 
    ON UPDATE CASCADE;

-- ALTER TABLE order_details 
--     ADD CONSTRAINT fk_order_details_order_id 
--     FOREIGN KEY (order_id) 
--     REFERENCES orders (id)
--     ON DELETE CASCADE 
--     ON UPDATE CASCADE;

ALTER TABLE order_details 
    ADD CONSTRAINT fk_order_details_product_id 
    FOREIGN KEY (product_id) 
    REFERENCES products (id)
    ON DELETE CASCADE 
    ON UPDATE CASCADE;
