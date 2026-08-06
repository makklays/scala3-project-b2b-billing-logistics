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
 * BillingTransactionRow - Структура строки таблицы billing_transactions для Anorm.
 * Изолирует прикладной финтех-слой от низкоуровневых типов реляционной СУБД.
 *
 *
 */

case class BillingTransactionRow(
  id: UUID,
  companyId: UUID,
  amount: BigDecimal,
  currency: String,
  category: String,
  sourceId: Option[UUID],
  description: Option[String],
  createdAt: Instant
) {
  // Конвертация сырой строки PostgreSQL в чистый доменный агрегат (DDD)
  def toDomain: BillingTransaction = BillingTransaction(
    id = BillingTransactionId(id.toString), // Заворачиваем UUID в наш opaque-тип (String)
    companyId = CompanyId(companyId),
    amount = amount,
    currency = currency,
    category = category,
    sourceId = sourceId,
    description = description,
    createdAt = createdAt
  )
}

object BillingTransactionRow {
  // Anorm-парсер для автоматического маршаллинга строк SQL-ответа в BillingTransactionRow
  val parser: RowParser[BillingTransactionRow] = {
    get[UUID]("id") ~
      get[UUID]("company_id") ~
      get[BigDecimal]("amount") ~
      get[String]("currency") ~
      get[String]("category") ~
      get[Option[UUID]]("source_id") ~
      get[Option[String]]("description") ~
      get[Instant]("created_at") map {
      case id ~ companyId ~ amount ~ currency ~ category ~ sourceId ~ description ~ createdAt =>
        BillingTransactionRow(id, companyId, amount, currency, category, sourceId, description, createdAt)
    }
  }

  // Сборка плоской строки БД из иммутабельного доменного объекта перед записью в Postgres
  def fromDomain(transaction: BillingTransaction): BillingTransactionRow = BillingTransactionRow(
    id = UUID.fromString(transaction.id.value), // Извлекаем String через extension и парсим в UUID
    companyId = transaction.companyId.raw,      // Получаем чистый UUID компании через метод расширения
    amount = transaction.amount,
    currency = transaction.currency,
    category = transaction.category,
    sourceId = transaction.sourceId,
    description = transaction.description,
    createdAt = transaction.createdAt
  )
}

