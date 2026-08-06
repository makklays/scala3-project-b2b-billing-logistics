package com.techmatrix18.cargo_balances.infrastructure.db

import com.techmatrix18.cargo_balances.application.out.{CargoBalanceRepository, CargoBalanceFilter}
import com.techmatrix18.cargo_balances.domain.{CargoBalance, CargoBalanceId}
import com.techmatrix18.hub_sections.domain.HubSectionId
import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * CargoBalanceRow - Структура строки таблицы cargo_balances для Anorm.
 * Полностью изолирует инфраструктурный слой от доменных моделей.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class CargoBalanceRow(
  id: UUID,
  hubSectionId: UUID,
  clientName: String,
  currentPallets: Int,
  createdAt: Instant,
  updatedAt: Instant
) {
  // Конвертация строки базы данных в чистый доменный агрегат (DDD)
  def toDomain: CargoBalance = CargoBalance(
    id = CargoBalanceId(id.toString), // Заворачиваем UUID в кастомный opaque-тип String
    hubSectionId = HubSectionId(hubSectionId.toString), // Заворачиваем UUID секции в opaque-тип String
    clientName = clientName,
    currentPallets = currentPallets,
    createdAt = createdAt,
    updatedAt = updatedAt
  )
}

object CargoBalanceRow {
  // Anorm-парсер для автоматической сборки структуры CargoBalanceRow из SQL-ответа
  val parser: RowParser[CargoBalanceRow] = {
    get[UUID]("id") ~
      get[UUID]("hub_section_id") ~
      get[String]("client_name") ~
      get[Int]("current_pallets") ~
      get[Instant]("created_at") ~
      get[Instant]("updated_at") map {
      case id ~ hubSectionId ~ clientName ~ currentPallets ~ createdAt ~ updatedAt =>
        CargoBalanceRow(id, hubSectionId, clientName, currentPallets, createdAt, updatedAt)
    }
  }

  // Сборка строки БД из иммутабельного доменного объекта перед записью в Postgres
  def fromDomain(balance: CargoBalance): CargoBalanceRow = CargoBalanceRow(
    id = UUID.fromString(balance.id.value), // Извлекаем String через extension и парсим в UUID
    hubSectionId = UUID.fromString(balance.hubSectionId.value), // Парсим строковой ID секции в UUID
    clientName = balance.clientName,
    currentPallets = balance.currentPallets,
    createdAt = balance.createdAt,
    updatedAt = balance.updatedAt
  )
}

