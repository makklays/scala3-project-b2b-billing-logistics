package com.techmatrix18.billing_transactions.infrastructure.db

import com.techmatrix18.billing_transactions.application.out.{BillingTransactionRepository, BillingTransactionFilter}
import com.techmatrix18.billing_transactions.domain.{BillingTransaction, BillingTransactionId}
import com.techmatrix18.companies.domain.CompanyId
import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * PostgresBillingTransactionRepository - Реализация финансового Ledger-порта вывода (Anorm SQL).
 * Строгий Append-Only контракт: методы изменения (update) и удаления (delete) отсутствуют сознательно.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class PostgresBillingTransactionRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends BillingTransactionRepository {

  override def findById(transactionId: BillingTransactionId): Future[Option[BillingTransaction]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, company_id as companyId, amount, currency, category,
                 source_id as sourceId, description, created_at as createdAt
          FROM billing_transactions
          WHERE id = ${UUID.fromString(transactionId.value)}::uuid
        """.as(BillingTransactionRow.parser.singleOptional).map(_.toDomain)
    }
  }

  // Вставка новой неизменяемой финансовой проводки (Ledger Entry)
  override def create(transaction: BillingTransaction): Future[BillingTransactionId] = Future {
    val row = BillingTransactionRow.fromDomain(transaction)
    db.withConnection { implicit connection =>
      SQL"""
          INSERT INTO billing_transactions (
            id, company_id, amount, currency, category, source_id, description, created_at
          ) VALUES (
            ${row.id}::uuid,
            ${row.companyId}::uuid,
            ${row.amount},
            ${row.currency},
            ${row.category},
            ${row.sourceId}.map(_.toString)::uuid, -- Корректная обработка Option[UUID] для PostgreSQL
            ${row.description},
            ${row.createdAt}
          )
        """.executeInsert()

      transaction.id
    }
  }

  override def findByFilter(filter: BillingTransactionFilter): Future[List[BillingTransaction]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, company_id as companyId, amount, currency, category,
                 source_id as sourceId, description, created_at as createdAt
          FROM billing_transactions
          ORDER BY created_at DESC
          LIMIT 100
        """.as(BillingTransactionRow.parser.list).map(_.toDomain)
    }
  }
}

