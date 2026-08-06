package com.techmatrix18.hub_sections.application.in

import com.techmatrix18.hub_sections.application.out.HubSectionRepository
import com.techmatrix18.hub_sections.domain.{HubSection, HubSectionId, SectionType}
import com.techmatrix18.hubs.domain.HubId
import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * CreateHubSectionUseCase - Inbound Driving Service for registering new warehouse zones
 * Создание секции
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class CreateHubSectionUseCase(
  hubSectionRepository: HubSectionRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of creating a new hub section / storage zone
  def execute(command: CreateHubSectionCommand): Future[Either[String, CreateHubSectionResponse]] = {

    // 1. Прикладная валидация входных данных
    if (command.sectionName.trim.isEmpty) {
      Future.successful(Left("Hub section name cannot be empty"))
    } else if (command.totalCapacity <= 0) {
      Future.successful(Left("Total capacity for a warehouse section must be strictly positive"))
    } else {

      // 2. Безопасный парсинг строкового типа секции в доменный Enum
      val parsedSectionType = command.sectionType.trim.toUpperCase match {
        case "COLD_ZONE" => SectionType.ColdZone
        case "BULK_ZONE" => SectionType.BulkZone
        case "HAZARDOUS_ZONE" => SectionType.HazardousZone
        case _ => SectionType.PalletZone // Сухой стандартный стеллаж по умолчанию
      }

      val now = Instant.now()
      val newSectionId = HubSectionId(UUID.randomUUID().toString) // Генерируем уникальный ID секции

      // 3. Сборка иммутабельного Aggregate Root HubSection в точном соответствии с вашей 3NF БД
      val newSection = HubSection(
        id = newSectionId,
        hubId = HubId(UUID.fromString(command.hubId)), // Привязка Foreign Key к хабу
        sectionName = command.sectionName.trim,
        sectionType = parsedSectionType,
        totalCapacity = command.totalCapacity,
        createdAt = now,
        updatedAt = now
      )

      // 4. Сохранение в PostgreSQL через Out-порт репозитория
      hubSectionRepository.create(newSection).map { generatedId =>
        Right(CreateHubSectionResponse(
          sectionId = generatedId.value, // Наш метод расширения (extension) из HubSectionId
          sectionName = newSection.sectionName,
          createdAt = newSection.createdAt
        ))
      }.recover {
        case error: Exception =>
          Left(s"Failed to persist hub warehouse section due to database error: ${error.getMessage}")
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class CreateHubSectionResponse(
  sectionId: String,
  sectionName: String,
  createdAt: Instant
)

