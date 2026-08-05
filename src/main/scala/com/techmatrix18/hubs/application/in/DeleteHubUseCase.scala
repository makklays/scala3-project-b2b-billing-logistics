package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.{Hub, HubId, HubStatus}
import com.techmatrix18.hubs.application.out.HubRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * DeleteHubUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class DeleteHubUseCase(
  hubRepository: HubRepository
)(using ec: ExecutionContext) {

  // Executes soft delete scenario by switching hub status to 'Suspended'
  def execute(command: DeleteHubCommand): Future[Either[String, DeleteHubResponse]] = {
    // 1. Асинхронный поиск хаба в PostgreSQL
    hubRepository.findById(command.hubId).flatMap {
      case None =>
        Future.successful(Left(s"Logistics hub with ID '${command.hubId.value}' not found"))

      case Some(hub) =>
        // 2. Проверяем доменные инварианты
        if (hub.status == HubStatus.Suspended) {
          Future.successful(Left("Logistics hub is already suspended or decommissioned"))
        } else {
          // 3. Создаем иммутабельный слепок сущности со статусом Suspended
          val deletedHub = hub.copy(
            status = HubStatus.Suspended,
            updatedAt = Instant.now()
          )

          // 4. Сохраняем изменения состояния через Out-порт репозитория
          hubRepository.update(deletedHub).map { _ =>
            Right(DeleteHubResponse(
              hubId = deletedHub.id.value, // Наш метод расширения (extension) из HubId
              deletedAt = deletedHub.updatedAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to soft-delete logistics hub due to database error: ${error.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class DeleteHubResponse(
  hubId: String,
  deletedAt: Instant
)

