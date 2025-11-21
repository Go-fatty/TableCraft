-- Generated FOREIGN KEY constraints

ALTER TABLE products 
    ADD CONSTRAINT fk_products_category_id 
    FOREIGN KEY (category_id) 
    REFERENCES categories (id)
    ON DELETE SET NULL 
    ON UPDATE CASCADE;

ALTER TABLE order_details 
    ADD CONSTRAINT fk_order_details_order_id 
    FOREIGN KEY (order_id) 
    REFERENCES orders (id)
    ON DELETE CASCADE 
    ON UPDATE CASCADE;

ALTER TABLE order_details 
    ADD CONSTRAINT fk_order_details_product_id 
    FOREIGN KEY (product_id) 
    REFERENCES products (id)
    ON DELETE CASCADE 
    ON UPDATE CASCADE;
