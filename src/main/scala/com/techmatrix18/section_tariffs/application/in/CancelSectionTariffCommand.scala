package com.techmatrix18.section_tariffs.application.in

import com.techmatrix18.section_tariffs.domain.SectionTariffId

/**
 * CancelSectionTariffCommand
 * Аннулирование / Досрочное расторжение контракта
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class CancelSectionTariffCommand(
  tariffId: SectionTariffId,
  reason: String
)

