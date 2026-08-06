package com.techmatrix18.hub_sections.application.in

import com.techmatrix18.hub_sections.application.out.HubSectionRepository
import com.techmatrix18.hub_sections.domain.{HubSection, HubSectionId}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * DeleteHubSectionUseCase - Inbound Driving Service for decommissioning warehouse zones
 * Мягкое удаление
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class DeleteHubSectionUseCase(
  hubSectionRepository: HubSectionRepository
)(using ec: ExecutionContext) {

  // Executes the decommissioning scenario of a hub warehouse section
  def execute(command: DeleteHubSectionCommand): Future[Either[String, DeleteHubSectionResponse]] = {

    // 1. Асинхронно проверяем существование секции в PostgreSQL черезfindById
    hubSectionRepository.findById(command.sectionId).flatMap {
      case None =>
        Future.successful(Left(s"Hub section with ID '${command.sectionId.value}' not found"))

      case Some(section) =>
        // 2. Вызываем Out-порт удаления из репозитория (возвращает Future[Unit])
        hubSectionRepository.delete(command.sectionId).map { _ =>
          Right(DeleteHubSectionResponse(
            sectionId = command.sectionId.value, // Наш метод расширения для извлечения String
            deletedAt = Instant.now()
          ))
        }.recover {
          case error: Exception =>
            Left(s"Failed to delete hub section due to database error: ${error.getMessage}")
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class DeleteHubSectionResponse(
  sectionId: String,
  deletedAt: Instant
)