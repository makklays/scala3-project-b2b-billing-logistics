package com.techmatrix18.cargo_transactions.infrastructure.db

import com.techmatrix18.cargo_transactions.application.out.{CargoTransactionRepository, CargoTransactionFilter}
import com.techmatrix18.cargo_transactions.domain.{CargoTransaction, CargoTransactionId, OperationType}
import com.techmatrix18.hub_sections.domain.HubSectionId
import com.techmatrix18.gate_bookings.domain.GateBookingId
import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * CargoTransactionRow - Вспомогательный DTO строки таблицы cargo_transactions.
 * Полностью изолирует инфраструктурный слой от доменных моделей Ledger.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class CargoTransactionRow(
  id: UUID,
  hubSectionId: UUID,
  gateBookingId: UUID,
  clientName: String,
  operationType: String,
  palletsDelta: Int,
  createdAt: Instant
) {
  // Конвертация строки базы данных в чистый доменный агрегат (DDD)
  def toDomain: CargoTransaction = CargoTransaction(
    id = CargoTransactionId(id.toString), // Заворачиваем UUID в наш opaque-тип (String)
    hubSectionId = HubSectionId(hubSectionId.toString),
    gateBookingId = GateBookingId(gateBookingId.toString),
    clientName = clientName,
    operationType = OperationType.values.find(_.code == operationType).getOrElse(OperationType.Supply),
    palletsDelta = palletsDelta,
    createdAt = createdAt
  )
}

object CargoTransactionRow {
  // Anorm-парсер для автоматической сборки структуры CargoTransactionRow из SQL-ответа
  val parser: RowParser[CargoTransactionRow] = {
    get[UUID]("id") ~
      get[UUID]("hub_section_id") ~
      get[UUID]("gate_booking_id") ~
      get[String]("client_name") ~
      get[String]("operation_type") ~
      get[Int]("pallets_delta") ~
      get[Instant]("created_at") map {
      case id ~ hubSectionId ~ gateBookingId ~ clientName ~ operationType ~ palletsDelta ~ createdAt =>
        CargoTransactionRow(id, hubSectionId, gateBookingId, clientName, operationType, palletsDelta, createdAt)
    }
  }

  // Сборка строки БД из иммутабельного доменного объекта перед записью в Postgres
  def fromDomain(transaction: CargoTransaction): CargoTransactionRow = CargoTransactionRow(
    id = UUID.fromString(transaction.id.value), // Извлекаем String через extension и парсим в UUID
    hubSectionId = UUID.fromString(transaction.hubSectionId.value),
    gateBookingId = UUID.fromString(transaction.gateBookingId.value),
    clientName = transaction.clientName,
    operationType = transaction.operationType.code,
    palletsDelta = transaction.palletsDelta,
    createdAt = transaction.createdAt
  )
}

