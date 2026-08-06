package com.techmatrix18.cargo_balances.application.in

import com.techmatrix18.cargo_balances.application.out.CargoBalanceRepository
import com.techmatrix18.cargo_balances.domain.{CargoBalance, CargoBalanceId}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * AddCargoPayloadUseCase - Inbound Driving Service for processing warehouse stock replenishment
 * Прием груза на склад и увеличение баланса палет.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class AddCargoPayloadUseCase(
  cargoBalanceRepository: CargoBalanceRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of adding cargo pallets to a specific warehouse slot balance
  def execute(command: AddCargoPayloadCommand): Future[Either[String, AddCargoPayloadResponse]] = {

    // 1. Прикладная валидация входных параметров команды
    if (command.palletsAdded <= 0) {
      Future.successful(Left("The number of added pallets must be strictly greater than zero"))
    } else {

      // 2. Асинхронно ищем запись баланса в PostgreSQL через Out-порт репозитория
      cargoBalanceRepository.findById(command.balanceId).flatMap {
        case None =>
          Future.successful(Left(s"Cargo balance account with ID '${command.balanceId.value}' not found"))

        case Some(cargoBalance) =>
          try {
            // 3. Вызываем чистое доменное правило инкремента палет внутри агрегата (.credit)
            val updatedBalance = cargoBalance.credit(command.palletsAdded)

            // 4. Сохраняем обновленный иммутабельный слепок сущности в базу данных
            cargoBalanceRepository.update(updatedBalance).map { _ =>
              Right(AddCargoPayloadResponse(
                balanceId = updatedBalance.id.value, // Наш метод расширения (extension) из CargoBalanceId
                clientName = updatedBalance.clientName,
                totalPalletsNow = updatedBalance.currentPallets,
                updatedAt = updatedBalance.updatedAt
              ))
            }
          } catch {
            case ex: IllegalArgumentException =>
              // Перехватываем защитные инварианты (require) из доменного слоя
              Future.successful(Left(ex.getMessage))
            case error: Exception =>
              Future.successful(Left(s"Failed to update warehouse cargo balance due to database error: ${error.getMessage}"))
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class AddCargoPayloadResponse(
  balanceId: String,
  clientName: String,
  totalPalletsNow: Int,
  updatedAt: Instant
)

