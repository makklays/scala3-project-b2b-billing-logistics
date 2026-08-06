package com.techmatrix18.section_tariffs.infrastructure.db

import com.techmatrix18.section_tariffs.application.out.{SectionTariffRepository, SectionTariffFilter}
import com.techmatrix18.section_tariffs.domain.{SectionTariff, SectionTariffId}
import com.techmatrix18.hub_sections.domain.{HubSectionId, SectionType}
import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * SectionTariffRow - Вспомогательная структура строки таблицы section_tariffs для Anorm.
 * Полностью изолирует инфраструктуру от доменных моделей.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class SectionTariffRow(
  id: UUID,
  hubSectionId: UUID,
  sectionType: String,
  clientName: String,
  occupiedRatePerHour: BigDecimal,
  emptyReservationRatePerHour: BigDecimal,
  validFrom: Instant,
  validTo: Instant,
  createdAt: Instant,
  updatedAt: Instant
) {
  // Конвертация строки базы данных в чистый доменный агрегат (DDD)
  def toDomain: SectionTariff = SectionTariff(
    id = SectionTariffId(id.toString), // Заворачиваем UUID в наш opaque-тип (String)
    hubSectionId = HubSectionId(hubSectionId.toString), // Заворачиваем UUID секции в opaque-тип (String)
    sectionType = SectionType.values.find(_.code == sectionType).getOrElse(SectionType.PalletZone),
    clientName = clientName,
    occupiedRatePerHour = occupiedRatePerHour,
    emptyReservationRatePerHour = emptyReservationRatePerHour,
    validFrom = validFrom,
    validTo = validTo,
    createdAt = createdAt,
    updatedAt = updatedAt
  )
}

object SectionTariffRow {
  // Anorm-парсер для автоматической сборки структуры SectionTariffRow из SQL-ответа
  val parser: RowParser[SectionTariffRow] = {
    get[UUID]("id") ~
      get[UUID]("hub_section_id") ~
      get[String]("section_type") ~
      get[String]("client_name") ~
      get[BigDecimal]("occupied_rate_per_hour") ~
      get[BigDecimal]("empty_reservation_rate_per_hour") ~
      get[Instant]("valid_from") ~
      get[Instant]("valid_to") ~
      get[Instant]("created_at") ~
      get[Instant]("updated_at") map {
      case id ~ hubSectionId ~ sectionType ~ clientName ~ occupiedRatePerHour ~ emptyReservationRatePerHour ~ validFrom ~ validTo ~ createdAt ~ updatedAt =>
        SectionTariffRow(id, hubSectionId, sectionType, clientName, occupiedRatePerHour, emptyReservationRatePerHour, validFrom, validTo, createdAt, updatedAt)
    }
  }

  // Сборка строки БД из иммутабельного доменного объекта перед записью в Postgres
  def fromDomain(tariff: SectionTariff): SectionTariffRow = SectionTariffRow(
    id = UUID.fromString(tariff.id.value), // Извлекаем String через extension и парсим в UUID
    hubSectionId = UUID.fromString(tariff.hubSectionId.value), // Парсим строковой ID секции в UUID
    sectionType = tariff.sectionType.code,
    clientName = tariff.clientName,
    occupiedRatePerHour = tariff.occupiedRatePerHour,
    emptyReservationRatePerHour = tariff.emptyReservationRatePerHour,
    validFrom = tariff.validFrom,
    validTo = tariff.validTo,
    createdAt = tariff.createdAt,
    updatedAt = tariff.updatedAt
  )
}

