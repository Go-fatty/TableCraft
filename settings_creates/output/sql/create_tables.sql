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
    order_id BIGINT NOT NULL PRIMARY KEY,
    product_id BIGINT NOT NULL PRIMARY KEY,
    quantity INT NOT NULL DEFAULT '1',
    unit_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, product_id)
);
