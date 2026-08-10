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
 * PostgresHubSectionRepository - Реализация порта вывода для секций хаба (Anorm SQL)
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class PostgresHubSectionRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends HubSectionRepository {

  override def findById(sectionId: HubSectionId): Future[Option[HubSection]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT id, hub_id as hubId, section_name as sectionName, section_type as sectionType,
               total_capacity as totalCapacity, created_at as createdAt, updated_at as updatedAt
        FROM hub_sections
        WHERE id = ${UUID.fromString(sectionId.value)}::uuid
      """.as(HubSectionRow.parser.singleOpt).map(_.toDomain)
    }
  }

  override def create(section: HubSection): Future[HubSectionId] = Future {
    val row = HubSectionRow.fromDomain(section)
    db.withConnection { implicit connection =>
      SQL"""
          INSERT INTO hub_sections (
            id, hub_id, section_name, section_type, total_capacity, created_at, updated_at
          ) VALUES (
            ${row.id}::uuid,
            ${row.hubId}::uuid,
            ${row.sectionName},
            ${row.sectionType},
            ${row.totalCapacity},
            ${row.createdAt},
            ${row.updatedAt}
          )
        """.executeInsert()

      section.id
    }
  }

  override def update(section: HubSection): Future[Unit] = Future {
    val row = HubSectionRow.fromDomain(section)
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE hub_sections
          SET section_name = ${row.sectionName},
              total_capacity = ${row.totalCapacity},
              updated_at = ${row.updatedAt}
          WHERE id = ${row.id}::uuid
        """.executeUpdate()
      () // Возвращаем Unit
    }
  }

  override def delete(sectionId: HubSectionId): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          DELETE FROM hub_sections
          WHERE id = ${UUID.fromString(sectionId.value)}::uuid
        """.executeUpdate()
      ()
    }
  }

  override def findByFilter(filter: HubSectionFilter): Future[List[HubSection]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, hub_id as hubId, section_name as sectionName, section_type as sectionType,
                 total_capacity as totalCapacity, created_at as createdAt, updated_at as updatedAt
          FROM hub_sections
          LIMIT 100
        """.as(HubSectionRow.parser.*).map(_.toDomain)
    }
  }
}

