-- Sección de administración (ejecutar una vez en un entorno de desarrollo)
drop database if exists proyect;
drop user if exists usuario_proyect;

-- Creación del esquema
CREATE database proyect
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- Creación de usuarios con contraseñas seguras (idealmente asignadas fuera del script)
create user 'usuario_proyect'@'%' identified by 'Usuar1o_Claveproyect.';


-- Asignación de permisos
-- Se otorgan permisos específicos en lugar de todos los permisos a todas las tablas futuras
GRANT SELECT, INSERT, UPDATE, DELETE, ALTER, CREATE, DROP on proyect.* to 'usuario_proyect'@'%';
flush privileges;

use proyect;

-- --- Sección de Creación de Tablas ---

DROP TABLE IF EXISTS items_pedido;
DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS items_carrito;
DROP TABLE IF EXISTS carritos;
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS categorias;
DROP TABLE IF EXISTS usuarios;

SET FOREIGN_KEY_CHECKS = 1;

-- ===================================
-- CREAR TABLAS
-- ===================================

-- Tabla Usuarios
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    rol ENUM('USER', 'ADMIN') DEFAULT 'USER',
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- Tabla Categorías
CREATE TABLE categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla Productos
CREATE TABLE productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    personalizable BOOLEAN DEFAULT FALSE,
    categoria_id BIGINT,
    imagen_url VARCHAR(500),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla Carritos
CREATE TABLE carritos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla Items del Carrito
CREATE TABLE items_carrito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carrito_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    personalizacion TEXT,
    FOREIGN KEY (carrito_id) REFERENCES carritos(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla Pedidos
CREATE TABLE pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    estado ENUM('PENDIENTE', 'PROCESANDO', 'ENVIADO', 'ENTREGADO', 'CANCELADO') DEFAULT 'PENDIENTE',
    direccion_envio VARCHAR(500) NOT NULL,
    telefono_contacto VARCHAR(20) NOT NULL,
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla Items del Pedido
CREATE TABLE items_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    nombre_producto VARCHAR(200) NOT NULL,
    personalizacion TEXT,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================
-- INSERTAR DATOS DE EJEMPLO
-- ===================================

-- Insertar Categorías
INSERT INTO categorias (nombre, descripcion, activo) VALUES
('Camisas', 'Camisas sublimadas con diseños personalizados en poliéster de alta calidad', true),
('Tazas', 'Tazas sublimables con tus diseños favoritos, resistentes al lavado', true),
('Gorras', 'Gorras personalizadas con sublimación de alta definición', true),
('Uniformes', 'Uniformes deportivos completos con sublimación profesional', true),
('Suéter', 'Suéteres cómodos con diseños sublimados personalizados', true);

-- Insertar Productos
INSERT INTO productos (nombre, descripcion, precio, stock, activo, personalizable, categoria_id, imagen_url, fecha_creacion) VALUES
-- Camisas Sublimadas
('Camisa Poliéster Blanca', 'Camisa 100% poliéster ideal para sublimación de diseños personalizados', 8500.00, 50, true, true, 1, '/images/products/camisa-blanca.jpg', NOW()),
('Camisa Deportiva Negra', 'Camisa deportiva con tecnología de sublimación, tela transpirable', 9500.00, 40, true, true, 1, '/images/products/camisa-negra.jpg', NOW()),
('Camisa Premium Azul', 'Camisa premium con acabado sublimado profesional', 12000.00, 30, true, true, 1, '/images/products/camisa-azul.jpg', NOW()),
('Camisa Casual Gris', 'Camisa casual perfecta para personalización con tu logo o diseño', 8000.00, 45, true, true, 1, '/images/products/camisa-gris.jpg', NOW()),


-- Tazas Sublimadas
('Taza Blanca 11oz', 'Taza de cerámica blanca de 11oz, ideal para sublimación', 4500.00, 100, true, true, 2, '/images/products/taza-blanca.jpg', NOW()),
('Taza Mágica Negra', 'Taza mágica que cambia de color, efecto especial al agregar líquido caliente', 6500.00, 60, true, true, 2, '/images/products/taza-magica.jpg', NOW()),
('Taza Star Wars', 'Taza con diseños de Star Wars sublimados en alta calidad', 5500.00, 80, true, false, 2, '/images/products/taza-starwars.jpg', NOW()),
('Taza Personalizada 15oz', 'Taza grande de 15oz con tu diseño personalizado', 5000.00, 70, true, true, 2, '/images/products/taza-15oz.jpg', NOW()),

-- Gorras Sublimadas
('Gorra Trucker Blanca', 'Gorra tipo trucker con panel frontal sublimable', 6500.00, 60, true, true, 3, '/images/products/gorra-trucker.jpg', NOW()),
('Gorra Plana Negra', 'Gorra plana con panel completo para sublimación', 7000.00, 50, true, true, 3, '/images/products/gorra-plana.jpg', NOW()),
('Gorra Deportiva', 'Gorra deportiva con tecnología de sublimación total', 7500.00, 45, true, true, 3, '/images/products/gorra-deportiva.jpg', NOW()),

-- Uniformes Deportivos Sublimados
('Uniforme Milan Emirates', 'Uniforme completo estilo AC Milan con sublimación profesional (camisa + short + medias)', 25000.00, 20, true, true, 4, '/images/products/uniforme-milan.jpg', NOW()),
('Uniforme Deportivo Personalizado', 'Set completo de uniforme con tu diseño sublimado (camisa + short)', 22000.00, 25, true, true, 4, '/images/products/uniforme-personalizado.jpg', NOW()),
('Jersey Deportivo', 'Jersey deportivo con sublimación de alta calidad', 12000.00, 35, true, true, 4, '/images/products/jersey-deportivo.jpg', NOW()),

-- Suéteres Sublimados
('Suéter con Capucha Negro', 'Suéter con capucha en poliéster, ideal para sublimación', 15000.00, 30, true, true, 5, '/images/products/sueter-capucha.jpg', NOW()),
('Suéter Deportivo Gris', 'Suéter deportivo cómodo con panel frontal sublimable', 14000.00, 35, true, true, 5, '/images/products/sueter-gris.jpg', NOW()),
('Suéter Personalizado', 'Suéter de alta calidad con tu diseño sublimado', 16000.00, 28, true, true, 5, '/images/products/sueter-personalizado.jpg', NOW());

-- Insertar Usuarios (password: admin123 encriptado con BCrypt)

DELETE FROM usuarios
WHERE email IN ('admin@sakari.com', 'cliente@ejemplo.com');
INSERT INTO usuarios (nombre, apellido, email, password, telefono, direccion, rol, activo, fecha_registro) VALUES
('Administrador', 'Sakari', 'admin@sakari.com', '$2a$10$rN8qGUpNqLFNvJlh6QqJKO7VxXXqNQp7a.x6zp1LwKdYQqOqWqNJa', '8873-5743', 'La Aurora de Heredia, Costa Rica', 'ADMIN', true, NOW()),
('Cliente', 'Demo', 'cliente@ejemplo.com', '$2a$10$rN8qGUpNqLFNvJlh6QqJKO7VxXXqNQp7a.x6zp1LwKdYQqOqWqNJa', '8888-9999', 'Heredia, Costa Rica', 'USER', true, NOW());


-- Insertar Carritos para los usuarios
INSERT INTO carritos (usuario_id, fecha_actualizacion) VALUES
(1, NOW()),
(2, NOW());


-- ===================================
-- ACTUALIZAR URLs DE IMÁGENES
-- ===================================

-- Desactivar modo seguro temporalmente
SET SQL_SAFE_UPDATES = 0;

UPDATE productos
SET imagen_url = '/img/CamisaPoliesterBlanca.jpg'
WHERE nombre = 'Camisa Poliéster Blanca';

UPDATE productos
SET imagen_url = '/img/CamisaDeportivaNegra.jpg'
WHERE nombre = 'Camisa Deportiva Negra';

UPDATE productos
SET imagen_url = '/img/CamisaPremiumAzul.jpg'
WHERE nombre = 'Camisa Premium Azul';

UPDATE productos
SET imagen_url = '/img/CamisaCasualGris.jpg'
WHERE nombre = 'Camisa Casual Gris';

UPDATE productos 
SET imagen_url = '/img/TazaBlanca.jpg'
WHERE nombre = 'Taza Blanca 11oz';

UPDATE productos 
SET imagen_url = '/img/TazaMagicaNegra.jpg'
WHERE nombre = 'Taza Mágica Negra';

UPDATE productos 
SET imagen_url = '/img/TazaStarWars.jpg'
WHERE nombre = 'Taza Star Wars';

UPDATE productos 
SET imagen_url = '/img/TazaPersonalizada.jpg'
WHERE nombre = 'Taza Personalizada 15oz';

UPDATE productos
SET imagen_url = '/img/GorraTruckerBlanca.jpg'
WHERE nombre = 'Gorra Trucker Blanca';

UPDATE productos
SET imagen_url = '/img/GorraPlanaNegra.jpg'
WHERE nombre = 'Gorra Plana Negra';

UPDATE productos
SET imagen_url = '/img/GorraDeportiva.jpg'
WHERE nombre = 'Gorra Deportiva';

UPDATE productos
SET imagen_url = '/img/UniformeMilanEmirates.jpg'
WHERE nombre = 'Uniforme Milan Emirates';

UPDATE productos
SET imagen_url = '/img/UniformeDeportivoPersonalizado.jpg'
WHERE nombre = 'Uniforme Deportivo Personalizado';

UPDATE productos
SET imagen_url = '/img/JerseyDeportivo.jpg'
WHERE nombre = 'Jersey Deportivo';

UPDATE productos
SET imagen_url = '/img/SuéterconCapucha Negro.jpg'
WHERE nombre = 'Suéter con Capucha Negro';

UPDATE productos
SET imagen_url = '/img/SuéterDeportivoGris.jpg'
WHERE nombre = 'Suéter Deportivo Gris';

UPDATE productos
SET imagen_url = '/img/SuéterPersonalizado.jpg'
WHERE nombre = 'Suéter Personalizado';

-- Reactivar modo seguro
SET SQL_SAFE_UPDATES = 1;

-- ===================================
-- VERIFICAR TABLAS Y ESTRUCTURA
-- ===================================

SHOW TABLES;
DESCRIBE productos;





