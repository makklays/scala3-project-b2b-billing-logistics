-- V4__create_gate_bookings_table.sql

-- Создаем таблицу бронирования тайм-слотов и ворот для грузовиков
CREATE TABLE gate_bookings (
    id UUID NOT NULL,
    gate_id UUID NOT NULL, -- Внешний ключ на таблицу ворот gates.id

    client_name VARCHAR(255) NOT NULL,
    truck_license_plate VARCHAR(20) NOT NULL,

    -- Планируемое временное окно (Тайм-слот) с поддержкой таймзон
    scheduled_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end_time TIMESTAMP WITH TIME ZONE NOT NULL,

    -- Фактические логические метки времени (заполняются по сигналам IoT/API)
    actual_arrival_time TIMESTAMP WITH TIME ZONE NULL DEFAULT NULL,
    actual_departure_time TIMESTAMP WITH TIME ZONE NULL DEFAULT NULL,

    -- Жизненный цикл бронирования (например: 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED', 'NO_SHOW')
    status VARCHAR(25) NOT NULL DEFAULT 'SCHEDULED',

    -- Системные поля аудита (управляются Scala-бэкендом)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_gate_bookings PRIMARY KEY (id),
    CONSTRAINT fk_gate_bookings_gate FOREIGN KEY (gate_id) REFERENCES gates(id) ON DELETE CASCADE
);

-- Оптимизация индексов под требования вакансии (PostgreSQL Query Profiling):
-- 1. Индекс для IoT-камер на въезде: мгновенный поиск активной брони по номеру грузовика
CREATE INDEX idx_bookings_truck_status ON gate_bookings (truck_license_plate, status);

-- 2. Индекс для биллинг-шедулера Pekko: ежечасный поиск закрытых или просроченных броней для расчета штрафов
CREATE INDEX idx_bookings_billing_lookup ON gate_bookings (status, scheduled_end_time, actual_departure_time);

