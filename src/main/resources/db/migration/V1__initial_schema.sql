CREATE TABLE IF NOT EXISTS categories (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by BINARY(16) NOT NULL,
    updated_at TIMESTAMP(3) NULL ON UPDATE CURRENT_TIMESTAMP(3),
    updated_by BINARY(16) NULL,
    deleted_at TIMESTAMP(3) NULL,
    deleted_by BINARY(16) NULL
);

CREATE TABLE IF NOT EXISTS suppliers (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    contact_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(30),
    address VARCHAR(255),
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by BINARY(16) NOT NULL,
    updated_at TIMESTAMP(3) NULL ON UPDATE CURRENT_TIMESTAMP(3),
    updated_by BINARY(16) NULL,
    deleted_at TIMESTAMP(3) NULL,
    deleted_by BINARY(16) NULL
);

CREATE TABLE IF NOT EXISTS products (
    id BINARY(16) NOT NULL PRIMARY KEY,
    category_id BINARY(16) NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    min_stock INT NOT NULL DEFAULT 5,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by BINARY(16) NOT NULL,
    updated_at TIMESTAMP(3) NULL ON UPDATE CURRENT_TIMESTAMP(3),
    updated_by BINARY(16) NULL,
    deleted_at TIMESTAMP(3) NULL,
    deleted_by BINARY(16) NULL,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE IF NOT EXISTS inventory_movements (
    id BINARY(16) NOT NULL PRIMARY KEY,
    product_id BINARY(16) NOT NULL,
    supplier_id BINARY(16) NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    unit_price DECIMAL(10, 2) NULL,
    document_number VARCHAR(100) NULL,
    reason VARCHAR(255) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by BINARY(16) NOT NULL,
    updated_at TIMESTAMP(3) NULL ON UPDATE CURRENT_TIMESTAMP(3),
    updated_by BINARY(16) NULL,
    deleted_at TIMESTAMP(3) NULL,
    deleted_by BINARY(16) NULL,
    CONSTRAINT fk_movements_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_movements_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
);

CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_deleted_at ON products (deleted_at);
CREATE INDEX idx_products_stock ON products (stock);
CREATE INDEX idx_movements_product ON inventory_movements (product_id);
CREATE INDEX idx_movements_supplier ON inventory_movements (supplier_id);
CREATE INDEX idx_movements_created_at ON inventory_movements (created_at);
CREATE INDEX idx_movements_type ON inventory_movements (movement_type);
