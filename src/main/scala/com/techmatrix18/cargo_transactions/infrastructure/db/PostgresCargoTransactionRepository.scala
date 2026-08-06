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
 * PostgresCargoTransactionRepository - Реализация Ledger-порта вывода для аудита грузов (Anorm SQL)
 * Строгий Append-Only контракт: методы update и delete отсутствуют сознательно.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class PostgresCargoTransactionRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) {

  override def findById(transactionId: CargoTransactionId): Future[Option[CargoTransaction]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, hub_section_id as hubSectionId, gate_booking_id as gateBookingId,
                 client_name as clientName, operation_type as operationType,
                 pallets_delta as palletsDelta, created_at as createdAt
          FROM cargo_transactions
          WHERE id = ${UUID.fromString(transactionId.value)}::uuid
        """.as(CargoTransactionRow.parser.singleOptional).map(_.toDomain)
    }
  }

  /**
   * Вставка новой неизменяемой транзакции движения палет
   */
  override def create(transaction: CargoTransaction): Future[CargoTransactionId] = Future {
    val row = CargoTransactionRow.fromDomain(transaction)
    db.withConnection { implicit connection =>
      SQL"""
          INSERT INTO cargo_transactions (
            id, hub_section_id, gate_booking_id, client_name, operation_type, pallets_delta, created_at
          ) VALUES (
            ${row.id}::uuid,
            ${row.hubSectionId}::uuid,
            ${row.gateBookingId}::uuid,
            ${row.clientName},
            ${row.operationType},
            ${row.palletsDelta},
            ${row.createdAt}
          )
        """.executeInsert()

      transaction.id
    }
  }

  override def findByFilter(filter: CargoTransactionFilter): Future[List[CargoTransaction]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, hub_section_id as hubSectionId, gate_booking_id as gateBookingId,
                 client_name as clientName, operation_type as operationType,
                 pallets_delta as palletsDelta, created_at as createdAt
          FROM cargo_transactions
          ORDER BY created_at DESC
          LIMIT 100
        """.as(CargoTransactionRow.parser.list).map(_.toDomain)
    }
  }
}

