package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.application.out.GateRepository
import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * UpdateGateRatesUseCase - Inbound Driving Service for managing gate pricing and billing rates
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class UpdateGateRatesUseCase(
  gateRepository: GateRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of updating warehouse gate financial rates
  def execute(command: UpdateGateRatesCommand): Future[Either[String, UpdateGateRatesResponse]] = {

    // 1. Прикладная валидация тарифов биллинга
    if (command.newHourlyRate <= 0) {
      Future.successful(Left("Base hourly rate must be strictly greater than zero"))
    } else if (command.newOvertimeHourlyRate <= 0) {
      Future.successful(Left("Overtime hourly rate/penalty must be strictly greater than zero"))
    } else {

      // 2. Асинхронный поиск ворот в PostgreSQL через Out-порт репозитория
      gateRepository.findById(command.gateId).flatMap {
        case None =>
          Future.successful(Left(s"Warehouse gate with ID '${command.gateId.value}' not found"))

        case Some(gate) =>
          // Проверяем доменный инвариант: нельзя менять коммерческие тарифы для полностью списанных ворот
          if (gate.status == GateStatus.Maintenance) {
            Future.successful(Left("Cannot modify billing rates for a gate currently under maintenance"))
          } else {

            // 3. Создаем иммутабельный слепок сущности домена с обновленной тарифной сеткой
            val updatedGate = gate.copy(
              hourlyRate = command.newHourlyRate,
              overtimeHourlyRate = command.newOvertimeHourlyRate,
              updatedAt = Instant.now()
            )

            // 4. Сохраняем измененную сущность в базу данных
            gateRepository.update(updatedGate).map { _ =>
              Right(UpdateGateRatesResponse(
                gateId = updatedGate.id.value, // Наш метод расширения (extension) из GateId
                newHourlyRate = updatedGate.hourlyRate,
                newOvertimeHourlyRate = updatedGate.overtimeHourlyRate,
                updatedAt = updatedGate.updatedAt
              ))
            }.recover {
              case error: Exception =>
                Left(s"Failed to update gate billing rates due to database error: ${error.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class UpdateGateRatesResponse(
  gateId: String,
  newHourlyRate: BigDecimal,
  newOvertimeHourlyRate: BigDecimal,
  updatedAt: Instant
)

