package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.{Hub, HubId, HubStatus}
import com.techmatrix18.hubs.application.out.HubRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * UpdateHubUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class UpdateHubUseCase(
  hubRepository: HubRepository
)(using ec: ExecutionContext) {

  // Executes logistics hub profile update scenario
  def execute(command: UpdateHubCommand): Future[Either[String, UpdateHubResponse]] = {

    // 1. Прикладная валидация входных данных профиля
    if (command.title.trim.isEmpty) {
      Future.successful(Left("Hub title cannot be empty"))
    } else if (command.addressLine.trim.isEmpty) {
      Future.successful(Left("Physical address line is required for cargo routing"))
    } else {

      // 2. Асинхронный поиск хаба в базе данных через Out-порт репозитория
      hubRepository.findById(command.hubId).flatMap {
        case None =>
          Future.successful(Left(s"Logistics hub with ID '${command.hubId.value}' not found"))

        case Some(hub) =>
          // Проверяем доменный инвариант: нельзя редактировать декоммиссированный хаб
          if (hub.status == HubStatus.Suspended) {
            Future.successful(Left("Cannot update profile data for a suspended or decommissioned hub account"))
          } else {

            // 3. Создаем иммутабельный слепок сущности домена с новыми реквизитами
            val updatedHub = hub.copy(
              title = command.title.trim,
              description = command.description, // Мапится Option[String], переданный из команды
              addressLine = command.addressLine.trim,
              postalCode = command.postalCode.trim,
              updatedAt = Instant.now()
            )

            // 4. Сохраняем обновленные данные хаба в PostgreSQL
            hubRepository.update(updatedHub).map { _ =>
              Right(UpdateHubResponse(
                hubId = updatedHub.id.value, // Наш метод расширения (extension) из HubId
                title = updatedHub.title,
                addressLine = updatedHub.addressLine,
                updatedAt = updatedHub.updatedAt
              ))
            }.recover {
              case error: Exception =>
                Left(s"Failed to update logistics hub profile due to database error: ${error.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class UpdateHubResponse(
  hubId: String,
  title: String,
  addressLine: String,
  updatedAt: Instant
)

