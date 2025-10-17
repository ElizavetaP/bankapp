-- Create users table in auth schema
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birthdate DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT check_login_length CHECK (LENGTH(login) >= 3),
    CONSTRAINT check_password_length CHECK (LENGTH(password) >= 6)
);

CREATE INDEX idx_users_login ON users(login);
CREATE INDEX idx_users_enabled ON users(enabled);

