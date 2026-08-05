package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.{Hub, HubId, HubStatus}
import com.techmatrix18.hubs.application.out.HubRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * UpdateHubGpsCoordinatesUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class UpdateHubGpsCoordinatesUseCase(
  hubRepository: HubRepository
)(using ec: ExecutionContext) {

  // Executes logistics hub GPS coordinates update scenario
  def execute(command: UpdateHubGpsCoordinatesCommand): Future[Either[String, UpdateHubGpsCoordinatesResponse]] = {
    // 1. Прикладная валидация: координаты не должны быть строго нулевыми
    if (command.latitude == 0 || command.longitude == 0) {
      Future.successful(Left("Invalid GPS telemetry: latitude and longitude cannot be zero"))
    } else {

      // 2. Асинхронный поиск хаба в базе данных через Out-порт репозитория
      hubRepository.findById(command.hubId).flatMap {
        case None =>
          Future.successful(Left(s"Logistics hub with ID '${command.hubId.value}' not found"))

        case Some(hub) =>
          // Проверяем доменный инвариант: нельзя настраивать геолокацию списанного объекта
          if (hub.status == HubStatus.Suspended) {
            Future.successful(Left("Cannot update GPS coordinates for a suspended or decommissioned hub account"))
          } else {

            // 3. Создаем иммутабельный слепок сущности домена с новыми GPS-координатами
            val updatedHub = hub.copy(
              latitude = command.latitude,
              longitude = command.longitude,
              updatedAt = Instant.now()
            )

            // 4. Сохраняем обновленные данные в PostgreSQL
            hubRepository.update(updatedHub).map { _ =>
              Right(UpdateHubGpsCoordinatesResponse(
                hubId = updatedHub.id.value, // Наш метод расширения (extension) из HubId
                latitude = updatedHub.latitude,
                longitude = updatedHub.longitude,
                updatedAt = updatedHub.updatedAt
              ))
            }.recover {
              case error: Exception =>
                Left(s"Failed to update hub GPS coordinates due to database error: ${error.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class UpdateHubGpsCoordinatesResponse(
  hubId: String,
  latitude: BigDecimal,
  longitude: BigDecimal,
  updatedAt: Instant
)

