package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus}
import com.techmatrix18.gates.application.out.GateRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * PutGateUnderMaintenanceUseCase - Inbound Driving Service for scheduling gate maintenance
 * Перевод ворот на техобслуживание
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class PutGateUnderMaintenanceUseCase(
  gateRepository: GateRepository
)(using ec: ExecutionContext) {

  // Executes the gate maintenance state transition
  def execute(command: PutGateUnderMaintenanceCommand): Future[Either[String, PutGateUnderMaintenanceResponse]] = {

    // 1. Асинхронный поиск ворот в PostgreSQL через Out-порт репозитория
    gateRepository.findById(command.gateId).flatMap {
      case None =>
        Future.successful(Left(s"Warehouse gate with ID '${command.gateId.value}' not found"))

      case Some(gate) =>
        // 2. Проверка доменных инвариантов
        if (gate.status == GateStatus.Maintenance) {
          Future.successful(Left("Warehouse gate is already under maintenance"))
        } else if (gate.status == GateStatus.Occupied) {
          Future.successful(Left("Cannot put gate under maintenance while a vehicle is currently occupying it"))
        } else {

          // 3. Создаем мутировавший иммутабельный слепок сущности со статусом Maintenance
          val maintenanceGate = gate.copy(
            status = GateStatus.Maintenance,
            updatedAt = Instant.now()
          )

          // 4. Сохраняем обновленное состояние ворот в PostgreSQL
          gateRepository.update(maintenanceGate).map { _ =>
            Right(PutGateUnderMaintenanceResponse(
              gateId = maintenanceGate.id.value, // Наш метод расширения (extension) из GateId
              status = "MAINTENANCE",
              updatedAt = maintenanceGate.updatedAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to update gate status due to database error: ${error.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class PutGateUnderMaintenanceResponse(
  gateId: String,
  status: String,
  updatedAt: Instant
)

