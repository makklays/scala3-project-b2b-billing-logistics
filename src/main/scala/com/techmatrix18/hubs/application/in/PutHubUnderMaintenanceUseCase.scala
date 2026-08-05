package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.{Hub, HubId, HubStatus}
import com.techmatrix18.hubs.application.out.HubRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * PutHubUnderMaintenanceUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class PutHubUnderMaintenanceUseCase(
  hubRepository: HubRepository
)(using ec: ExecutionContext) {

  // Executes logistics hub maintenance state transition
  def execute(command: PutHubUnderMaintenanceCommand): Future[Either[String, PutHubUnderMaintenanceResponse]] = {
    // 1. Асинхронный поиск хаба в базе данных через Out-порт репозитория
    hubRepository.findById(command.hubId).flatMap {
      case None =>
        Future.successful(Left(s"Logistics hub with ID '${command.hubId.value}' not found"))

      case Some(hub) =>
        // 2. Проверка доменных инвариантов
        if (hub.status == HubStatus.Maintenance) {
          Future.successful(Left("Logistics hub is already under maintenance"))
        } else if (hub.status == HubStatus.Suspended) {
          Future.successful(Left("Cannot put a fully suspended or decommissioned hub under maintenance"))
        } else {
          // 3. Создаем мутировавшую иммутабельную копию сущности с обновленным статусом
          val maintenanceHub = hub.copy(
            status = HubStatus.Maintenance,
            updatedAt = Instant.now()
          )

          // 4. Сохраняем измененное состояние в PostgreSQL
          hubRepository.update(maintenanceHub).map { _ =>
            Right(PutHubUnderMaintenanceResponse(
              hubId = maintenanceHub.id.value, // Наш метод расширения (extension) из HubId
              status = "MAINTENANCE",
              updatedAt = maintenanceHub.updatedAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to update hub status due to database infrastructure error: ${error.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class PutHubUnderMaintenanceResponse(
  hubId: String,
  status: String,
  updatedAt: Instant
)

