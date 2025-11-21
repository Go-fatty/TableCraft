-- Generated INDEX statements

CREATE UNIQUE INDEX idx_users_email_unique ON users (email);
CREATE INDEX idx_products_category_id_fk ON products (category_id);
CREATE INDEX idx_order_details_order_id_fk ON order_details (order_id);
CREATE INDEX idx_order_details_product_id_fk ON order_details (product_id);