-- V9__create_billing_transactions_table.sql

-- Создаем таблицу финансового лога (Ledger) для учета всех начислений и списаний
CREATE TABLE billing_transactions (
    id UUID NOT NULL,
    company_id UUID NOT NULL, -- Внешний ключ на таблицу компаний companies.id

    -- Финансовые параметры (NUMERIC в Postgres — аналог DECIMAL)
    amount NUMERIC(15, 4) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'EUR',

    -- Категория списания (например: 'GATE_RENTAL', 'GATE_OVERTIME', 'PALLET_STORAGE', 'RESERVATION_QUOTA', 'API_METERING')
    category VARCHAR(25) NOT NULL,

    -- Полиморфный ID источника списания для сквозного аудита
    source_id UUID NULL DEFAULT NULL,

    -- Понятный человеку текст для инвойса/квитанции
    description VARCHAR(255) NOT NULL,

    -- Только дата создания, так как финансовые транзакции иммутабельны
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_billing_transactions PRIMARY KEY (id),
    CONSTRAINT fk_billing_tx_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- Оптимизация индексов под требования вакансии (PostgreSQL Query Profiling):
-- 1. Для личного кабинета и бухгалтерии клиента: выгрузка истории трат за определенный период времени
CREATE INDEX idx_billing_tx_company_time ON billing_transactions (company_id, created_at);

-- 2. Для аналитических отчетов платформы: подсчет прибыли по конкретным категориям услуг
CREATE INDEX idx_billing_tx_category ON billing_transactions (category, amount);

