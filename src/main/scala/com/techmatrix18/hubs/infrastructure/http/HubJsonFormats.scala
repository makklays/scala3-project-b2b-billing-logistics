package com.techmatrix18.hubs.infrastructure.http

import com.techmatrix18.hubs.application.in.*
import play.api.libs.json.{Json, OFormat, Format}
import com.techmatrix18.hubs.domain.HubId

/**
 * HubJsonFormats - Infrastructure driving adapter for Play JSON marshalling.
 * Manages JSON transformation for logistics hub profiles, lifecycles, and GPS positioning.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 06.08.2026
 */

object HubJsonFormats {

  given hubIdFormat: Format[HubId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(s => s.asInstanceOf[HubId]),
      play.api.libs.json.Writes((id: HubId) => play.api.libs.json.JsString(id.asInstanceOf[String]))
    )

  // 1. Форматы для создания и обновления профиля хаба
  given createHubCommandFormat: OFormat[CreateHubCommand] = Json.format[CreateHubCommand]
  given createHubResponseFormat: OFormat[CreateHubResponse] = Json.format[CreateHubResponse]

  given updateHubProfileCommandFormat: OFormat[UpdateHubCommand] = Json.format[UpdateHubCommand]
  given updateHubResponseFormat: OFormat[UpdateHubResponse] = Json.format[UpdateHubResponse]

  // 2. Форматы для IoT/GPS позиционирования и телеметрии
  given updateHubGpsCoordinatesCommandFormat: OFormat[UpdateHubGpsCoordinatesCommand] = Json.format[UpdateHubGpsCoordinatesCommand]
  given updateHubGpsCoordinatesResponseFormat: OFormat[UpdateHubGpsCoordinatesResponse] = Json.format[UpdateHubGpsCoordinatesResponse]

  // 3. Форматы для операционного жизненного цикла (Активация, ремонт, блокировка, удаление)
  given activateHubCommandFormat: OFormat[ActivateHubCommand] = Json.format[ActivateHubCommand]
  given activeHubResponseFormat: OFormat[ActiveHubResponse] = Json.format[ActiveHubResponse]

  given putHubUnderMaintenanceCommandFormat: OFormat[PutHubUnderMaintenanceCommand] = Json.format[PutHubUnderMaintenanceCommand]
  given putHubUnderMaintenanceResponseFormat: OFormat[PutHubUnderMaintenanceResponse] = Json.format[PutHubUnderMaintenanceResponse]

  given suspendHubCommandFormat: OFormat[SuspendHubCommand] = Json.format[SuspendHubCommand]
  given suspendHubResponseFormat: OFormat[SuspendHubResponse] = Json.format[SuspendHubResponse]

  given deleteHubCommandFormat: OFormat[DeleteHubCommand] = Json.format[DeleteHubCommand]
  given deleteHubResponseFormat: OFormat[DeleteHubResponse] = Json.format[DeleteHubResponse]
}

