-- ========================================
-- 1. Create schemas for each microservice
-- ========================================
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS accounts;
CREATE SCHEMA IF NOT EXISTS cash;
CREATE SCHEMA IF NOT EXISTS transfer;
CREATE SCHEMA IF NOT EXISTS exchange;
CREATE SCHEMA IF NOT EXISTS blocker;
CREATE SCHEMA IF NOT EXISTS notifications;

-- ========================================
-- 2. Grant privileges to bankapp user
-- ========================================
GRANT ALL PRIVILEGES ON SCHEMA auth TO bankapp;
GRANT ALL PRIVILEGES ON SCHEMA accounts TO bankapp;
GRANT ALL PRIVILEGES ON SCHEMA cash TO bankapp;
GRANT ALL PRIVILEGES ON SCHEMA transfer TO bankapp;
GRANT ALL PRIVILEGES ON SCHEMA exchange TO bankapp;
GRANT ALL PRIVILEGES ON SCHEMA blocker TO bankapp;
GRANT ALL PRIVILEGES ON SCHEMA notifications TO bankapp;

ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT ALL ON TABLES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA accounts GRANT ALL ON TABLES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA cash GRANT ALL ON TABLES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA transfer GRANT ALL ON TABLES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA exchange GRANT ALL ON TABLES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA blocker GRANT ALL ON TABLES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA notifications GRANT ALL ON TABLES TO bankapp;

ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT ALL ON SEQUENCES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA accounts GRANT ALL ON SEQUENCES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA cash GRANT ALL ON SEQUENCES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA transfer GRANT ALL ON SEQUENCES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA exchange GRANT ALL ON SEQUENCES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA blocker GRANT ALL ON SEQUENCES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA notifications GRANT ALL ON SEQUENCES TO bankapp;

-- ========================================
-- 3. AUTH SCHEMA - Users table (with passwords)
-- ========================================
SET search_path TO auth;

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

CREATE INDEX IF NOT EXISTS idx_auth_users_login ON users(login);
CREATE INDEX IF NOT EXISTS idx_auth_users_enabled ON users(enabled);

-- ========================================
-- 4. ACCOUNTS SCHEMA - Users and Accounts tables
-- ========================================
SET search_path TO accounts;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT check_age_18 CHECK (EXTRACT(YEAR FROM AGE(CURRENT_DATE, birth_date)) >= 18)
);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_currency UNIQUE (user_id, currency),
    CONSTRAINT check_positive_balance CHECK (balance >= 0)
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_users_login ON users(login);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_currency ON accounts(currency);
CREATE INDEX IF NOT EXISTS idx_outbox_processed ON outbox_events(processed);
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox_events(created_at);

-- ========================================
-- 5. CASH SCHEMA - Cash operations table
-- ========================================
SET search_path TO cash;

CREATE TABLE IF NOT EXISTS cash_operations (
    id BIGSERIAL PRIMARY KEY,
    user_login VARCHAR(50) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    operation_type VARCHAR(10) NOT NULL,
    saga_id VARCHAR(36) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT check_positive_amount CHECK (amount > 0),
    CONSTRAINT check_operation_type CHECK (operation_type IN ('DEPOSIT', 'WITHDRAW')),
    CONSTRAINT check_saga_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'COMPENSATED'))
);

CREATE INDEX IF NOT EXISTS idx_cash_operations_user_login ON cash_operations(user_login);
CREATE INDEX IF NOT EXISTS idx_cash_operations_created_at ON cash_operations(created_at);
CREATE INDEX IF NOT EXISTS idx_cash_operations_operation_type ON cash_operations(operation_type);
CREATE INDEX IF NOT EXISTS idx_cash_operations_saga_id ON cash_operations(saga_id);
CREATE INDEX IF NOT EXISTS idx_cash_operations_status ON cash_operations(status);

CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_cash_outbox_processed ON outbox_events(processed);
CREATE INDEX IF NOT EXISTS idx_cash_outbox_created_at ON outbox_events(created_at);

-- ========================================
-- 6. TRANSFER SCHEMA - Transfers table
-- ========================================
SET search_path TO transfer;

CREATE TABLE IF NOT EXISTS transfers (
    id BIGSERIAL PRIMARY KEY,
    from_login VARCHAR(50) NOT NULL,
    to_login VARCHAR(50) NOT NULL,
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    from_amount DECIMAL(19, 2) NOT NULL,
    to_amount DECIMAL(19, 2) NOT NULL,
    exchange_rate DECIMAL(19, 8) NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT check_positive_from_amount CHECK (from_amount > 0),
    CONSTRAINT check_positive_to_amount CHECK (to_amount > 0),
    CONSTRAINT check_positive_exchange_rate CHECK (exchange_rate > 0)
);

CREATE INDEX IF NOT EXISTS idx_transfers_from_login ON transfers(from_login);
CREATE INDEX IF NOT EXISTS idx_transfers_to_login ON transfers(to_login);
CREATE INDEX IF NOT EXISTS idx_transfers_created_at ON transfers(created_at);
CREATE INDEX IF NOT EXISTS idx_transfers_from_currency ON transfers(from_currency);
CREATE INDEX IF NOT EXISTS idx_transfers_to_currency ON transfers(to_currency);

-- ========================================
-- 7. EXCHANGE SCHEMA - Exchange rates table
-- ========================================
SET search_path TO exchange;

CREATE TABLE IF NOT EXISTS exchange_rates (
    id BIGSERIAL PRIMARY KEY,
    currency_code VARCHAR(3) UNIQUE NOT NULL,
    buy_rate DECIMAL(19, 4) NOT NULL,
    sell_rate DECIMAL(19, 4) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT check_positive_rates CHECK (buy_rate > 0 AND sell_rate > 0),
    CONSTRAINT check_rub_rate CHECK (
        currency_code != 'RUB' OR (buy_rate = 1 AND sell_rate = 1)
    )
);

-- Insert initial exchange rates
INSERT INTO exchange_rates (currency_code, buy_rate, sell_rate) VALUES
('RUB', 1.00, 1.00),
('USD', 82.50, 78.00),
('EUR', 96.40, 90.70)
ON CONFLICT (currency_code) DO NOTHING;

-- ========================================
-- 8. NOTIFICATIONS SCHEMA - Notifications table
-- ========================================
SET search_path TO notifications;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_login VARCHAR(50) NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT check_notification_type CHECK (notification_type IN ('TRANSFER', 'CASH', 'ACCOUNT', 'SYSTEM'))
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_login ON notifications(user_login);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(notification_type);

-- ========================================
-- 9. BLOCKER SCHEMA - Blocked operations table
-- ========================================
SET search_path TO blocker;

CREATE TABLE IF NOT EXISTS blocked_operations (
    id BIGSERIAL PRIMARY KEY,
    user_login VARCHAR(50) NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    reason TEXT NOT NULL,
    blocked_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT check_operation_type CHECK (operation_type IN ('TRANSFER', 'CASH_WITHDRAW', 'CASH_DEPOSIT', 'ACCOUNT'))
);

-- Индекс для быстрой проверки блокировки
CREATE UNIQUE INDEX IF NOT EXISTS idx_blocked_operations_unique 
ON blocked_operations(user_login, operation_type);

-- ========================================
-- 10. OUTBOX SCHEMA - Transactional Outbox Pattern для уведомлений
-- ========================================
CREATE SCHEMA IF NOT EXISTS outbox;
SET search_path TO outbox;

CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,        -- 'CASH_DEPOSIT', 'CASH_WITHDRAW', 'TRANSFER', etc.
    payload TEXT NOT NULL,                  -- JSON строка с данными события
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Индекс для сортировки по времени (FIFO)
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox_events(created_at);

-- ========================================
-- Reset search path
-- ========================================
SET search_path TO public;
