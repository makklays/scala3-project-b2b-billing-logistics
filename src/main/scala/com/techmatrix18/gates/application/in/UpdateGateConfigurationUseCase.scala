package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.application.out.GateRepository
import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus, WorkingHours}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * UpdateGateConfigurationUseCase - Inbound Driving Service for modifying gate parameters
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class UpdateGateConfigurationUseCase(
  gateRepository: GateRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of updating warehouse gate configuration (number and working hours)
  def execute(command: UpdateGateConfigurationCommand): Future[Either[String, UpdateGateConfigurationResponse]] = {

    // 1. Прикладная валидация входных данных профиля ворот
    if (command.gateNumber.trim.isEmpty) {
      Future.successful(Left("Gate number/identifier cannot be empty"))
    } else {

      // 2. Асинхронный поиск ворот в PostgreSQL через Out-порт репозитория
      gateRepository.findById(command.gateId).flatMap {
        case None =>
          Future.successful(Left(s"Warehouse gate with ID '${command.gateId.value}' not found"))

        case Some(gate) =>
          // Проверяем доменный инвариант: нельзя переконфигурировать полностью списанные ворота
          if (gate.status == GateStatus.Maintenance) {
            Future.successful(Left("Cannot update configuration parameters for a gate currently under maintenance"))
          } else {

            // 3. Создаем иммутабельный слепок сущности домена с новыми конфигурационными параметрами
            val updatedGate = gate.copy(
              gateNumber = command.gateNumber.trim,
              workingHours = command.workingHours, // Прямой маппинг Value Object WorkingHours
              updatedAt = Instant.now()
            )

            // 4. Сохраняем обновленные данные ворот в PostgreSQL через репозиторий
            gateRepository.update(updatedGate).map { _ =>
              Right(UpdateGateConfigurationResponse(
                gateId = updatedGate.id.value, // Наш метод расширения (extension) из GateId
                gateNumber = updatedGate.gateNumber,
                updatedAt = updatedGate.updatedAt
              ))
            }.recover {
              case error: Exception =>
                Left(s"Failed to update gate configuration due to database infrastructure error: ${error.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class UpdateGateConfigurationResponse(
  gateId: String,
  gateNumber: String,
  updatedAt: Instant
)

