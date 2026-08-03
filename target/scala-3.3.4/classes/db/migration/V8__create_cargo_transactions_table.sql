-- V8__create_cargo_transactions_table.sql

-- Создаем таблицу транзакционного лога перемещения грузов (история приходов и расходов)
CREATE TABLE cargo_transactions (
    id UUID NOT NULL,
    hub_section_id UUID NOT NULL, -- Внешний ключ на таблицу секций хаба hub_sections.id
    gate_booking_id UUID NULL DEFAULT NULL, -- NULLABLE: связь с фурой/тайм-слотом ворот

    client_name VARCHAR(255) NOT NULL,

    -- Тип операции (например: 'INCOMING' — привоз, 'OUTGOING' — вывоз)
    operation_type VARCHAR(25) NOT NULL,

    -- Дельта изменения палет (всегда положительное число, например 21)
    pallets_delta INT NOT NULL,

    -- Точная системная дата и время фиксации лога операции с поддержкой таймзон
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_cargo_transactions PRIMARY KEY (id),
    CONSTRAINT fk_cargo_tx_section FOREIGN KEY (hub_section_id) REFERENCES hub_sections(id),
    CONSTRAINT fk_cargo_tx_booking FOREIGN KEY (gate_booking_id) REFERENCES gate_bookings(id) ON DELETE SET NULL
);

-- 🔍 Оптимизация индексов под требования вакансии (PostgreSQL Query Profiling):
-- 1. Для генерации отчетов клиентам по истории их поставок за определенный период времени (time-range)
CREATE INDEX idx_cargo_tx_client_time ON cargo_transactions (client_name, created_at);

-- 2. Составной индекс для аналитики загруженности конкретных секций хаба по операциям
CREATE INDEX idx_cargo_tx_section_op ON cargo_transactions (hub_section_id, operation_type);

