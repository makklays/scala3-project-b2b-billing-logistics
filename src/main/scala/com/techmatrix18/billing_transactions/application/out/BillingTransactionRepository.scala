package com.techmatrix18.billing_transactions.application.out

import com.techmatrix18.billing_transactions.domain.{BillingTransaction, BillingTransactionId}
import com.techmatrix18.companies.domain.CompanyId
import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

/**
 * BillingTransactionRepository - Outbound Driven Port for financial ledger auditing.
 * Строгий Append-Only контракт без методов изменения и удаления данных.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

trait BillingTransactionRepository {

  // Находит конкретную финансовую проводку в Ledger по её уникальному идентификатору
  def findById(transactionId: BillingTransactionId): Future[Option[BillingTransaction]]

  // КЛЮЧЕВОЙ МЕТОД APPEND: Создает новую неизменяемую финансовую запись в PostgreSQL.
  // Вызывается атомарно при каждом успешном изменении баланса B2B-клиента.
  def create(transaction: BillingTransaction): Future[BillingTransactionId]

  // Универсальный Senior-метод для поиска, выгрузки аналитики и налоговых аудит-проверок.
  // Позволяет делать выборки по конкретной компании, категории расходов или временному окну.
  def findByFilter(filter: BillingTransactionFilter): Future[List[BillingTransaction]]
}

