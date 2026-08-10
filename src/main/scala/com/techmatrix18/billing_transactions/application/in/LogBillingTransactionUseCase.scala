package com.techmatrix18.billing_transactions.application.in

import com.techmatrix18.billing_transactions.application.out.BillingTransactionRepository
import com.techmatrix18.billing_transactions.domain.{BillingTransaction, BillingTransactionId}
import com.techmatrix18.companies.domain.CompanyId
import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * LogBillingTransactionUseCase - Прикладной сервис для ведения неизменяемой финансовой книги учета
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class LogBillingTransactionUseCase(
  transactionRepository: BillingTransactionRepository
)(using ec: ExecutionContext) {

  // Записывает новую неизменяемую финансовую проводку в PostgreSQL
  def execute(command: LogBillingTransactionCommand): Future[Either[String, LogBillingTransactionResponse]] = {

    // 1. Прикладная валидация входных данных
    if (command.amount == 0) {
      Future.successful(Left("Financial transaction amount cannot be zero"))
    } else if (command.currency.trim.length != 3) {
      Future.successful(Left("Currency must be a valid 3-letter ISO code (e.g., EUR, USD)"))
    } else if (command.category.trim.isEmpty) {
      Future.successful(Left("Transaction category is required for financial auditing"))
    } else {

      val now = Instant.now()
      val newTransactionId = BillingTransactionId(UUID.randomUUID().toString)

      // 2. Сборка нового иммутабельного Aggregate Root доменной транзакции (Append-Only)
      val newTransaction = BillingTransaction(
        id = newTransactionId,
        companyId = CompanyId(command.companyId), // Безопасно парсим Foreign Key компании
        amount = command.amount,
        currency = command.currency.trim.toUpperCase,
        category = command.category.trim.toUpperCase,
        sourceId = command.sourceId, // Option[UUID] документ-основание (например, ID брони ворот)
        description = command.description.map(_.trim), // Option[String] кастомный комментарий
        createdAt = now // Поле updatedAt отсутствует сознательно: финансовая история неизменяема
      )

      // 3. Сохранение записи в PostgreSQL через Out-порт репозитория
      transactionRepository.create(newTransaction).map { generatedId =>
        Right(LogBillingTransactionResponse(
          transactionId = generatedId.value, // Наш метод расширения (extension) из BillingTransactionId
          companyId = newTransaction.companyId.value,
          category = newTransaction.category,
          createdAt = newTransaction.createdAt
        ))
      }.recover {
        case error: Exception =>
          Left(s"Failed to append row to financial ledger due to database error: ${error.getMessage}")
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class LogBillingTransactionResponse(
  transactionId: String,
  companyId: String,
  category: String,
  createdAt: Instant
)

