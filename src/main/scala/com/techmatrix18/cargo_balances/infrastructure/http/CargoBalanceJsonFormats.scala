package com.techmatrix18.cargo_balances.infrastructure.http

import com.techmatrix18.cargo_balances.application.in.*
import play.api.libs.json.{Json, OFormat}
import play.api.libs.json.{Json, OFormat, Format}
import com.techmatrix18.cargo_balances.domain.CargoBalanceId

/**
 * CargoBalanceJsonFormats - Infrastructure driving adapter for Play JSON marshalling.
 * Provides implicit formats for core cargo balance commands and response DTOs using Scala 3 'given' syntax.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

object CargoBalanceJsonFormats {

  // Вариант для непрозрачного типа (opaque type) в Scala 3
  given cargoBalanceIdFormat: Format[CargoBalanceId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(s => s.asInstanceOf[CargoBalanceId]),
      play.api.libs.json.Writes((id: CargoBalanceId) => play.api.libs.json.JsString(id.asInstanceOf[String]))
    )

  // 1. Форматы для входящих JSON-команд (Запросы из API на изменение остатков)
  given initializeCargoBalanceCommandFormat: OFormat[InitializeCargoBalanceCommand] = Json.format[InitializeCargoBalanceCommand]
  given addCargoPayloadCommandFormat: OFormat[AddCargoPayloadCommand] = Json.format[AddCargoPayloadCommand]
  given removeCargoPayloadCommandFormat: OFormat[RemoveCargoPayloadCommand] = Json.format[RemoveCargoPayloadCommand]

  // 2. Форматы для исходящих DTO-ответов (Передача состояния товарных остатков)
  given initializeCargoBalanceResponseFormat: OFormat[InitializeCargoBalanceResponse] = Json.format[InitializeCargoBalanceResponse]
  given addCargoPayloadResponseFormat: OFormat[AddCargoPayloadResponse] = Json.format[AddCargoPayloadResponse]
  given removeCargoPayloadResponseFormat: OFormat[RemoveCargoPayloadResponse] = Json.format[RemoveCargoPayloadResponse]
}

