-- =====================================================
-- SAKARI - Script Completo de Base de Datos
-- Ejecutar este script en MySQL Workbench o línea de comandos
-- =====================================================

-- Eliminar base de datos si existe (CUIDADO: borra todos los datos)
-- DROP DATABASE IF EXISTS sakari_db;

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS sakari_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- IMPORTANTE: Seleccionar la base de datos
USE sakari_db;

-- =====================================================
-- CREACIÓN DE TABLAS
-- =====================================================

-- Tabla de Categorías
CREATE TABLE IF NOT EXISTS categoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Productos
CREATE TABLE IF NOT EXISTS producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    imagen_url VARCHAR(500),
    personalizable BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    categoria_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(500),
    rol VARCHAR(20) DEFAULT 'USER',
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Carritos
CREATE TABLE IF NOT EXISTS carrito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Items del Carrito
CREATE TABLE IF NOT EXISTS item_carrito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carrito_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL,
    personalizacion TEXT,
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (carrito_id) REFERENCES carrito(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Pedidos
CREATE TABLE IF NOT EXISTS pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_pedido VARCHAR(50) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    total DECIMAL(10,2) NOT NULL,
    direccion_envio VARCHAR(500) NOT NULL,
    telefono_contacto VARCHAR(20),
    notas TEXT,
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Items del Pedido
CREATE TABLE IF NOT EXISTS item_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    personalizacion TEXT,
    FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Tokens de Recuperación de Contraseña
CREATE TABLE IF NOT EXISTS token_recuperacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Mensajes de Contacto
CREATE TABLE IF NOT EXISTS mensaje_contacto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    telefono VARCHAR(20),
    asunto VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    leido BOOLEAN DEFAULT FALSE,
    respondido BOOLEAN DEFAULT FALSE,
    fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura TIMESTAMP NULL,
    fecha_respuesta TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERCIÓN DE DATOS DE PRUEBA
-- =====================================================

-- Categorías
INSERT INTO categoria (nombre, descripcion, activo) VALUES
('Camisetas', 'Camisetas de poliéster sublimables en variedad de tallas', true),
('Tazas', 'Tazas de cerámica con recubrimiento especial para sublimación', true),
('Gorras', 'Gorras sublimables de diferentes estilos y colores', true),
('Cojines', 'Cojines decorativos con fundas sublimables', true),
('Llaveros', 'Llaveros personalizables con diferentes formas', true),
('Otros', 'Otros productos sublimables', true)
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion);

-- Productos con imágenes atractivas
INSERT INTO producto (nombre, descripcion, precio, stock, imagen_url, personalizable, activo, categoria_id) VALUES
('Camiseta Blanca Premium', 'Camiseta 100% poliéster blanca de alta calidad, ideal para sublimación full color. Disponible en tallas S, M, L, XL. Acabado suave y cómodo.', 8500.00, 50, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop', true, true, 1),
('Camiseta Negra Sport', 'Camiseta deportiva de poliéster negro con zonas sublimables. Perfecta para equipos deportivos. Tallas S-XL.', 9500.00, 30, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=400&h=400&fit=crop', true, true, 1),
('Taza Mágica Térmica', 'Taza mágica que revela tu diseño con el calor. Sorprende a todos con este efecto especial. Capacidad 11oz.', 6500.00, 40, 'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?w=400&h=400&fit=crop', true, true, 2),
('Taza Blanca Clásica', 'Taza de cerámica premium 11oz, superficie perfecta para sublimación de alta definición.', 4500.00, 100, 'https://images.unsplash.com/photo-1572119865084-43c285814d63?w=400&h=400&fit=crop', true, true, 2),
('Gorra Trucker Original', 'Gorra estilo trucker con panel frontal sublimable. Ajustable con broche trasero. Máxima comodidad.', 7000.00, 25, 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=400&h=400&fit=crop', true, true, 3),
('Cojín Decorativo Premium', 'Cojín con funda sublimable 40x40cm. Incluye relleno de alta calidad. Perfecto para decorar cualquier espacio.', 12000.00, 20, 'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=400&h=400&fit=crop', true, true, 4),
('Llavero Metálico', 'Llavero de metal cromado con área sublimable. Duradero y elegante. Ideal para regalos personalizados.', 2500.00, 75, 'https://images.unsplash.com/photo-1616486029423-aaa4789e8c9a?w=400&h=400&fit=crop', true, true, 5),
('Mousepad XL Gamer', 'Mousepad con superficie de tela sublimable de alta precisión. Tamaño 30x25cm. Base antideslizante.', 5500.00, 35, 'https://images.unsplash.com/photo-1527814050087-3793815479db?w=400&h=400&fit=crop', true, true, 6),
('Bolso Tote Canvas', 'Bolso tipo tote de canvas resistente con amplia área sublimable. Ecológico y reutilizable.', 8000.00, 15, 'https://images.unsplash.com/photo-1597633425046-08f5110420b5?w=400&h=400&fit=crop', true, true, 6),
('Rompecabezas Fotográfico', 'Rompecabezas sublimable tamaño A4 con 120 piezas. Transforma tus fotos en un regalo único y divertido.', 7500.00, 30, 'https://images.unsplash.com/photo-1606503153255-59d8b8b82176?w=400&h=400&fit=crop', true, true, 6)
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion), imagen_url = VALUES(imagen_url);

-- Usuario Administrador
-- Email: admin@sakari.com | Password: admin123
INSERT INTO usuario (nombre, apellido, email, password, telefono, direccion, rol, activo, fecha_registro) VALUES
('Admin', 'Sakari', 'admin@sakari.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTL5.OABAsYg1V1xyHDNsV4W2lq', '88735743', 'San José, Costa Rica', 'ADMIN', true, NOW())
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- Usuario de Prueba
-- Email: usuario@test.com | Password: user123
INSERT INTO usuario (nombre, apellido, email, password, telefono, direccion, rol, activo, fecha_registro) VALUES
('Usuario', 'Prueba', 'usuario@test.com', '$2a$10$M6HQ8YMxVQNCSZhHGDxVpO9c4NQZ0t8JQs2PqkV8PJZ5LqmKPvOea', '88888888', 'Heredia, Costa Rica', 'USER', true, NOW())
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- Crear carritos para los usuarios
INSERT INTO carrito (usuario_id) 
SELECT id FROM usuario WHERE email = 'admin@sakari.com'
ON DUPLICATE KEY UPDATE fecha_actualizacion = NOW();

INSERT INTO carrito (usuario_id) 
SELECT id FROM usuario WHERE email = 'usuario@test.com'
ON DUPLICATE KEY UPDATE fecha_actualizacion = NOW();

-- =====================================================
-- CREDENCIALES DE ACCESO
-- =====================================================
-- ADMINISTRADOR:
--   Email: admin@sakari.com
--   Password: admin123
--
-- USUARIO DE PRUEBA:
--   Email: usuario@test.com
--   Password: user123
-- =====================================================

-- Verificar datos insertados
SELECT 'Base de datos sakari_db configurada correctamente!' AS mensaje;
SELECT CONCAT('Categorías: ', COUNT(*)) AS total FROM categoria;
SELECT CONCAT('Productos: ', COUNT(*)) AS total FROM producto;
SELECT CONCAT('Usuarios: ', COUNT(*)) AS total FROM usuario;
