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
 * PostgresCargoBalanceRepository - Реализация порта вывода для остатков палет (Anorm SQL)
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class PostgresCargoBalanceRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends CargoBalanceRepository {

  override def findById(balanceId: CargoBalanceId): Future[Option[CargoBalance]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT id, hub_section_id as hubSectionId, client_name as clientName,
               current_pallets as currentPallets, created_at as createdAt, updated_at as updatedAt
        FROM cargo_balances
        WHERE id = ${UUID.fromString(balanceId.value)}::uuid
      """.as(CargoBalanceRow.parser.singleOptional).map(_.toDomain)
    }
  }

  override def create(balance: CargoBalance): Future[CargoBalanceId] = Future {
    val row = CargoBalanceRow.fromDomain(balance)
    db.withConnection { implicit connection =>
      SQL"""
          INSERT INTO cargo_balances (
            id, hub_section_id, client_name, current_pallets, created_at, updated_at
          ) VALUES (
            ${row.id}::uuid,
            ${row.hubSectionId}::uuid,
            ${row.clientName},
            ${row.currentPallets},
            ${row.createdAt},
            ${row.updatedAt}
          )
        """.executeInsert()

      balance.id
    }
  }

  override def update(balance: CargoBalance): Future[Unit] = Future {
    val row = CargoBalanceRow.fromDomain(balance)
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE cargo_balances
          SET current_pallets = ${row.currentPallets},
              updated_at = ${row.updatedAt}
          WHERE id = ${row.id}::uuid
        """.executeUpdate()
      () // Возвращаем Unit (void)
    }
  }

  override def delete(balanceId: CargoBalanceId): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          DELETE FROM cargo_balances
          WHERE id = ${UUID.fromString(balanceId.value)}::uuid
        """.executeUpdate()
      ()
    }
  }

  override def findByFilter(filter: CargoBalanceFilter): Future[List[CargoBalance]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, hub_section_id as hubSectionId, client_name as clientName,
                 current_pallets as currentPallets, created_at as createdAt, updated_at as updatedAt
          FROM cargo_balances
          LIMIT 100
        """.as(CargoBalanceRow.parser.*).map(_.toDomain)
    }
  }
}

