-- Generate UUIDs for all initial seed entities
SET @system_user_id = UUID_TO_BIN(UUID());

SET @user_admin_id = UUID_TO_BIN(UUID());
SET @user_gamer_id = UUID_TO_BIN(UUID());

SET @cat_peripherals = UUID_TO_BIN(UUID());
SET @cat_monitors = UUID_TO_BIN(UUID());
SET @cat_audio = UUID_TO_BIN(UUID());
SET @cat_components = UUID_TO_BIN(UUID());
SET @cat_chairs = UUID_TO_BIN(UUID());

SET @sup_logitech = UUID_TO_BIN(UUID());
SET @sup_razer = UUID_TO_BIN(UUID());
SET @sup_asus = UUID_TO_BIN(UUID());
SET @sup_hyperx = UUID_TO_BIN(UUID());

SET @prod_mouse_g502 = UUID_TO_BIN(UUID());
SET @prod_keyboard_huntsman = UUID_TO_BIN(UUID());
SET @prod_monitor_asus = UUID_TO_BIN(UUID());
SET @prod_headset_hyperx = UUID_TO_BIN(UUID());
SET @prod_microphone_hyperx = UUID_TO_BIN(UUID());
SET @prod_gpu_asus = UUID_TO_BIN(UUID());
SET @prod_chair_dxracer = UUID_TO_BIN(UUID());
SET @prod_mousepad_razer = UUID_TO_BIN(UUID());

SET @mov_entry_mouse = UUID_TO_BIN(UUID());
SET @mov_exit_mouse = UUID_TO_BIN(UUID());
SET @mov_entry_keyboard = UUID_TO_BIN(UUID());
SET @mov_exit_keyboard = UUID_TO_BIN(UUID());
SET @mov_exit_gpu = UUID_TO_BIN(UUID());

SET @alert_keyboard = UUID_TO_BIN(UUID());
SET @alert_monitor = UUID_TO_BIN(UUID());
SET @alert_microphone = UUID_TO_BIN(UUID());
SET @alert_gpu = UUID_TO_BIN(UUID());

-- Seed initial admin and demo user
-- Default password: password (BCrypt: $2a$10$yK1tujj8KjpjcX.f9xuh9uhkErK4drhHsGY0OTL4p7Qomyj/N83Jq)
INSERT INTO users (id, username, password, first_name, last_name, role, is_active, created_by)
VALUES (@user_admin_id, 'admin@tekna.com', '$2a$10$yK1tujj8KjpjcX.f9xuh9uhkErK4drhHsGY0OTL4p7Qomyj/N83Jq', 'Administrador', 'Sistema', 'ADMIN', TRUE, @system_user_id),
       (@user_gamer_id, 'gamer@tekna.com', '$2a$10$yK1tujj8KjpjcX.f9xuh9uhkErK4drhHsGY0OTL4p7Qomyj/N83Jq', 'Gamer', 'Pro', 'USER', TRUE, @system_user_id);

-- Seed initial categories for Tekna Gamer
INSERT INTO categories (id, name, description, created_by)
VALUES 
    (@cat_peripherals, 'Periféricos gamer', 'Mouses, teclados mecánicos y mousepads de alto rendimiento', @system_user_id),
    (@cat_monitors, 'Monitores', 'Monitores gamer con alta tasa de refresco y baja latencia', @system_user_id),
    (@cat_audio, 'Audio y streaming', 'Auriculares con sonido envolvente y micrófonos de estudio', @system_user_id),
    (@cat_components, 'Componentes de pc', 'Tarjetas gráficas, procesadores y fuentes certificadas', @system_user_id),
    (@cat_chairs, 'Sillas y ergonomía', 'Sillas ergonómicas diseñadas para largas sesiones gaming', @system_user_id);

-- Seed initial suppliers for Tekna Gamer
INSERT INTO suppliers (id, name, contact_name, email, phone, address, created_by)
VALUES
    (@sup_logitech, 'Logitech Gaming Corp', 'Carlos Mendoza', 'cmendoza@logitech-dist.com', '+51 987 654 321', 'Av. Javier Prado Este 2450, Lima', @system_user_id),
    (@sup_razer, 'Razer Distributor Peru', 'Andrea Paredes', 'aparedes@razer-latam.pe', '+51 912 345 678', 'Av. República de Panamá 3500, Lima', @system_user_id),
    (@sup_asus, 'Asus ROG Wholesale', 'Fernando Quispe', 'fquispe@asuswholesale.pe', '+51 998 877 665', 'Calle Las Begonias 441, San Isidro', @system_user_id),
    (@sup_hyperx, 'HyperX Technologies', 'Lucia Ramirez', 'lramirez@hyperxtech.com', '+51 944 556 677', 'Av. Primavera 1230, Surco', @system_user_id);

-- Seed initial products for Tekna Gamer
INSERT INTO products (id, category_id, sku, name, description, price, stock, min_stock, created_by)
VALUES
    (@prod_mouse_g502, @cat_peripherals, 'MOU-LOG-G502', 'Mouse Logitech G502 Hero Lightspeed', 'Sensor HERO 25K, 11 botones programables, tecnología inalámbrica', 389.90, 18, 5, @system_user_id),
    (@prod_keyboard_huntsman, @cat_peripherals, 'KB-RZR-HUNT', 'Teclado mecánico Razer Huntsman Mini 60%', 'Switches ópticos lineales Razer, retroiluminación Chroma RGB', 499.00, 3, 5, @system_user_id),
    (@prod_monitor_asus, @cat_monitors, 'MON-ASU-27VG', 'Monitor gamer Asus TUF Gaming 27" 165Hz', 'Panel IPS QHD (2560x1440), 1ms MPRT, compatibilidad G-Sync', 1299.00, 2, 4, @system_user_id),
    (@prod_headset_hyperx, @cat_audio, 'AUD-HYP-CLD2', 'Auriculares HyperX Cloud II Wireless', 'Audio espacial DTS Headphone:X, transductores de 53 mm, micrófono cancelador', 459.00, 12, 6, @system_user_id),
    (@prod_microphone_hyperx, @cat_audio, 'MIC-HYP-QUAD', 'Micrófono USB HyperX QuadCast S RGB', 'Patrones polares seleccionables, suspensión antivibración, filtro antipop interno', 649.00, 1, 4, @system_user_id),
    (@prod_gpu_asus, @cat_components, 'GPU-ASU-4070', 'Tarjeta de video Asus Dual GeForce RTX 4070 12GB', 'Arquitectura Ada Lovelace, DLSS 3, ventiladores Axial-tech', 2899.00, 0, 3, @system_user_id),
    (@prod_chair_dxracer, @cat_chairs, 'CHR-DXR-PR01', 'Silla gamer ergonómica DXRacer Master Series', 'Cuero sintético microfibra, soporte lumbar integrado, reposabrazos 4D', 1450.00, 8, 3, @system_user_id),
    (@prod_mousepad_razer, @cat_peripherals, 'PAD-RZR-STRD', 'Mousepad Razer Gigantus V2 XXL', 'Superficie de tela texturizada microtejida, base de goma antideslizante', 149.00, 25, 8, @system_user_id);

-- Seed initial inventory movements
INSERT INTO inventory_movements (id, product_id, supplier_id, movement_type, quantity, previous_stock, new_stock, unit_price, document_number, reason, created_by)
VALUES
    (@mov_entry_mouse, @prod_mouse_g502, @sup_logitech, 'ENTRY', 20, 0, 20, 290.00, 'F001-000452', 'Compra inicial de lote a distribuidor oficial', @system_user_id),
    (@mov_exit_mouse, @prod_mouse_g502, NULL, 'EXIT', 2, 20, 18, 389.90, 'B001-001204', 'Venta a cliente en tienda física', @system_user_id),
    (@mov_entry_keyboard, @prod_keyboard_huntsman, @sup_razer, 'ENTRY', 10, 0, 10, 380.00, 'F002-000981', 'Adquisición de teclados mecánicos compactos', @system_user_id),
    (@mov_exit_keyboard, @prod_keyboard_huntsman, NULL, 'EXIT', 7, 10, 3, 499.00, 'B001-001210', 'Salida por ventas online - alcanzó nivel mínimo', @system_user_id),
    (@mov_exit_gpu, @prod_gpu_asus, NULL, 'EXIT', 5, 5, 0, 2899.00, 'F001-000512', 'Venta por ensamblaje corporativo - stock agotado', @system_user_id);

-- Seed initial stock alerts for products below or equal to min_stock
INSERT INTO alerts (id, product_id, stock_recorded, min_stock_snapshot, status, created_by)
VALUES
    (@alert_keyboard, @prod_keyboard_huntsman, 3, 5, 'PENDING', @system_user_id),
    (@alert_monitor, @prod_monitor_asus, 2, 4, 'PENDING', @system_user_id),
    (@alert_microphone, @prod_microphone_hyperx, 1, 4, 'PENDING', @system_user_id),
    (@alert_gpu, @prod_gpu_asus, 0, 3, 'PENDING', @system_user_id);
