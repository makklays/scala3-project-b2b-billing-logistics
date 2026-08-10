package com.techmatrix18.hubs.infrastructure.db

import com.techmatrix18.hubs.application.out.HubRepository
import com.techmatrix18.hubs.domain.{Hub, HubId, HubStatus, HubType}
import com.techmatrix18.companies.domain.CompanyId

import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.{~, *}
import anorm.SqlParser.*

import scala.concurrent.{ExecutionContext, Future}

/**
 * HubRow - Вспомогательная структура данных строки таблицы hubs.
 * Полностью изолирует инфраструктурный слой от доменных моделей.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

private case class HubRow(
  id: UUID,
  companyId: UUID,
  title: String,
  description: Option[String],
  countryCode: String, // ДОБАВЛЕНО
  city: String, // ДОБАВЛЕНО
  addressLine: String,
  postalCode: String,
  latitude: BigDecimal,
  longitude: BigDecimal,
  hubType: String,
  status: String,
  createdAt: Instant,
  updatedAt: Instant
) {
  // Конвертация строки базы данных в чистый доменный агрегат (DDD)
  def toDomain: Hub = Hub(
    id = HubId(id.toString),
    companyId = CompanyId(companyId.toString),
    title = title,
    description = description,
    addressLine = addressLine,
    countryCode = countryCode, // ДОБАВЛЕНО
    city = city, // ДОБАВЛЕНО
    postalCode = postalCode,
    latitude = latitude,
    longitude = longitude,
    hubType = HubType.values.find(_.code == hubType).getOrElse(HubType.LandTerminal),
    status = HubStatus.values.find(_.code == status).getOrElse(HubStatus.Active),
    createdAt = createdAt,
    updatedAt = updatedAt
  )
}

private object HubRow {
  // Anorm-парсер для автоматической сборки структуры HubRow из SQL-ответа
  val parser: RowParser[HubRow] = {
    get[UUID]("id") ~
    get[UUID]("company_id") ~
    get[String]("title") ~
    get[Option[String]]("description") ~
    get[String]("country_code") ~ // ДОБАВЛЕНО
    get[String]("city") ~ // ДОБАВЛЕНО
    get[String]("address_line") ~
    get[String]("postal_code") ~
    get[BigDecimal]("latitude") ~
    get[BigDecimal]("longitude") ~
    get[String]("hub_type") ~
    get[String]("status") ~
    get[Instant]("created_at") ~
    get[Instant]("updated_at") map {
      case id ~ companyId ~ title ~ description ~ country_code ~ city ~ addressLine ~ postalCode ~ latitude ~ longitude ~ hubType ~ status ~ createdAt ~ updatedAt =>
        HubRow(id, companyId, title, description, country_code, city, addressLine, postalCode, latitude, longitude, hubType, status, createdAt, updatedAt)
    }
  }

  // Сборка строки БД из иммутабельного доменного объекта перед записью в Postgres
  def fromDomain(hub: Hub): HubRow = HubRow(
    id = UUID.fromString(hub.id),
    //companyId = UUID.fromString(hub.companyId),
    companyId = UUID.fromString(hub.companyId.value),
    title = hub.title,
    description = hub.description,
    countryCode = hub.countryCode, // ДОБАВЛЕНО
    city = hub.city, // ДОБАВЛЕНО
    addressLine = hub.addressLine,
    postalCode = hub.postalCode,
    latitude = hub.latitude,
    longitude = hub.longitude,
    hubType = hub.hubType.code,
    status = hub.status.code,
    createdAt = hub.createdAt,
    updatedAt = hub.updatedAt
  )
}

