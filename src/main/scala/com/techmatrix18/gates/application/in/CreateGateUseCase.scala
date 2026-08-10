package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus}
import com.techmatrix18.hubs.domain.HubId
import com.techmatrix18.gates.application.out.GateRepository
import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import com.techmatrix18.gates.domain.GateType

/**
 * CreateGateUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class CreateGateUseCase(
  gateRepository: GateRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of creating a new warehouse gate / loading dock
  def execute(command: CreateGateCommand): Future[Either[String, CreateGateResponse]] = {

    // 1. Прикладная валидация входных данных
    if (command.gateNumber.trim.isEmpty) {
      Future.successful(Left("Gate number/identifier cannot be empty"))
    } else if (command.hourlyRate <= 0) {
      Future.successful(Left("Hourly rate for gate renting must be strictly positive"))
    } else {

      // 2. Безопасный парсинг строкового типа ворот в доменный Enum
      val parsedGateType = command.gateType.trim.toUpperCase match {
        case "COLD_STORAGE" => GateType.ColdStorage
        case "MARINE_BERTH" => GateType.MarineBerth
        case "SPACE_CARGO_DOCK" => GateType.SpaceDock
        case _ => GateType.Dry // По умолчанию сухой док
      }

      // 3. Сборка иммутабельного Aggregate Root Gate
      val newGate = Gate(
        id = GateId(UUID.randomUUID()),                // Заворачиваем UUID в строгий тип идентификатора
        hubId = HubId(UUID.fromString(command.hubId)), // Привязка Foreign Key к хабу
        gateNumber = command.gateNumber.trim,
        gateType = parsedGateType,
        status = GateStatus.Available,                 // Новые ворота свободны по умолчанию
        workingHours = command.workingHours,           // Объект WorkingHours передан напрямую из команды
        hourlyRate = command.hourlyRate,
        overtimeHourlyRate = command.overtimeHourlyRate,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
      )

      // 4. Сохранение в PostgreSQL через Out-порт репозитория
      gateRepository.create(newGate).map { generatedId =>
        Right(CreateGateResponse(
          gateId = generatedId.value,                  // Извлекаем String через метод расширения opaque-типа
          gateNumber = newGate.gateNumber
        ))
      }.recover {
        case error: Exception =>
          Left(s"Failed to persist warehouse gate due to infrastructure error: ${error.getMessage}")
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class CreateGateResponse(
  gateId: String,
  gateNumber: String
)

