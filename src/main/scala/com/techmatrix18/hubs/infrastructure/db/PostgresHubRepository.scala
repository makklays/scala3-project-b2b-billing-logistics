package com.techmatrix18.hubs.infrastructure.db

import com.techmatrix18.hubs.application.out.HubRepository
import com.techmatrix18.hubs.domain.{Hub, HubId, HubType, HubStatus}
import com.techmatrix18.companies.domain.CompanyId
import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * PostgresHubRepository - Реализация порта вывода в стиле нативного SQL Anorm
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class PostgresHubRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends HubRepository {

  override def findById(id: HubId): Future[Option[Hub]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, company_id as companyId, title, description, address_line as addressLine,
                 postal_code as postalCode, latitude, longitude, hub_type as hubType, status,
                 created_at as createdAt, updated_at as updatedAt
          FROM hubs
          WHERE id = ${id.raw}::uuid
        """.as(HubRow.parser.singleOpt).map(_.toDomain)
    }
  }

  override def create(hub: Hub): Future[HubId] = Future {
    val row = HubRow.fromDomain(hub)
    db.withConnection { implicit connection =>
      SQL"""
          INSERT INTO hubs (
            id, company_id, title, description, address_line,
            postal_code, latitude, longitude, hub_type, status, created_at, updated_at
          ) VALUES (
            ${row.id}::uuid,
            ${row.companyId}::uuid,
            ${row.title},
            ${row.description},
            ${row.addressLine},
            ${row.postalCode},
            ${row.latitude},
            ${row.longitude},
            ${row.hubType},
            ${row.status},
            ${row.createdAt},
            ${row.updatedAt}
          )
        """.executeInsert()

      hub.id
    }
  }

  override def update(hub: Hub): Future[Unit] = Future {
    val row = HubRow.fromDomain(hub)
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE hubs
          SET title = ${row.title},
              description = ${row.description},
              address_line = ${row.addressLine},
              postal_code = ${row.postalCode},
              latitude = ${row.latitude},
              longitude = ${row.longitude},
              status = ${row.status},
              updated_at = ${row.updatedAt}
          WHERE id = ${row.id}::uuid
        """.executeUpdate()
      () // Возвращаем Unit (void)
    }
  }

  override def delete(id: HubId): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          DELETE FROM hubs
          WHERE id = ${id.raw}::uuid
        """.executeUpdate()
      ()
    }
  }

  override def findAll(): Future[List[Hub]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, company_id as companyId, title, description, address_line as addressLine,
                 postal_code as postalCode, latitude, longitude, hub_type as hubType, status,
                 created_at as createdAt, updated_at as updatedAt
          FROM hubs
          LIMIT 100
        """.as(HubRow.parser.list).map(_.toDomain)
    }
  }
}

