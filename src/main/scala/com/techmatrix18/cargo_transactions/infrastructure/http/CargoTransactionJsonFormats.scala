package com.techmatrix18.cargo_transactions.infrastructure.http

import com.techmatrix18.cargo_transactions.domain.CargoTransaction
import com.techmatrix18.cargo_transactions.application.in.*
import play.api.libs.json.{Json, OFormat, Format}
import com.techmatrix18.cargo_transactions.domain.CargoTransactionId
import com.techmatrix18.hub_sections.domain.HubSectionId
import com.techmatrix18.gate_bookings.domain.GateBookingId
import com.techmatrix18.cargo_transactions.domain.OperationType

/**
 * CargoTransactionJsonFormats - Infrastructure driving adapter for Play JSON marshalling.
 * Provides implicit formats for cargo ledger commands and response DTOs using Scala 3 'given' syntax.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

object CargoTransactionJsonFormats {

  // Вариант для непрозрачного типа (opaque type) через приведение типов
  given cargoTransactionIdFormat: Format[CargoTransactionId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(s => s.asInstanceOf[CargoTransactionId]),
      play.api.libs.json.Writes((id: CargoTransactionId) => play.api.libs.json.JsString(id.asInstanceOf[String]))
    )

  given hubSectionIdFormat: Format[HubSectionId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(s => s.asInstanceOf[HubSectionId]),
      play.api.libs.json.Writes((id: HubSectionId) => play.api.libs.json.JsString(id.asInstanceOf[String]))
    )

  given gateBookingIdFormat: Format[GateBookingId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(s => s.asInstanceOf[GateBookingId]),
      play.api.libs.json.Writes((id: GateBookingId) => play.api.libs.json.JsString(id.asInstanceOf[String]))
    )

  given operationTypeFormat: Format[OperationType] =
    Format(
      play.api.libs.json.Reads.StringReads.map { s =>
        OperationType.values.find(_.toString == s).getOrElse(OperationType.values.head)
      },
      play.api.libs.json.Writes(op => play.api.libs.json.JsString(op.toString))
    )

  // 1. Формат для входящей JSON-команды (Фиксация операции перемещения груза)
  given logCargoTransactionCommandFormat: OFormat[LogCargoTransactionCommand] = Json.format[LogCargoTransactionCommand]

  // 2. Формат для исходящего JSON-ответа (Подтверждение записи в Ledger-книгу)
  given logCargoTransactionResponseFormat: OFormat[LogCargoTransactionResponse] = Json.format[LogCargoTransactionResponse]

  // 3. Специфический формат для доменной модели грузов на Scala 3
  given cargoTransactionFormat: OFormat[CargoTransaction] = Json.format[CargoTransaction]
}

