package com.techmatrix18.cargo_transactions.application.out

import com.techmatrix18.cargo_transactions.domain.{CargoTransaction, CargoTransactionId, OperationType}
import com.techmatrix18.hub_sections.domain.HubSectionId
import com.techmatrix18.gate_bookings.domain.GateBookingId
import java.time.Instant
import scala.concurrent.Future

/**
 * CargoTransactionRepository - Outbound Driven Port for financial and cargo ledger audit trail.
 * Строгий Append-Only контракт без методов обновления и удаления.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

trait CargoTransactionRepository {

  // Находит конкретную транзакцию в книге учета по её уникальному идентификатору
  def findById(transactionId: CargoTransactionId): Future[Option[CargoTransaction]]

  // КЛЮЧЕВОЙ МЕТОД APPEND: Создает новую неизменяемую запись в PostgreSQL.
  // Вызывается атомарно при каждом успешном изменении баланса палет.
  def create(transaction: CargoTransaction): Future[CargoTransactionId]

  // Универсальный Senior-метод для поиска, выгрузки отчетов и аудит-проверок.
  // Позволяет делать выборки за конкретный период (fromDate/toDate) по клиенту или воротам.
  def findByFilter(filter: CargoTransactionFilter): Future[List[CargoTransaction]]
}

