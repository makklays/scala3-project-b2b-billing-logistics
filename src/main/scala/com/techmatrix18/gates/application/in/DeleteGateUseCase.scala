package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.application.out.GateRepository
import com.techmatrix18.gates.domain.{Gate, GateId}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * DeleteGateUseCase - Inbound Driving Service for decommissioning warehouse gates
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 05.08.2026
 */

class DeleteGateUseCase(
  gateRepository: GateRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of activating warehouse gates
  def execute(command: DeleteGateCommand): Future[Either[String, DeleteGateResponse]] = {

    // 1. Асинхронно проверяем существование ворот в PostgreSQL
    gateRepository.findById(command.gateId).flatMap {
      case None =>
        Future.successful(Left(s"Warehouse gate with ID '${command.gateId.value}' not found"))

      case Some(gate) =>
        // 2. Вызываем Out-порт удаления из репозитория (возвращает Future[Unit])
        gateRepository.delete(command.gateId).map { _ =>
          Right(DeleteGateResponse(
            gateId = command.gateId.value, // Наш метод расширения для извлечения String
            deletedAt = Instant.now()
          ))
        }.recover {
          case error: Exception =>
            Left(s"Failed to delete warehouse gate due to database error: ${error.getMessage}")
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class DeleteGateResponse(
  gateId: String,
  deletedAt: Instant
)

