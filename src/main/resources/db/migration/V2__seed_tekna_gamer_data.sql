-- Seed initial categories for Tekna Gamer
INSERT INTO categories (id, name, description, created_by)
VALUES 
    (UUID_TO_BIN('c1000000-0000-0000-0000-000000000001'), 'Periféricos gamer', 'Mouses, teclados mecánicos y mousepads de alto rendimiento', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('c1000000-0000-0000-0000-000000000002'), 'Monitores', 'Monitores gamer con alta tasa de refresco y baja latencia', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('c1000000-0000-0000-0000-000000000003'), 'Audio y streaming', 'Auriculares con sonido envolvente y micrófonos de estudio', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('c1000000-0000-0000-0000-000000000004'), 'Componentes de pc', 'Tarjetas gráficas, procesadores y fuentes certificadas', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('c1000000-0000-0000-0000-000000000005'), 'Sillas y ergonomía', 'Sillas ergonómicas diseñadas para largas sesiones gaming', UUID_TO_BIN('00000000-0000-0000-0000-000000000000'))
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Seed initial suppliers for Tekna Gamer
INSERT INTO suppliers (id, name, contact_name, email, phone, address, created_by)
VALUES
    (UUID_TO_BIN('d1000000-0000-0000-0000-000000000001'), 'Logitech Gaming Corp', 'Carlos Mendoza', 'cmendoza@logitech-dist.com', '+51 987 654 321', 'Av. Javier Prado Este 2450, Lima', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('d1000000-0000-0000-0000-000000000002'), 'Razer Distributor Peru', 'Andrea Paredes', 'aparedes@razer-latam.pe', '+51 912 345 678', 'Av. República de Panamá 3500, Lima', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('d1000000-0000-0000-0000-000000000003'), 'Asus ROG Wholesale', 'Fernando Quispe', 'fquispe@asuswholesale.pe', '+51 998 877 665', 'Calle Las Begonias 441, San Isidro', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('d1000000-0000-0000-0000-000000000004'), 'HyperX Technologies', 'Lucia Ramirez', 'lramirez@hyperxtech.com', '+51 944 556 677', 'Av. Primavera 1230, Surco', UUID_TO_BIN('00000000-0000-0000-0000-000000000000'))
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Seed initial products for Tekna Gamer
INSERT INTO products (id, category_id, sku, name, description, price, stock, min_stock, created_by)
VALUES
    (UUID_TO_BIN('e1000000-0000-0000-0000-000000000001'), UUID_TO_BIN('c1000000-0000-0000-0000-000000000001'), 'MOU-LOG-G502', 'Mouse Logitech G502 Hero Lightspeed', 'Sensor HERO 25K, 11 botones programables, tecnología inalámbrica', 389.90, 18, 5, UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('e1000000-0000-0000-0000-000000000002'), UUID_TO_BIN('c1000000-0000-0000-0000-000000000001'), 'KB-RZR-HUNT', 'Teclado mecánico Razer Huntsman Mini 60%', 'Switches ópticos lineales Razer, retroiluminación Chroma RGB', 499.00, 3, 5, UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('e1000000-0000-0000-0000-000000000003'), UUID_TO_BIN('c1000000-0000-0000-0000-000000000002'), 'MON-ASU-27VG', 'Monitor gamer Asus TUF Gaming 27" 165Hz', 'Panel IPS QHD (2560x1440), 1ms MPRT, compatibilidad G-Sync', 1299.00, 2, 4, UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('e1000000-0000-0000-0000-000000000004'), UUID_TO_BIN('c1000000-0000-0000-0000-000000000003'), 'AUD-HYP-CLD2', 'Auriculares HyperX Cloud II Wireless', 'Audio espacial DTS Headphone:X, transductores de 53 mm, micrófono cancelador', 459.00, 12, 6, UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('e1000000-0000-0000-0000-000000000005'), UUID_TO_BIN('c1000000-0000-0000-0000-000000000003'), 'MIC-HYP-QUAD', 'Micrófono USB HyperX QuadCast S RGB', 'Patrones polares seleccionables, suspensión antivibración, filtro antipop interno', 649.00, 1, 4, UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('e1000000-0000-0000-0000-000000000006'), UUID_TO_BIN('c1000000-0000-0000-0000-000000000004'), 'GPU-ASU-4070', 'Tarjeta de video Asus Dual GeForce RTX 4070 12GB', 'Arquitectura Ada Lovelace, DLSS 3, ventiladores Axial-tech', 2899.00, 0, 3, UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('e1000000-0000-0000-0000-000000000007'), UUID_TO_BIN('c1000000-0000-0000-0000-000000000005'), 'CHR-DXR-PR01', 'Silla gamer ergonómica DXRacer Master Series', 'Cuero sintético microfibra, soporte lumbar integrado, reposabrazos 4D', 1450.00, 8, 3, UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('e1000000-0000-0000-0000-000000000008'), UUID_TO_BIN('c1000000-0000-0000-0000-000000000001'), 'PAD-RZR-STRD', 'Mousepad Razer Gigantus V2 XXL', 'Superficie de tela texturizada microtejida, base de goma antideslizante', 149.00, 25, 8, UUID_TO_BIN('00000000-0000-0000-0000-000000000000'))
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Seed initial inventory movements
INSERT INTO inventory_movements (id, product_id, supplier_id, movement_type, quantity, previous_stock, new_stock, unit_price, document_number, reason, created_by)
VALUES
    (UUID_TO_BIN('f1000000-0000-0000-0000-000000000001'), UUID_TO_BIN('e1000000-0000-0000-0000-000000000001'), UUID_TO_BIN('d1000000-0000-0000-0000-000000000001'), 'ENTRY', 20, 0, 20, 290.00, 'F001-000452', 'Compra inicial de lote a distribuidor oficial', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('f1000000-0000-0000-0000-000000000002'), UUID_TO_BIN('e1000000-0000-0000-0000-000000000001'), NULL, 'EXIT', 2, 20, 18, 389.90, 'B001-001204', 'Venta a cliente en tienda física', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('f1000000-0000-0000-0000-000000000003'), UUID_TO_BIN('e1000000-0000-0000-0000-000000000002'), UUID_TO_BIN('d1000000-0000-0000-0000-000000000002'), 'ENTRY', 10, 0, 10, 380.00, 'F002-000981', 'Adquisición de teclados mecánicos compactos', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('f1000000-0000-0000-0000-000000000004'), UUID_TO_BIN('e1000000-0000-0000-0000-000000000002'), NULL, 'EXIT', 7, 10, 3, 499.00, 'B001-001210', 'Salida por ventas online - alcanzó nivel mínimo', UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
    (UUID_TO_BIN('f1000000-0000-0000-0000-000000000005'), UUID_TO_BIN('e1000000-0000-0000-0000-000000000006'), NULL, 'EXIT', 5, 5, 0, 2899.00, 'F001-000512', 'Venta por ensamblaje corporativo - stock agotado', UUID_TO_BIN('00000000-0000-0000-0000-000000000000'))
ON DUPLICATE KEY UPDATE reason = VALUES(reason);
