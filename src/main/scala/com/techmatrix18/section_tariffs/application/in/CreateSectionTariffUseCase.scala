package com.techmatrix18.section_tariffs.application.in

import com.techmatrix18.hub_sections.application.out.HubSectionRepository
import com.techmatrix18.hub_sections.domain.{HubSectionId, SectionType}
import com.techmatrix18.section_tariffs.application.out.SectionTariffRepository
import com.techmatrix18.section_tariffs.domain.{SectionTariff, SectionTariffId}
import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * CreateSectionTariffUseCase - Inbound Driving Service for scheduling and issuing new B2B contract rates
 * Назначение тарифа
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class CreateSectionTariffUseCase(
  tariffRepository: SectionTariffRepository,
  hubSectionRepository: HubSectionRepository // Внедряем для проверки существования секции склада
)(using ec: ExecutionContext) {

  // Executes the scenario of assigning a new custom tariff for a B2B client
  def execute(command: CreateSectionTariffCommand): Future[Either[String, CreateSectionTariffResponse]] = {

    // 1. Прикладная валидация временного окна и финансовых рейтов
    if (command.validFrom.isAfter(command.validTo)) {
      Future.successful(Left("Invalid contract window: validFrom cannot be after validTo"))
    } else if (command.occupiedRatePerHour <= 0 || command.emptyReservationRatePerHour <= 0) {
      Future.successful(Left("Billing rates for warehouse storage must be strictly greater than zero"))
    } else if (command.clientName.trim.isEmpty) {
      Future.successful(Left("Client name is required for registering a custom B2B tariff contract"))
    } else {

      // 2. Асинхронно проверяем существование целевой секции склада
      val targetSectionId = HubSectionId(UUID.fromString(command.hubSectionId).toString)

      hubSectionRepository.findById(targetSectionId).flatMap {
        case None =>
          Future.successful(Left(s"Target warehouse section with ID '${command.hubSectionId}' not found"))

        case Some(section) =>

          // 3. Безопасный парсинг строкового типа секции в доменный Enum
          val parsedSectionType = command.sectionType.trim.toUpperCase match {
            case "COLD_ZONE" => SectionType.ColdZone
            case "BULK_ZONE" => SectionType.BulkZone
            case "HAZARDOUS_ZONE" => SectionType.HazardousZone
            case _ => SectionType.PalletZone
          }

          val now = Instant.now()
          val newTariffId = SectionTariffId(UUID.randomUUID().toString) // Генерируем уникальный ID тарифа

          // 4. Сборка нового иммутабельного Aggregate Root домена SectionTariff
          val newTariff = SectionTariff(
            id = newTariffId,
            hubSectionId = section.id,
            sectionType = parsedSectionType,
            clientName = command.clientName.trim,
            occupiedRatePerHour = command.occupiedRatePerHour,
            emptyReservationRatePerHour = command.emptyReservationRatePerHour,
            validFrom = command.validFrom,
            validTo = command.validTo,
            createdAt = now,
            updatedAt = now
          )

          // 5. Сохраняем тарифный контракт в PostgreSQL через Out-порт репозитория
          tariffRepository.create(newTariff).map { generatedId =>
            Right(CreateSectionTariffResponse(
              tariffId = generatedId.value, // Наш метод расширения из SectionTariffId
              clientName = newTariff.clientName,
              validFrom = newTariff.validFrom,
              validTo = newTariff.validTo
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to persist section tariff contract due to database error: ${error.getMessage}")
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class CreateSectionTariffResponse(
  tariffId: String,
  clientName: String,
  validFrom: Instant,
  validTo: Instant
)

