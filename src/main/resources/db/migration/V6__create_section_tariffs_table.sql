-- V6__create_section_tariffs_table.sql

-- Создаем таблицу тарифной сетки для секций склада
CREATE TABLE section_tariffs (
    id UUID NOT NULL,
    hub_section_id UUID NULL DEFAULT NULL, -- NULLABLE: точечный тариф или глобальный для типа секции

    section_type VARCHAR(25) NOT NULL,    -- Например, 'DRY', 'CHILLED', 'FREEZER'
    client_name VARCHAR(255) NULL DEFAULT NULL, -- NULLABLE: персональный контракт или публичный тариф

    -- Финансовые рейты с высокой точностью (NUMERIC в Postgres — аналог DECIMAL)
    occupied_rate_per_hour NUMERIC(10, 4) NOT NULL DEFAULT 0.0000,
    empty_reservation_rate_per_hour NUMERIC(10, 4) NOT NULL DEFAULT 0.0000,

    -- Временной интервал действия тарифа (Бизнес-логика)
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_to TIMESTAMP WITH TIME ZONE NULL DEFAULT NULL,

    -- Системные поля аудита (управляются Scala-бэкендом)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_section_tariffs PRIMARY KEY (id),
    CONSTRAINT fk_section_tariffs_section FOREIGN KEY (hub_section_id) REFERENCES hub_sections(id) ON DELETE CASCADE
);

-- Оптимизация индексов под требования вакансии (PostgreSQL Query Profiling):
-- Составной индекс для фонового Pekko Billing Engine для мгновенного поиска актуальной цены
-- по иерархии (конкретная секция -> клиент -> временное окно) без Full Table Scan
CREATE INDEX idx_tariffs_billing_lookup
ON section_tariffs (hub_section_id, client_name, valid_from, valid_to);

