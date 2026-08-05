package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.{Hub, HubId, HubStatus}
import com.techmatrix18.hubs.application.out.HubRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * SuspendHubUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class SuspendHubUseCase(
  hubRepository: HubRepository
)(using ec: ExecutionContext) {

  // Executes logistics hub suspension scenario
  def execute(command: SuspendHubCommand): Future[Either[String, SuspendHubResponse]] = {
    // 1. Асинхронный поиск хаба в базе данных через Out-порт репозитория
    hubRepository.findById(command.hubId).flatMap {
      case None =>
        Future.successful(Left(s"Logistics hub with ID '${command.hubId.value}' not found"))

      case Some(hub) =>
        // 2. Проверка доменных инвариантов
        if (hub.status == HubStatus.Suspended) {
          Future.successful(Left("Logistics hub is already suspended"))
        } else {
          // 3. Создаем мутировавшую иммутабельную копию сущности с обновленным статусом
          val suspendedHub = hub.copy(
            status = HubStatus.Suspended,
            updatedAt = Instant.now()
          )

          // 4. Сохраняем измененное состояние в PostgreSQL
          hubRepository.update(suspendedHub).map { _ =>
            Right(SuspendHubResponse(
              hubId = suspendedHub.id.value, // Наш метод расширения (extension) из HubId
              suspendedAt = suspendedHub.updatedAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to suspend logistics hub due to database error: ${error.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class SuspendHubResponse(
  hubId: String,
  suspendedAt: Instant
)

