package com.techmatrix18.section_tariffs.application.in

import com.techmatrix18.hub_sections.domain.HubSectionId
import java.time.Instant

/**
 * CreateSectionTariffCommand
 * Назначение тарифа
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class CreateSectionTariffCommand(
  hubSectionId: HubSectionId,
  sectionType: String,
  clientName: String,
  occupiedRatePerHour: BigDecimal,
  emptyReservationRatePerHour: BigDecimal,
  validFrom: Instant,
  validTo: Instant
)

