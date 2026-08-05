package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gate_bookings.application.out.GateBookingRepository
import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import com.techmatrix18.gates.application.out.GateRepository
import com.techmatrix18.gates.domain.GateStatus
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * ArriveTruckUseCase - Inbound Driving Service for registering truck arrival at checkpoints
 * This use case handles the arrival of trucks at warehouse gates, ensuring that both the booking and physical gate statuses are updated accordingly.
 * Регистрация прибытия фуры
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */
class ArriveTruckUseCase(
  bookingRepository: GateBookingRepository,
  gateRepository: GateRepository
)(using ec: ExecutionContext) {

  // Executes the truck arrival scenario, updating both booking and physical gate status
  def execute(command: ArriveTruckCommand): Future[Either[String, ArriveTruckResponse]] = {

    // 1. Асинхронно ищем бронирование в PostgreSQL
    bookingRepository.findById(command.bookingId).flatMap {
      case None =>
        Future.successful(Left(s"Gate booking with ID '${command.bookingId.value}' not found"))

      case Some(booking) =>
        // 2. Проверяем доменные инварианты бронирования
        if (booking.status == GateBookingStatus.InProgress) {
          Future.successful(Left("Truck arrival has already been registered for this booking slot"))
        } else if (booking.status == GateBookingStatus.Completed || booking.status == GateBookingStatus.Canceled) {
          Future.successful(Left(s"Cannot activate booking in status '${booking.status.code}'"))
        } else {

          // 3. Асинхронно проверяем состояние физических ворот
          gateRepository.findById(booking.gateId).flatMap {
            case None =>
              Future.successful(Left(s"Assigned warehouse gate with ID '${booking.gateId.value}' not found"))

            case Some(gate) =>
              if (gate.status == GateStatus.Occupied) {
                Future.successful(Left(s"Cannot process arrival: warehouse gate '${gate.gateNumber}' is currently occupied by another vehicle"))
              } else if (gate.status == GateStatus.Maintenance) {
                Future.successful(Left(s"Cannot process arrival: gate '${gate.gateNumber}' is urgently undergoing maintenance"))
              } else {

                val now = Instant.now()

                // 4. Создаем иммутабельный слепок обновленной брони
                val updatedBooking = booking.copy(
                  status = GateBookingStatus.InProgress,
                  actualArrivalTime = Some(now),                 // Фиксируем точную метку времени заезда для биллинга
                  updatedAt = now
                )

                // 5. Создаем иммутабельный слепок обновленных физических ворот
                val updatedGate = gate.copy(
                  status = GateStatus.Occupied,
                  updatedAt = now
                )

                // 6. Проводим транзакционные обновления в БД через параллельную фор-цепочку
                for {
                  _ <- bookingRepository.update(updatedBooking)  // Метод обновления брони
                  _ <- gateRepository.update(updatedGate)        // Метод обновления ворот
                } yield {
                  Right(ArriveTruckResponse(
                    bookingId = updatedBooking.id.value,
                    gateId = updatedGate.id.value,
                    arrivalTime = now,
                    status = updatedBooking.status.code
                  ))
                }
              }
          }
        }
    }.recover {
      case error: Exception =>
        Left(s"Failed to register truck arrival due to infrastructure infrastructure error: ${error.getMessage}")
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class ArriveTruckResponse(
  bookingId: String,
  gateId: String,
  arrivalTime: Instant,
  status: String
)

