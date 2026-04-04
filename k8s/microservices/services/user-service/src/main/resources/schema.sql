-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    
    -- Campos de estado
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    
    -- Campos de auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Crear índice para provider y provider_id
CREATE INDEX IF NOT EXISTS idx_provider ON users(provider, provider_id);

-- Tabla de metadatos para login
CREATE TABLE IF NOT EXISTS metadata_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20),
    provider_id VARCHAR(255),
    picture_url VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE, 
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    device_info VARCHAR(255),    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Crear índice para user_id
CREATE INDEX IF NOT EXISTS idx_user_id ON metadata_login(user_id);

-- Crear restricción única para user_id y provider
ALTER TABLE metadata_login ADD CONSTRAINT IF NOT EXISTS unique_user_provider UNIQUE (user_id, provider);

-- Tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Tabla de relación entre usuarios y roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Tabla unificada para tokens y metadatos de autenticación
CREATE TABLE IF NOT EXISTS user_auth_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    token_type VARCHAR(50) DEFAULT 'Bearer',
    status ENUM('ACTIVE', 'REVOKED', 'EXPIRED', 'BLACKLISTED') NOT NULL DEFAULT 'ACTIVE',
    
    -- Campos para autenticación local y social
    provider VARCHAR(20),
    provider_user_id VARCHAR(255),
    provider_access_token TEXT,
    provider_refresh_token TEXT,
    scopes VARCHAR(255),
    metadata JSON,
    
    -- Información de seguridad y auditoría
    device_info VARCHAR(255),
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    expires_at TIMESTAMP,
    refresh_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Crear índices para la tabla user_auth_tokens
CREATE INDEX IF NOT EXISTS idx_user_status ON user_auth_tokens(user_id, status);
CREATE INDEX IF NOT EXISTS idx_refresh_token ON user_auth_tokens(refresh_token);
CREATE INDEX IF NOT EXISTS idx_provider_auth ON user_auth_tokens(provider, provider_user_id);

-- Insertar roles específicos del marketplace
MERGE INTO roles (name, description) KEY(name) VALUES 
('ROLE_ADMIN', 'Administrador con acceso completo al sistema');

MERGE INTO roles (name, description) KEY(name) VALUES 
('ROLE_BUYER', 'Usuario cliente con permisos de comprador');

MERGE INTO roles (name, description) KEY(name) VALUES 
('ROLE_SELLER', 'Usuario cliente con permisos de vendedor');

MERGE INTO roles (name, description) KEY(name) VALUES 
('ROLE_SUPPORT', 'Personal de soporte técnico y atención al cliente');

-- Insertar usuarios de ejemplo (password: 'password' encriptado con BCrypt)
MERGE INTO users (name, first_name, last_name, email, password, phone, created_at) KEY(email) VALUES
('admin', 'Admin', 'User', 'admin@example.com', '$2a$10$ixlPY3AAd4ty1l6E2IsQ9OFZi2ba9ZQE0bP7RFcGIWNhyFrrT3YUi', '+1234567890', CURRENT_TIMESTAMP());

MERGE INTO users (name, first_name, last_name, email, password, phone, created_at) KEY(email) VALUES
('buyer', 'Buyer', 'User', 'buyer@example.com', '$2a$10$ixlPY3AAd4ty1l6E2IsQ9OFZi2ba9ZQE0bP7RFcGIWNhyFrrT3YUi', '+1234567891', CURRENT_TIMESTAMP());

MERGE INTO users (name, first_name, last_name, email, password, phone, created_at) KEY(email) VALUES
('seller', 'Seller', 'User', 'seller@example.com', '$2a$10$ixlPY3AAd4ty1l6E2IsQ9OFZi2ba9ZQE0bP7RFcGIWNhyFrrT3YUi', '+1234567892', CURRENT_TIMESTAMP());

MERGE INTO users (name, first_name, last_name, email, password, phone, created_at) KEY(email) VALUES
('support', 'Support', 'User', 'support@example.com', '$2a$10$ixlPY3AAd4ty1l6E2IsQ9OFZi2ba9ZQE0bP7RFcGIWNhyFrrT3YUi', '+1234567893', CURRENT_TIMESTAMP());

-- Asignar roles a los usuarios
-- Admin tiene todos los roles
MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@example.com' AND r.name = 'ROLE_ADMIN';

MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@example.com' AND r.name = 'ROLE_BUYER';

MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@example.com' AND r.name = 'ROLE_SELLER';

MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@example.com' AND r.name = 'ROLE_SUPPORT';

-- Buyer solo tiene rol de comprador
MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'buyer@example.com' AND r.name = 'ROLE_BUYER';

-- Seller tiene roles de vendedor y comprador
MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'seller@example.com' AND r.name = 'ROLE_SELLER';

MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'seller@example.com' AND r.name = 'ROLE_BUYER';

-- Support tiene rol de soporte y comprador
MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'support@example.com' AND r.name = 'ROLE_SUPPORT';

MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'support@example.com' AND r.name = 'ROLE_BUYER';
