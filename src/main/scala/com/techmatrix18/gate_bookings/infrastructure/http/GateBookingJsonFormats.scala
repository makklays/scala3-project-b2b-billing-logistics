package com.techmatrix18.gate_bookings.infrastructure.http

import com.techmatrix18.gate_bookings.application.in.*
import play.api.libs.json.{Json, OFormat, Format}
import com.techmatrix18.gates.domain.GateId
import com.techmatrix18.gate_bookings.domain.GateBookingId

/**
 * Объект, содержащий форматы JSON для сериализации и десериализации команд и событий,
 * связанных с бронированием ворот.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

object GateBookingJsonFormats {

  given gateIdFormat: Format[GateId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(s => s.asInstanceOf[GateId]),
      play.api.libs.json.Writes((id: GateId) => play.api.libs.json.JsString(id.asInstanceOf[String]))
    )

  given gateBookingIdFormat: Format[GateBookingId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(s => s.asInstanceOf[GateBookingId]),
      play.api.libs.json.Writes((id: GateBookingId) => play.api.libs.json.JsString(id.asInstanceOf[String]))
    )

  // 1. Форматы для входящих JSON-команд (Запросы клиентов на резервирование и отмену)
  given createGateBookingCommandFormat: OFormat[CreateGateBookingCommand] = Json.format[CreateGateBookingCommand]
  given cancelGateBookingCommandFormat: OFormat[CancelGateBookingCommand] = Json.format[CancelGateBookingCommand]
  given arriveTruckCommandFormat: OFormat[ArriveTruckCommand] = Json.format[ArriveTruckCommand]
  given departTruckCommandFormat: OFormat[DepartTruckCommand] = Json.format[DepartTruckCommand]
  given rescheduleGateBookingCommandFormat: OFormat[RescheduleGateBookingCommand] = Json.format[RescheduleGateBookingCommand]
  given markAsNoShowCommandFormat: OFormat[MarkAsNoShowCommand] = Json.format[MarkAsNoShowCommand]

  // 2. Форматы для исходящих JSON-событий (События, которые будут отправляться клиентам)
  given createGateBookingResponseFormat: OFormat[CreateGateBookingResponse] = Json.format[CreateGateBookingResponse]
  given cancelGateBookingResponseFormat: OFormat[CancelGateBookingResponse] = Json.format[CancelGateBookingResponse]
  given arriveTruckResponseFormat: OFormat[ArriveTruckResponse] = Json.format[ArriveTruckResponse]
  given departTruckResponseFormat: OFormat[DepartTruckResponse] = Json.format[DepartTruckResponse]
  given rescheduleGateBookingResponseFormat: OFormat[RescheduleGateBookingResponse] = Json.format[RescheduleGateBookingResponse]
  given markAsNoShowResponseFormat: OFormat[MarkAsNoShowResponse] = Json.format[MarkAsNoShowResponse]
}

