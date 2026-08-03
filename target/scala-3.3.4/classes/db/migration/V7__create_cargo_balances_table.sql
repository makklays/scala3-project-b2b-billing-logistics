-- V7__create_cargo_balances_table.sql

-- Создаем таблицу текущих остатков палет груза по клиентам в секциях хаба
CREATE TABLE cargo_balances (
    id UUID NOT NULL,
    hub_section_id UUID NOT NULL, -- Внешний ключ на таблицу секций хаба hub_sections.id

    client_name VARCHAR(255) NOT NULL,

    -- Текущее количество палет на складе (валидация >= 0 проверяется Scala-бэкендом)
    current_pallets INT NOT NULL DEFAULT 0,

    -- Системные поля аудита (управляются Scala-бэкендом)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_cargo_balances PRIMARY KEY (id),
    CONSTRAINT fk_cargo_balances_section FOREIGN KEY (hub_section_id) REFERENCES hub_sections(id) ON DELETE CASCADE,

    -- 🧠 Бизнес-инвариант: у одного клиента в конкретной секции хаба может быть только одна строка баланса (для UPSERT)
    CONSTRAINT uq_section_client UNIQUE (hub_section_id, client_name)
);

-- 🔍 Оптимизация индексов под требования вакансии (PostgreSQL Query Profiling):
-- Индекс для ежечасного биллинг-шедулера Pekko, который ищет всех клиентов с ненулевым балансом палет
CREATE INDEX idx_balances_billing ON cargo_balances (current_pallets, hub_section_id);

