package com.techmatrix18.hub_sections.application.in

import com.techmatrix18.hub_sections.application.out.HubSectionRepository
import com.techmatrix18.hub_sections.domain.{HubSection, HubSectionId}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * RenameHubSectionUseCase - Inbound Driving Service for updating warehouse zone names
 * Переименование
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class RenameHubSectionUseCase(
  hubSectionRepository: HubSectionRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of renaming a hub section
  def execute(command: RenameHubSectionCommand): Future[Either[String, RenameHubSectionResponse]] = {

    // 1. Прикладная валидация входных данных
    if (command.newSectionName.trim.isEmpty) {
      Future.successful(Left("New hub section name cannot be empty"))
    } else {

      // 2. Асинхронно ищем секцию в PostgreSQL по строгому ID
      hubSectionRepository.findById(command.sectionId).flatMap {
        case None =>
          Future.successful(Left(s"Hub section with ID '${command.sectionId.value}' not found"))

        case Some(section) =>
          val now = Instant.now()

          // 3. Создаем иммутабельный слепок сущности домена с новым именем
          val updatedSection = section.copy(
            sectionName = command.newSectionName.trim,
            updatedAt = now
          )

          // 4. Сохраняем обновленное состояние секции в базу данных
          hubSectionRepository.update(updatedSection).map { _ =>
            Right(RenameHubSectionResponse(
              sectionId = updatedSection.id.value, // Наш метод расширения из HubSectionId
              newSectionName = updatedSection.sectionName,
              updatedAt = updatedSection.updatedAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to rename hub section due to database error: ${error.getMessage}")
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class RenameHubSectionResponse(
  sectionId: String,
  newSectionName: String,
  updatedAt: Instant
)

