-- V3__create_gates_table.sql

-- Создаем таблицу погрузочных ворот / рамп склада
CREATE TABLE gates (
    id UUID NOT NULL,
    hub_id UUID NOT NULL, -- Внешний ключ на таблицу складов/хабов hubs.id

    gate_number VARCHAR(50) NOT NULL, -- Например, «Gate 01», «Gate A-12»
    gate_type VARCHAR(25) NOT NULL,   -- Например, 'DRY', 'CHILLED', 'FREEZER'
    status VARCHAR(25) NOT NULL DEFAULT 'AVAILABLE', -- Например, 'AVAILABLE', 'OCCUPIED'

    -- Расписание работы ворот (учитывается биллинг-шедулером)
    working_hours VARCHAR(100) NOT NULL DEFAULT '24/7',

    -- Финансовые параметры тарификации ворот (NUMERIC в Postgres — аналог DECIMAL)
    hourly_rate NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    overtime_hourly_rate NUMERIC(10, 2) NOT NULL DEFAULT 0.00,

    -- Системные поля аудита с поддержкой таймзон (управляются Scala-бэкендом)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_gates PRIMARY KEY (id),
    CONSTRAINT fk_gates_hub FOREIGN KEY (hub_id) REFERENCES hubs(id) ON DELETE CASCADE,

    -- внутри одного хаба не может быть двух ворот с одинаковым номером
    CONSTRAINT uq_hub_gate_number UNIQUE (hub_id, gate_number)
);

-- Оптимизация индексов под требования вакансии (PostgreSQL Query Profiling):
-- Индекс по hub_id необходим для моментальной выборки и кэширования в Redis всей карты ворот склада
CREATE INDEX idx_gates_hub_id ON gates (hub_id);

