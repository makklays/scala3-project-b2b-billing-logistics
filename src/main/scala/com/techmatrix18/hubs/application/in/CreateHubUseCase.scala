package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.{Hub, HubId, HubStatus, HubType}
import com.techmatrix18.hubs.application.out.HubRepository
import com.techmatrix18.companies.domain.CompanyId
import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * CreateHubUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class CreateHubUseCase(
  hubRepository: HubRepository
)(using ec: ExecutionContext) {

  /**
   * Executes the scenario of creating a new logistics hub
   *
   * @return Future[Either[String, CreateHubResponse]]
   */
  def execute(command: CreateHubCommand): Future[Either[String, CreateHubResponse]] = {

    // 1. Прикладная валидация критических полей
    if (command.title.trim.isEmpty) {
      Future.successful(Left("Hub title cannot be empty"))
    } else if (command.latitude == 0 || command.longitude == 0) {
      Future.successful(Left("Valid GPS coordinates are required for warehouse navigation"))
    } else {

      // 2. Парсинг строковых ENUM из команды в строго типизированные доменные типы
      val parsedHubType = command.hubType.trim.toUpperCase match {
        case "SEA_PORT" => HubType.SeaPort
        case "AIRPORT" => HubType.Airport
        case _ => HubType.LandTerminal // Дефолтное значение для сухопутных терминалов
      }

      // 3. Сборка иммутабельного Aggregate Root домена Hub
      val newHub = Hub(
        id = HubId(UUID.randomUUID().toString),   // Генерация нового UUID
        //companyId = CompanyId(UUID.fromString(command.companyId)), // Привязка к компании-владельцу
        companyId = CompanyId(command.companyId),
        title = command.title.trim,
        description = command.description, // Автоматически мапится Option[String]
        hubType = parsedHubType,
        status = HubStatus.Active, // Новый хаб активен по умолчанию
        countryCode = command.countryCode.trim.toUpperCase,
        city = command.city.trim,
        postalCode = command.postalCode.trim,
        addressLine = command.addressLine.trim,
        latitude = command.latitude,
        longitude = command.longitude,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
      )

      // 4. Сохранение сущности в PostgreSQL через Out-порт репозитория
      hubRepository.create(newHub).map { _ =>
        Right(CreateHubResponse(
          hubId = newHub.id.value, // Извлекаем String через наш метод расширения
          title = newHub.title,
          createdAt = newHub.createdAt
        ))
      }.recover {
        case error: Exception =>
          Left(s"Failed to persist logistics hub due to database infrastructure error: ${error.getMessage}")
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class CreateHubResponse(
  hubId: String,      // Возвращаем как String для легкого маппинга в Play JSON
  title: String,      // Название созданного хаба
  createdAt: Instant  // Точная метка времени из PostgreSQL (TIMESTAMP WITH TIME ZONE)
)

