CREATE TABLE IF NOT EXISTS users
(
    id         BINARY(16)   NOT NULL PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NULL,
    last_name  VARCHAR(100) NULL,
    role       VARCHAR(30)  NOT NULL DEFAULT 'USER',
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by BINARY(16)   NOT NULL,
    updated_at TIMESTAMP(3) NULL ON UPDATE CURRENT_TIMESTAMP(3),
    updated_by BINARY(16)   NULL,
    deleted_at TIMESTAMP(3) NULL,
    deleted_by BINARY(16)   NULL
);

CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id         BINARY(16)   NOT NULL PRIMARY KEY,
    user_id    BINARY(16)   NOT NULL,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP(3) NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by BINARY(16)   NOT NULL,
    updated_at TIMESTAMP(3) NULL ON UPDATE CURRENT_TIMESTAMP(3),
    updated_by BINARY(16)   NULL,
    deleted_at TIMESTAMP(3) NULL,
    deleted_by BINARY(16)   NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_deleted_at ON users (deleted_at);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_deleted_at ON refresh_tokens (deleted_at);

-- Seed initial admin and demo user where username is the email
-- Default password: password (BCrypt: $2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.)
INSERT INTO users (id, username, password, first_name, last_name, role, is_active, created_by)
VALUES (UUID_TO_BIN('a1000000-0000-0000-0000-000000000001'),
        'admin@tekna.com',
        '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.',
        'Administrador',
        'Sistema',
        'ADMIN',
        TRUE,
        UUID_TO_BIN('00000000-0000-0000-0000-000000000000')),
       (UUID_TO_BIN('a1000000-0000-0000-0000-000000000002'),
        'gamer@tekna.com',
        '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.',
        'Gamer',
        'Pro',
        'USER',
        TRUE,
        UUID_TO_BIN('00000000-0000-0000-0000-000000000000'))
ON DUPLICATE KEY UPDATE username = VALUES(username);
