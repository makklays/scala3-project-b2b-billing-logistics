package com.techmatrix18.section_tariffs.domain

/**
 * SectionType - определяет температурный режим секции для расчета стоимости хранения.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 06.08.2026
 */

enum SectionType(val code: String) { // Использован val code под типы данных VARCHAR в PostgreSQL
  case Dry     extends SectionType("DRY")     // Обычный сухой складской температурный режим
  case Chilled extends SectionType("CHILLED") // Охлаждаемый склад (+2..+4 °C)
  case Freezer extends SectionType("FREEZER") // Зона глубокой заморозки (-18..-24 °C)
}

