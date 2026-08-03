-- V1__create_companies_table.sql

-- Создаем таблицу компаний
CREATE TABLE companies (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,

    -- Налоговый код в Испании (CIF/NIF), уникальный на уровне системы
    tax_number VARCHAR(50) NOT NULL,

    -- Финансовый баланс с высокой точностью (NUMERIC — аналог DECIMAL в Postgres)
    balance NUMERIC(15, 4) NOT NULL DEFAULT 0.0000,

    -- Статус компании: 'ACTIVE', 'SUSPENDED' (управляется бэкендом)
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Системные поля аудита с поддержкой таймзон
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_companies PRIMARY KEY (id),
    CONSTRAINT uq_companies_tax UNIQUE (tax_number)
);

-- 🔍 Оптимизация индексов (PostgreSQL Query Profiling):
-- Составной индекс для Pekko-акторов для мгновенного поиска активных компаний при расчете биллинга
CREATE INDEX idx_companies_status_balance ON companies (status, balance);

