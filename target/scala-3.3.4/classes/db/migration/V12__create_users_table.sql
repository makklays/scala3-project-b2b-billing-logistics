-- V12__create_users_table.sql

-- Creating table 'users' for storing main user data
CREATE TABLE IF NOT EXISTS users
(
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(255) UNIQUE NOT NULL,
    email       VARCHAR(200) UNIQUE NOT NULL,
    roles       VARCHAR(200) DEFAULT 'USER',
    mobile      VARCHAR(20),

    gender      VARCHAR(50),
    age         INTEGER,
    avatar      VARCHAR(255),

    password    VARCHAR(255) NOT NULL,

    created_at  TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT now(),

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
);

-- Adding index for fast searching by email
CREATE INDEX idx_users_email ON users(email);
-- Adding index for fast searching by mobile
CREATE INDEX idx_users_mobile ON users(mobile);

