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
 * PostgresSectionTariffRepository - Реализация финансового порта вывода для тарифов (Anorm SQL)
 * Физическое удаление (delete) отсутствует сознательно по правилам финтех-аудита.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class PostgresSectionTariffRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends SectionTariffRepository {

  override def findById(tariffId: SectionTariffId): Future[Option[SectionTariff]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, hub_section_id as hubSectionId, section_type as sectionType, client_name as clientName,
                 occupied_rate_per_hour as occupiedRatePerHour, empty_reservation_rate_per_hour as emptyReservationRatePerHour,
                 valid_from as validFrom, valid_to as validTo, created_at as createdAt, updated_at as updatedAt
          FROM section_tariffs
          WHERE id = ${UUID.fromString(tariffId.value)}::uuid
        """.as(SectionTariffRow.parser.*).map(_.toDomain)
    }
  }

  override def create(tariff: SectionTariff): Future[SectionTariffId] = Future {
    val row = SectionTariffRow.fromDomain(tariff)
    db.withConnection { implicit connection =>
      SQL"""
          INSERT INTO section_tariffs (
            id, hub_section_id, section_type, client_name,
            occupied_rate_per_hour, empty_reservation_rate_per_hour,
            valid_from, valid_to, created_at, updated_at
          ) VALUES (
            ${row.id}::uuid,
            ${row.hubSectionId}::uuid,
            ${row.sectionType},
            ${row.clientName},
            ${row.occupiedRatePerHour},
            ${row.emptyReservationRatePerHour},
            ${row.validFrom},
            ${row.validTo},
            ${row.createdAt},
            ${row.updatedAt}
          )
        """.executeInsert()

      tariff.id
    }
  }

  override def update(tariff: SectionTariff): Future[Unit] = Future {
    val row = SectionTariffRow.fromDomain(tariff)
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE section_tariffs
          SET occupied_rate_per_hour = ${row.occupiedRatePerHour},
              empty_reservation_rate_per_hour = ${row.emptyReservationRatePerHour},
              valid_to = ${row.validTo},
              updated_at = ${row.updatedAt}
          WHERE id = ${row.id}::uuid
        """.executeUpdate()
      () // Возвращаем Unit (void)
    }
  }

  override def findByFilter(filter: SectionTariffFilter): Future[List[SectionTariff]] = Future {
    db.withConnection { implicit connection =>
      // Базовая выборка тарифов с сортировкой по актуальности дат
      SQL"""
          SELECT id, hub_section_id as hubSectionId, section_type as sectionType, client_name as clientName,
                 occupied_rate_per_hour as occupiedRatePerHour, empty_reservation_rate_per_hour as emptyReservationRatePerHour,
                 valid_from as validFrom, valid_to as validTo, created_at as createdAt, updated_at as updatedAt
          FROM section_tariffs
          ORDER BY valid_from DESC
          LIMIT 100
        """.as(SectionTariffRow.parser.*).map(_.toDomain)
    }
  }
}

