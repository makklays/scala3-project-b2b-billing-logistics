-- V2__create_hubs_table.sql

-- Создаем таблицу логистических хабов (склад)
CREATE TABLE hubs (
    id UUID NOT NULL,
    company_id UUID NOT NULL, -- Внешний ключ на таблицу companies

    title VARCHAR(255) NOT NULL,
    description TEXT,
    hub_type VARCHAR(25) NOT NULL, -- Например: 'LAND_TERMINAL', 'SEA_PORT'
    status VARCHAR(25) NOT NULL DEFAULT 'ACTIVE', -- Например: 'ACTIVE', 'MAINTENANCE'

    -- Географические данные
    country_code CHAR(2) NOT NULL, -- Например, 'ES' для Испании
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    address_line VARCHAR(255) NOT NULL,

    -- Точные GPS-координаты (NUMERIC в Postgres — аналог DECIMAL)
    latitude NUMERIC(10, 8) NOT NULL,
    longitude NUMERIC(11, 8) NOT NULL,

    -- Системные поля аудита с поддержкой таймзон
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_hubs PRIMARY KEY (id),
    CONSTRAINT fk_hubs_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- Индексы для оптимизации под требования вакансии (PostgreSQL Query Profiling):
-- Индекс по компании для быстрой выборки всех складов, принадлежащих конкретному B2B-клиенту
CREATE INDEX idx_hubs_company_id ON hubs (company_id);

