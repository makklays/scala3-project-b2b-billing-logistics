package com.techmatrix18.section_tariffs.application.in

import com.techmatrix18.section_tariffs.domain.SectionTariffId

/**
 * UpdateSectionTariffRatesCommand
 * Индексация стоимости хранения
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class UpdateSectionTariffRatesCommand(
  tariffId: SectionTariffId,
  newOccupiedRate: BigDecimal,
  newEmptyRate: BigDecimal
)

