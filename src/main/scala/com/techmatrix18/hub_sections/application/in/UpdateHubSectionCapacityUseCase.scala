package com.techmatrix18.hub_sections.application.in

import com.techmatrix18.hub_sections.application.out.HubSectionRepository
import com.techmatrix18.hub_sections.domain.{HubSection, HubSectionId}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * UpdateHubSectionCapacityUseCase - Inbound Driving Service for updating warehouse zone scale
 * Изменение емкости
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */
class UpdateHubSectionCapacityUseCase(
  hubSectionRepository: HubSectionRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of updating a hub section total storage capacity
  def execute(command: UpdateHubSectionCapacityCommand): Future[Either[String, UpdateHubSectionCapacityResponse]] = {

    // 1. Прикладная валидация входных данных емкости
    if (command.newTotalCapacity <= 0) {
      Future.successful(Left("New total capacity value must be strictly greater than zero"))
    } else {

      // 2. Асинхронно ищем секцию в PostgreSQL по строгому ID
      hubSectionRepository.findById(command.sectionId).flatMap {
        case None =>
          Future.successful(Left(s"Hub section with ID '${command.sectionId.value}' not found"))

        case Some(section) =>
          val now = Instant.now()

          // 3. Создаем иммутабельный слепок сущности домена с измененной емкостью
          val updatedSection = section.copy(
            totalCapacity = command.newTotalCapacity,
            updatedAt = now
          )

          // 4. Сохраняем измененный агрегат в базу данных через репозиторий
          hubSectionRepository.update(updatedSection).map { _ =>
            Right(UpdateHubSectionCapacityResponse(
              sectionId = updatedSection.id.value, // Наш метод расширения из HubSectionId
              newTotalCapacity = updatedSection.totalCapacity,
              updatedAt = updatedSection.updatedAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to update hub section capacity due to database error: ${error.getMessage}")
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class UpdateHubSectionCapacityResponse(
  sectionId: String,
  newTotalCapacity: BigDecimal,
  updatedAt: Instant
)

