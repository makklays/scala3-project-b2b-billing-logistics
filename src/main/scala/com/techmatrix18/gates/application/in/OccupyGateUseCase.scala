package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus}
import com.techmatrix18.gates.application.out.GateRepository
import com.techmatrix18.gate_bookings.application.out.GateBookingRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * OccupyGateUseCase - Полноценный бизнес-сценарий фиксации въезда фуры
 *
 * TODO: Пересмотреть использование GateBookingRepository, после добавления GateBookingRepository в проект,
 * TODO: чтобы убедиться, что он корректно интегрирован и используется для управления бронированиями ворот.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.3
 * @since 05.08.2026
 */

class OccupyGateUseCase(
  gateRepository: GateRepository,
  bookingRepository: GateBookingRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of occupying a warehouse gate when a truck arrives
  def execute(command: OccupyGateCommand): Future[Either[String, OccupyGateResponse]] = {

    // 1. Асинхронно ищем ворота в PostgreSQL
    gateRepository.findById(command.gateId).flatMap {
      case None =>
        Future.successful(Left(s"Warehouse gate with ID '${command.gateId.value}' not found"))

      case Some(gate) =>
        if (gate.status == GateStatus.Occupied) Future.successful(Left(s"Warehouse gate '${gate.gateNumber}' is already occupied")) else if (gate.status == GateStatus.Maintenance) Future.successful(Left(s"Gate '${gate.gateNumber}' is under maintenance")) else {

          // 2. Асинхронно проверяем саму бронь по bookingId
          bookingRepository.findById(command.bookingId).flatMap {
            case None =>
              Future.successful(Left(s"Gate booking with ID '${command.bookingId}' not found"))

            case Some(booking) =>
              val now = Instant.now()

              // 3. Создаем иммутабельный слепок обновленных ворот
              val occupiedGate = gate.copy(
                status = GateStatus.Occupied,
                updatedAt = now
              )

              // 4. Создаем иммутабельный слепок обновленной брони (активируем её)
              val activatedBooking = booking.copy(
                status = "IN_PROGRESS",          // Фура заехала на разгрузку
                actualArrivalTime = Some(now),   // Фиксируем точное время старта аренды для биллинга
                updatedAt = now
              )

              // 5. Компонуем два асинхронных вызова в единую цепочку (Future.sequence или flatMap)
              for {
                _ <- gateRepository.update(occupiedGate)
                _ <- bookingRepository.update(activatedBooking) // Используем bookingId!
              } yield {
                Right(OccupyGateResponse(
                  gateId = occupiedGate.id.value,
                  occupiedAt = now
                ))
              }
          }.recover {
            case error: Exception =>
              Left(s"Failed to update infrastructure state due to DB error: ${error.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class OccupyGateResponse(
  gateId: String,
  occupiedAt: Instant
)

