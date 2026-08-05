package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.{Hub, HubId, HubStatus}
import com.techmatrix18.hubs.application.out.HubRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * InactiveHubUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class InactiveHubUseCase(
  hubRepository: HubRepository
)(using ec: ExecutionContext) {

  // Executes logistics hub suspension scenario
  def execute(command: InactiveHubCommand): Future[Either[String, InactiveHubResponse]] = {
    // 1. Асинхронный поиск хаба в базе данных через Out-порт
    hubRepository.findById(command.hubId).flatMap {
      case None =>
        Future.successful(Left(s"Logistics hub with ID '${command.hubId.value}' not found"))

      case Some(hub) =>
        // 2. Проверка доменных инвариантов
        if (hub.status == HubStatus.Inactive) {
          Future.successful(Left("Logistics hub is already inactive"))
        } else {
          // 3. Создаем мутировавшую копию сущности с обновленным статусом
          val inactivatedHub = hub.copy(
            status = HubStatus.Inactive,
            updatedAt = Instant.now()
          )

          // 4. Сохраняем измененное состояние в PostgreSQL
          hubRepository.update(inactivatedHub).map { _ =>
            Right(InactiveHubResponse(
              hubId = inactivatedHub.id.value, // Наш метод расширения (extension)
              InactivatedAt = inactivatedHub.updatedAt
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
case class InactiveHubResponse(
  hubId: String,
  InactivatedAt: Instant
)

