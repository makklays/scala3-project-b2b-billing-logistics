package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gate_bookings.application.out.GateBookingRepository
import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import com.techmatrix18.gates.application.out.GateRepository
import com.techmatrix18.gates.domain.GateStatus
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * DepartTruckUseCase - Inbound Driving Service for handling truck departure from docks
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */
class DepartTruckUseCase(
  bookingRepository: GateBookingRepository,
  gateRepository: GateRepository
)(using ec: ExecutionContext) {

  // Executes the truck departure scenario, completing the booking and making the physical gate available again
  def execute(command: DepartTruckCommand): Future[Either[String, DepartTruckResponse]] = {

    // 1. Асинхронно ищем бронирование в PostgreSQL по строгому доменному ID
    bookingRepository.findById(command.bookingId).flatMap {
      case None =>
        Future.successful(Left(s"Gate booking with ID '${command.bookingId.value}' not found"))

      case Some(booking) =>
        // 2. Проверяем доменные инварианты: выехать можно только из статуса IN_PROGRESS
        if (booking.status != GateBookingStatus.InProgress) {
          Future.successful(Left(s"Cannot register departure: booking is currently in status '${booking.status.code}', expected 'IN_PROGRESS'"))
        } else {

          // 3. Асинхронно ищем и проверяем состояние связанных физических ворот
          gateRepository.findById(booking.gateId).flatMap {
            case None =>
              Future.successful(Left(s"Associated warehouse gate with ID '${booking.gateId.value}' not found"))

            case Some(gate) =>
              val now = Instant.now()

              // 4. Создаем иммутабельный слепок завершенной брони (фиксируем время выезда для архива биллинга)
              val updatedBooking = booking.copy(
                status = GateBookingStatus.Completed,
                actualDepartureTime = Some(now),
                updatedAt = now
              )

              // 5. Создаем иммутабельный слепок освобожденных ворот (готовы принимать следующий трафик)
              val updatedGate = gate.copy(
                status = GateStatus.Available,
                updatedAt = now
              )

              // 6. Атомарно обновляем состояние обеих сущностей в PostgreSQL
              for {
                _ <- bookingRepository.update(updatedBooking)
                _ <- gateRepository.update(updatedGate)
              } yield {
                Right(DepartTruckResponse(
                  bookingId = updatedBooking.id.value,
                  gateId = updatedGate.id.value,
                  departureTime = now,
                  status = updatedBooking.status.code
                ))
              }
          }
        }
    }.recover {
      case error: Exception =>
        Left(s"Failed to register truck departure due to database infrastructure error: ${error.getMessage}")
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class DepartTruckResponse(
  bookingId: String,
  gateId: String,
  departureTime: Instant,
  status: String
)

