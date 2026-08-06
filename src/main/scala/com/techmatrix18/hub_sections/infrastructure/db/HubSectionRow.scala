package com.techmatrix18.hub_sections.infrastructure.db

import com.techmatrix18.hub_sections.application.out.{HubSectionRepository, HubSectionFilter}
import com.techmatrix18.hub_sections.domain.{HubSection, HubSectionId, SectionType}
import com.techmatrix18.hubs.domain.HubId
import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * HubSectionRow - Структура строки таблицы hub_sections.
 * Полностью изолирует инфраструктурный слой от доменных моделей агрегата.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class HubSectionRow(
  id: UUID,
  hubId: UUID,
  sectionName: String,
  sectionType: String,
  totalCapacity: BigDecimal,
  createdAt: Instant,
  updatedAt: Instant
) {
  // Конвертация строки базы данных в чистый доменный агрегат (DDD)
  def toDomain: HubSection = HubSection(
    id = HubSectionId(id.toString), // Заворачиваем UUID в наш opaque-тип (String)
    hubId = HubId(hubId),
    sectionName = sectionName,
    sectionType = SectionType.values.find(_.code == sectionType).getOrElse(SectionType.PalletZone),
    totalCapacity = totalCapacity,
    createdAt = createdAt,
    updatedAt = updatedAt
  )
}

object HubSectionRow {
  // Anorm-парсер для автоматической сборки структуры HubSectionRow из SQL-ответа
  val parser: RowParser[HubSectionRow] = {
    get[UUID]("id") ~
      get[UUID]("hub_id") ~
      get[String]("section_name") ~
      get[String]("section_type") ~
      get[BigDecimal]("total_capacity") ~
      get[Instant]("created_at") ~
      get[Instant]("updated_at") map {
      case id ~ hubId ~ sectionName ~ sectionType ~ totalCapacity ~ createdAt ~ updatedAt =>
        HubSectionRow(id, hubId, sectionName, sectionType, totalCapacity, createdAt, updatedAt)
    }
  }

  // Сборка строки БД из иммутабельного доменного объекта перед записью в Postgres
  def fromDomain(section: HubSection): HubSectionRow = HubSectionRow(
    id = UUID.fromString(section.id.value), // Извлекаем String через extension и парсим в UUID
    hubId = section.hubId.raw,             // Берем чистый UUID хаба
    sectionName = section.sectionName,
    sectionType = section.sectionType.code,
    totalCapacity = section.totalCapacity,
    createdAt = section.createdAt,
    updatedAt = section.updatedAt
  )
}