package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.{Hub, HubId, HubStatus}
import com.techmatrix18.hubs.application.out.HubRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * ActiveHubUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class ActiveHubUseCase(
  hubRepository: HubRepository
)(using ec: ExecutionContext) {

  // Executes the logistics hub activation scenario
  def execute(command: ActivateHubCommand): Future[Either[String, ActiveHubResponse]] = {
    // 1. Асинхронный поиск хаба через Out-порт репозитория
    hubRepository.findById(command.hubId).flatMap {
      case None =>
        Future.successful(Left(s"Logistics hub with ID '${command.hubId.value}' not found"))

      case Some(hub) =>
        // 2. Проверка доменных инвариантов и текущего статуса
        if (hub.status == HubStatus.Active) {
          Future.successful(Left("Logistics hub is already active and operational"))
        } else {
          // 3. Создаем мутировавший иммутабельный слепок сущности через .copy
          val activatedHub = hub.copy(
            status = HubStatus.Active,
            updatedAt = Instant.now()
          )

          // 4. Сохраняем обновленное состояние в PostgreSQL
          hubRepository.update(activatedHub).map { _ =>
            Right(ActiveHubResponse(
              hubId = activatedHub.id.value, // Наш метод расширения (extension) из HubId
              activatedAt = activatedHub.updatedAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to update hub status due to an infrastructure error: ${error.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class ActiveHubResponse(
  hubId: String,
  activatedAt: Instant
)

