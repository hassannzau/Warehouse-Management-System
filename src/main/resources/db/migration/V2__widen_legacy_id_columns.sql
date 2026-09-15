-- Widens the primary/foreign key columns that a pre-V1 database (schema built by
-- Hibernate's ddl-auto=update, before Flyway existed here) still has as INT to the
-- BIGINT the Long-typed entities expect - the same type V1 already gives a brand
-- new database, so every statement below is a no-op there.

ALTER TABLE products DROP FOREIGN KEY products_ibfk_1;
ALTER TABLE products DROP FOREIGN KEY products_ibfk_2;
ALTER TABLE invoices DROP FOREIGN KEY invoices_ibfk_1;
ALTER TABLE invoice_items DROP FOREIGN KEY invoice_items_ibfk_1;
ALTER TABLE invoice_items DROP FOREIGN KEY invoice_items_ibfk_2;
ALTER TABLE stock_transactions DROP FOREIGN KEY stock_transactions_ibfk_1;

ALTER TABLE stores MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE categories MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE products MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE products MODIFY COLUMN category_id BIGINT DEFAULT NULL;
ALTER TABLE products MODIFY COLUMN store_id BIGINT NOT NULL;
ALTER TABLE invoices MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE invoices MODIFY COLUMN store_id BIGINT NOT NULL;
ALTER TABLE invoice_items MODIFY COLUMN invoice_id BIGINT NOT NULL;
ALTER TABLE invoice_items MODIFY COLUMN product_id BIGINT NOT NULL;
ALTER TABLE stock_transactions MODIFY COLUMN product_id BIGINT NOT NULL;

ALTER TABLE products ADD CONSTRAINT products_ibfk_1 FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL;
ALTER TABLE products ADD CONSTRAINT products_ibfk_2 FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE;
ALTER TABLE invoices ADD CONSTRAINT invoices_ibfk_1 FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE;
ALTER TABLE invoice_items ADD CONSTRAINT invoice_items_ibfk_1 FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE CASCADE;
ALTER TABLE invoice_items ADD CONSTRAINT invoice_items_ibfk_2 FOREIGN KEY (product_id) REFERENCES products (id);
ALTER TABLE stock_transactions ADD CONSTRAINT stock_transactions_ibfk_1 FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;
