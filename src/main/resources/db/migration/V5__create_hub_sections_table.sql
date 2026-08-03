-- V5__create_hub_sections_table.sql

-- Создаем таблицу секций хаба (зон хранения палет)
CREATE TABLE hub_sections (
    id UUID NOT NULL,
    hub_id UUID NOT NULL, -- Внешний ключ на таблицу складов/хабов hubs.id

    section_name VARCHAR(100) NOT NULL, -- Например, «Sector A - Cold Room», «Zone B - High Racks»
    section_type VARCHAR(25) NOT NULL,   -- Например, 'DRY', 'CHILLED', 'FREEZER', 'HAZARDOUS'

    -- Общая вместимость секции в палетах
    total_capacity INT NOT NULL,

    -- Системные поля аудита (управляются Scala-бэкендом)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT pk_hub_sections PRIMARY KEY (id),
    CONSTRAINT fk_hub_sections_hub FOREIGN KEY (hub_id) REFERENCES hubs(id) ON DELETE CASCADE,

    -- внутри одного хаба не может быть двух секций с одинаковым именем
    CONSTRAINT uq_hub_section_name UNIQUE (hub_id, section_name)
);

-- Оптимизация индексов под требования вакансии (PostgreSQL Query Profiling):
-- Индекс по hub_id необходим для моментальной выборки и отображения структуры склада на дашборде
CREATE INDEX idx_hub_sections_hub_id ON hub_sections (hub_id);

