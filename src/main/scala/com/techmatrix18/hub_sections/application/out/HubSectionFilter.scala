package com.techmatrix18.hub_sections.application.out

import com.techmatrix18.hub_sections.domain.{HubSection, HubSectionId, SectionType}
import com.techmatrix18.hubs.domain.HubId
import scala.concurrent.Future

/**
 * Filter criteria object for flexible hub section queries.
 * Избавляет репозиторий от комбинаторного взрыва методов.
 *
 *
 */

case class HubSectionFilter(
  hubId: Option[HubId] = None,
  sectionType: Option[SectionType] = None,
  sectionName: Option[String] = None
)

