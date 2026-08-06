package com.techmatrix18.hub_sections.application.in

import com.techmatrix18.hub_sections.domain.HubSectionId

/**
 * RenameHubSectionCommand
 * Переименование
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.3
 * @since 06.08.2026
 */

case class RenameHubSectionCommand(
  sectionId: HubSectionId,
  newSectionName: String
)

