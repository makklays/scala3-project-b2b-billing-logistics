package com.techmatrix18.gate_bookings.infrastructure.db

import com.techmatrix18.gates.domain.GateId
import com.techmatrix18.gates.domain.GateId.*
import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import com.techmatrix18.gate_bookings.domain.GateBookingId.*
import anorm.{Macro, RowParser, ~}
import java.time.Instant
import java.util.UUID

/**
 * GateBookingRow
 *
 * Вспомогательный класс строки базы данных (Data Transfer Object для СУБД)
 * Полностью изолирует инфраструктуру от доменного слоя.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.3
 * @since 06.08.2026
 */

case class GateBookingRow(
  id: UUID,
  gateId: UUID,
  clientName: String,
  truckLicensePlate: String,
  scheduledStartTime: Instant,
  scheduledEndTime: Instant,
  actualArrivalTime: Option[Instant],
  actualDepartureTime: Option[Instant],
  status: String,
  createdAt: Instant,
  updatedAt: Instant
) {

  // Трансформация строки БД в чистый доменный агрегат (DDD)
  def toDomain: GateBooking = GateBooking(
    id = GateBookingId(id.toString), // Заворачиваем UUID в наш opaque-тип (String)
    gateId = GateId(gateId.toString),
    clientName = clientName,
    truckLicensePlate = truckLicensePlate,
    scheduledStartTime = scheduledStartTime,
    scheduledEndTime = scheduledEndTime,
    actualArrivalTime = actualArrivalTime,
    actualDepartureTime = actualDepartureTime,
    status = GateBookingStatus.values.find(_.code == status).getOrElse(GateBookingStatus.Scheduled),
    createdAt = createdAt,
    updatedAt = updatedAt
  )
}

object GateBookingRow {
  // Anorm парсер для автоматической сборки кейс-класса строки
  val parser: RowParser[GateBookingRow] = {
    get[UUID]("id") ~
    get[UUID]("gate_id") ~
    get[String]("client_name") ~
    get[String]("truck_license_plate") ~
    get[Instant]("scheduled_start_time") ~
    get[Instant]("scheduled_end_time") ~
    get[Option[Instant]]("actual_arrival_time") ~
    get[Option[Instant]]("actual_departure_time") ~
    get[String]("status") ~
    get[Instant]("created_at") ~
    get[Instant]("updated_at") map {
      case id ~ gateId ~ clientName ~ truckLicensePlate ~ scheduledStartTime ~ scheduledEndTime ~ actualArrivalTime ~ actualDepartureTime ~ status ~ createdAt ~ updatedAt =>
        GateBookingRow(id, gateId, clientName, truckLicensePlate, scheduledStartTime, scheduledEndTime, actualArrivalTime, actualDepartureTime, status, createdAt, updatedAt)
    }
  }

  // Сборка строки БД из доменного объекта перед сохранением
  def fromDomain(booking: GateBooking): GateBookingRow = GateBookingRow(
    id = UUID.fromString(booking.id.value),
    //gateId = booking.gateId.raw,
    //gateId = booking.gateId.value,
    gateId = UUID.fromString(booking.gateId.value),
    clientName = booking.clientName,
    truckLicensePlate = booking.truckLicensePlate,
    scheduledStartTime = booking.scheduledStartTime,
    scheduledEndTime = booking.scheduledEndTime,
    actualArrivalTime = booking.actualArrivalTime,
    actualDepartureTime = booking.actualDepartureTime,
    status = booking.status.code,
    createdAt = booking.createdAt,
    updatedAt = booking.updatedAt
  )
}

