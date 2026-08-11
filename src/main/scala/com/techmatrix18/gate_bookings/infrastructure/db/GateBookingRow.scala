package com.techmatrix18.gate_bookings.infrastructure.db

import com.techmatrix18.gates.domain.GateId
import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import java.time.Instant
import java.util.UUID
import anorm.*
import anorm.SqlParser.* // КРИТИЧЕСКИ ВАЖНО: подключает методы get и комбинаторы

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
    id = GateBookingId(id.toString), // Заворачиваем UUID в наш доменный тип
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

  // ИСПРАВЛЕНО: Явный префикс SqlParser.get гарантирует стабильную сборку в Scala 3
  val parser: RowParser[GateBookingRow] = {
    SqlParser.get[UUID]("id") ~
      SqlParser.get[UUID]("gate_id") ~
      SqlParser.get[String]("client_name") ~
      SqlParser.get[String]("truck_license_plate") ~
      SqlParser.get[Instant]("scheduled_start_time") ~
      SqlParser.get[Instant]("scheduled_end_time") ~
      SqlParser.get[Option[Instant]]("actual_arrival_time") ~
      SqlParser.get[Option[Instant]]("actual_departure_time") ~
      SqlParser.get[String]("status") ~
      SqlParser.get[Instant]("created_at") ~
      SqlParser.get[Instant]("updated_at") map {
      case id ~ gateId ~ clientName ~ truckLicensePlate ~ scheduledStartTime ~ scheduledEndTime ~ actualArrivalTime ~ actualDepartureTime ~ status ~ createdAt ~ updatedAt =>
        GateBookingRow(id, gateId, clientName, truckLicensePlate, scheduledStartTime, scheduledEndTime, actualArrivalTime, actualDepartureTime, status, createdAt, updatedAt)
    }
  }

  // Сборка строки БД из доменного объекта перед сохранением
  def fromDomain(booking: GateBooking): GateBookingRow = {
    // Безопасно извлекаем строку из ID: если .value не сработает, Scala 3 подставит .toString
    val bookingIdStr = booking.id.toString
    val gateIdStr = booking.gateId.toString

    GateBookingRow(
      id = UUID.fromString(bookingIdStr),
      gateId = UUID.fromString(gateIdStr),
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
}

