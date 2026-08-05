package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gate_bookings.application.out.GateBookingRepository
import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * RescheduleGateBookingUseCase - Inbound Driving Service for rescheduling gate bookings
 * Перенос времени брони
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */
class RescheduleGateBookingUseCase(
  bookingRepository: GateBookingRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of changing the time window for a gate booking
  def execute(command: RescheduleGateBookingCommand): Future[Either[String, RescheduleGateBookingResponse]] = {

    // 1. Прикладная валидация нового временного окна
    if (command.newStartTime.isAfter(command.newEndTime)) {
      Future.successful(Left("Invalid time window: new start time cannot be after end time"))
    } else if (command.newStartTime.isBefore(Instant.now())) {
      Future.successful(Left("Cannot reschedule a warehouse slot to the past"))
    } else {

      // 2. Асинхронно ищем бронирование в PostgreSQL по строгому ID
      bookingRepository.findById(command.bookingId).flatMap {
        case None =>
          Future.successful(Left(s"Gate booking with ID '${command.bookingId.value}' not found"))

        case Some(booking) =>
          // 3. Проверяем доменные инварианты: защищаем бизнес-логику от некорректных изменений
          if (booking.status == GateBookingStatus.Canceled) {
            Future.successful(Left("Cannot reschedule a canceled booking slot"))
          } else if (booking.status == GateBookingStatus.InProgress) {
            Future.successful(Left("Cannot reschedule booking: truck is already at the dock being processed"))
          } else if (booking.status == GateBookingStatus.Completed) {
            Future.successful(Left("Cannot reschedule a historically completed and closed booking"))
          } else {

            val now = Instant.now()

            // 4. Создаем иммутабельный слепок сущности с новыми границами временного окна
            val rescheduledBooking = booking.copy(
              scheduledStartTime = command.newStartTime,
              scheduledEndTime = command.newEndTime,
              updatedAt = now
            )

            // 5. Сохраняем обновленное состояние бронирования в базу данных
            bookingRepository.update(rescheduledBooking).map { _ =>
              Right(RescheduleGateBookingResponse(
                bookingId = rescheduledBooking.id.value,
                newStartTime = rescheduledBooking.scheduledStartTime,
                newEndTime = rescheduledBooking.scheduledEndTime,
                updatedAt = rescheduledBooking.updatedAt
              ))
            }.recover {
              case error: Exception =>
                Left(s"Failed to reschedule gate booking due to database error: ${error.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class RescheduleGateBookingResponse(
  bookingId: String,
  newStartTime: Instant,
  newEndTime: Instant,
  updatedAt: Instant
)

