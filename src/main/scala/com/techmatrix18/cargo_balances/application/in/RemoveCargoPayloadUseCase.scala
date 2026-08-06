package com.techmatrix18.cargo_balances.application.in

import com.techmatrix18.cargo_balances.application.out.CargoBalanceRepository
import com.techmatrix18.cargo_balances.domain.{CargoBalance, CargoBalanceId}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * RemoveCargoPayloadUseCase - Inbound Driving Service for processing warehouse stock dispatch
 * Отгрузка груза со склада и уменьшение баланса палет.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class RemoveCargoPayloadUseCase(
  cargoBalanceRepository: CargoBalanceRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of removing cargo pallets from a specific warehouse slot balance
  def execute(command: RemoveCargoPayloadCommand): Future[Either[String, RemoveCargoPayloadResponse]] = {

    // 1. Прикладная валидация входных параметров команды
    if (command.palletsRemoved <= 0) {
      Future.successful(Left("The number of removed pallets must be strictly greater than zero"))
    } else {

      // 2. Асинхронно ищем запись баланса в PostgreSQL через Out-порт репозитория
      cargoBalanceRepository.findById(command.balanceId).flatMap {
        case None =>
          Future.successful(Left(s"Cargo balance account with ID '${command.balanceId.value}' not found"))

        case Some(cargoBalance) =>
          // 3. Вызываем чистое доменное правило дебета палет внутри агрегата (.debit)
          // Монада Either защищает нас от ухода остатков на складе в некорректный минус
          cargoBalance.debit(command.palletsRemoved) match {
            case Left(domainError) =>
              Future.successful(Left(domainError))

            case Right(updatedBalance) =>
              // 4. Сохраняем обновленный иммутабельный слепок сущности в базу данных
              cargoBalanceRepository.update(updatedBalance).map { _ =>
                Right(RemoveCargoPayloadResponse(
                  balanceId = updatedBalance.id.value, // Наш метод расширения (extension) из CargoBalanceId
                  clientName = updatedBalance.clientName,
                  totalPalletsLeft = updatedBalance.currentPallets,
                  updatedAt = updatedBalance.updatedAt
                ))
              }.recover {
                case error: Exception =>
                  Left(s"Failed to update warehouse cargo balance due to database error: ${error.getMessage}")
              }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class RemoveCargoPayloadResponse(
  balanceId: String,
  clientName: String,
  totalPalletsLeft: Int,
  updatedAt: Instant
)

