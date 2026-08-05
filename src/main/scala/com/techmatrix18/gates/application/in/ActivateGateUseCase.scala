package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus}
import com.techmatrix18.gates.application.out.GateRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * ActivateGateUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class ActivateGateUseCase(
  gateRepository: GateRepository
)(using ec: ExecutionContext) {

  // Execute the use case to activate a warehouse gate
  def execute(command: ActivateGateCommand): Future[Either[String, ActivateGateResponse]] = {

    // 1. Асинхронный поиск ворот в PostgreSQL через Out-порт репозитория
    gateRepository.findById(command.gateId).flatMap {
      case None =>
        Future.successful(Left(s"Warehouse gate with ID '${command.gateId.value}' not found"))

      case Some(gate) =>
        // 2. Проверка доменных инвариантов
        if (gate.status == GateStatus.Available) {
          Future.successful(Left("Warehouse gate is already active and available for traffic"))
        } else {
          // 3. Создаем мутировавший иммутабельный слепок сущности со статусом Available
          val activatedGate = gate.copy(
            status = GateStatus.Available,
            updatedAt = Instant.now()
          )

          // 4. Сохраняем измененное состояние в базу данных
          gateRepository.update(activatedGate).map { _ =>
            Right(ActivateGateResponse(
              gateId = activatedGate.id.value, // Наш метод расширения (extension) из GateId
              activatedAt = activatedGate.updatedAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to activate warehouse gate due to database error: ${error.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class ActivateGateResponse(
  gateId: String,
  activatedAt: Instant
)

