package com.techmatrix18.hub_sections.infrastructure.http

import com.techmatrix18.hub_sections.application.in.*
import play.api.libs.json.{Json, OFormat}

/**
 * HubSectionJsonFormats - Infrastructure driving adapter for Play JSON marshalling.
 * Provides implicit formats for core warehouse section commands and response DTOs using Scala 3 'given' syntax.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

object HubSectionJsonFormats {

  // 1. Форматы для входящих JSON-команд (Запросы из API на конфигурацию склада)
  given createHubSectionCommandFormat: OFormat[CreateHubSectionCommand] = Json.format[CreateHubSectionCommand]
  given updateHubSectionCapacityCommandFormat: OFormat[UpdateHubSectionCapacityCommand] = Json.format[UpdateHubSectionCapacityCommand]
  given renameHubSectionCommandFormat: OFormat[RenameHubSectionCommand] = Json.format[RenameHubSectionCommand]
  given deleteHubSectionCommandFormat: OFormat[DeleteHubSectionCommand] = Json.format[DeleteHubSectionCommand]

  // 2. Форматы для исходящих DTO-ответов (Передача состояния фронтенду)
  given createHubSectionResponseFormat: OFormat[CreateHubSectionResponse] = Json.format[CreateHubSectionResponse]
  given updateHubSectionCapacityResponseFormat: OFormat[UpdateHubSectionCapacityResponse] = Json.format[UpdateHubSectionCapacityResponse]
  given renameHubSectionResponseFormat: OFormat[RenameHubSectionResponse] = Json.format[RenameHubSectionResponse]
  given deleteHubSectionResponseFormat: OFormat[DeleteHubSectionResponse] = Json.format[DeleteHubSectionResponse]
}

