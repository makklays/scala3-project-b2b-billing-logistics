package com.techmatrix18.section_tariffs.domain

import com.techmatrix18.hub_sections.domain.{HubSectionId, SectionType}
import java.time.Instant

/**
 * SectionTariff
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

case class SectionTariff(
  id: SectionTariffId,
  hubSectionId: HubSectionId,
  sectionType: SectionType,
  clientName: String,
  occupiedRatePerHour: BigDecimal,
  emptyReservationRatePerHour: BigDecimal,
  validFrom: Instant,
  validTo: Instant,

  // System audit (managed by the system, not by the user)
  createdAt: Instant,
  updatedAt: Instant
)

