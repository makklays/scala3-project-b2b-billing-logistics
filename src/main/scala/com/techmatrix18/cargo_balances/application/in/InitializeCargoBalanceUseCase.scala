package com.techmatrix18.cargo_balances.application.in

import com.techmatrix18.cargo_balances.application.out.CargoBalanceRepository
import com.techmatrix18.cargo_balances.domain.{CargoBalance, CargoBalanceId}
import com.techmatrix18.hub_sections.application.out.HubSectionRepository
import com.techmatrix18.hub_sections.domain.HubSectionId
import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * InitializeCargoBalanceUseCase - Inbound Driving Service for opening new stock tracking cells
 * Инициализация ячейки товарного учета для B2B-клиента в конкретной секции склада.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class InitializeCargoBalanceUseCase(
  cargoBalanceRepository: CargoBalanceRepository,
  hubSectionRepository: HubSectionRepository // Внедряем для проверки целостности логистической цепи
)(using ec: ExecutionContext) {

  // Executes the scenario of opening a new cargo balance account
  def execute(command: InitializeCargoBalanceCommand): Future[Either[String, InitializeCargoBalanceResponse]] = {

    // 1. Прикладная валидация входных текстовых данных
    if (command.clientName.trim.isEmpty) {
      Future.successful(Left("Client name is required to initialize a warehouse cargo balance cell"))
    } else {

      // 2. Асинхронно проверяем существование целевой секции хаба в PostgreSQL (Защита 3NF связей)
      val targetSectionId = HubSectionId(UUID.fromString(command.hubSectionId).toString)

      hubSectionRepository.findById(targetSectionId).flatMap {
        case None =>
          Future.successful(Left(s"Target warehouse section with ID '${command.hubSectionId}' not found"))

        case Some(section) =>
          val now = Instant.now()
          val newBalanceId = CargoBalanceId(UUID.randomUUID().toString) // Генерируем уникальный ID баланса

          // 3. Сборка нового иммутабельного Aggregate Root домена CargoBalance с нулевым остатком
          val newBalance = CargoBalance(
            id = newBalanceId,
            hubSectionId = section.id,
            clientName = command.clientName.trim,
            currentPallets = 0, // При инициализации ячейка пуста, палеты придут через AddCargoPayload
            createdAt = now,
            updatedAt = now
          )

          // 4. Сохраняем агрегат остатков в PostgreSQL через Out-порт репозитория
          cargoBalanceRepository.create(newBalance).map { generatedId =>
            Right(InitializeCargoBalanceResponse(
              balanceId = generatedId.value, // Наш метод расширения (extension) из CargoBalanceId
              hubSectionId = section.id.value,
              clientName = newBalance.clientName,
              createdAt = newBalance.createdAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to initialize warehouse cargo balance due to database error: ${error.getMessage}")
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class InitializeCargoBalanceResponse(
  balanceId: String,
  hubSectionId: String,
  clientName: String,
  createdAt: Instant
)

