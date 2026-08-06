package com.techmatrix18.hub_sections.domain

import com.techmatrix18.hubs.domain.HubId
import java.time.Instant

/**
 * HubSection Aggregate Root - внутренние зоны распределительного центра
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.3
 * @since 06.08.2026
 */

case class HubSection(
  id: HubSectionId,
  hubId: HubId,
  sectionName: String,
  sectionType: SectionType,

  totalCapacity: BigDecimal,

  // System audit (managed by the system, not by the user)
  createdAt: Instant,
  updatedAt: Instant
)

