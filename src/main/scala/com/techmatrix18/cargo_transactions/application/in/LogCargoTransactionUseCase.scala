package com.techmatrix18.cargo_transactions.application.in

import com.techmatrix18.cargo_transactions.application.out.CargoTransactionRepository
import com.techmatrix18.cargo_transactions.domain.{CargoTransaction, CargoTransactionId, OperationType}
import com.techmatrix18.hub_sections.domain.HubSectionId
import com.techmatrix18.gate_bookings.domain.GateBookingId
import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * LogCargoTransactionUseCase - Прикладной сервис для ведения неизменяемого журнала учета грузов
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class LogCargoTransactionUseCase(
  transactionRepository: CargoTransactionRepository
)(using ec: ExecutionContext) {

  // Записывает новую неизменяемую транзакцию движения груза в PostgreSQL
  def execute(command: LogCargoTransactionCommand): Future[Either[String, LogCargoTransactionResponse]] = {

    // 1. Прикладная валидация входного дельты палет
    if (command.palletsDelta <= 0) {
      Future.successful(Left("Pallets delta inside a ledger transaction must be strictly positive"))
    } else if (command.clientName.trim.isEmpty) {
      Future.successful(Left("Client name is required for accounting audit trail"))
    } else {

      // 2. Безопасный парсинг строкового типа операции в доменный Enum
      val parsedOperationType = command.operationType.trim.toUpperCase match {
        case "DISPATCH" => OperationType.Dispatch
        case _ => OperationType.Supply // По умолчанию - поступление на склад
      }

      val now = Instant.now()
      val newTransactionId = CargoTransactionId(UUID.randomUUID().toString)

      // 3. Сборка иммутабельного Aggregate Root доменной транзакции (Append-Only)
      val newTransaction = CargoTransaction(
        id = newTransactionId,
        hubSectionId = HubSectionId(UUID.fromString(command.hubSectionId).toString),
        gateBookingId = GateBookingId(UUID.fromString(command.gateBookingId).toString),
        clientName = command.clientName.trim,
        operationType = parsedOperationType,
        palletsDelta = command.palletsDelta,
        createdAt = now // Нет поля updatedAt, транзакции неизменяемы
      )

      // 4. Сохранение записи в PostgreSQL через Out-порт репозитория
      transactionRepository.create(newTransaction).map { generatedId =>
        Right(LogCargoTransactionResponse(
          transactionId = generatedId.value, // Наш метод расширения (extension)
          operationType = newTransaction.operationType.code,
          createdAt = newTransaction.createdAt
        ))
      }.recover {
        case error: Exception =>
          Left(s"Failed to append row to cargo ledger due to database error: ${error.getMessage}")
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class LogCargoTransactionResponse(
  transactionId: String,
  operationType: String,
  createdAt: Instant
)

