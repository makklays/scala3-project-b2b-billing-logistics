package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gate_bookings.application.out.GateBookingRepository
import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * CancelGateBookingUseCase - Inbound Driving Service for canceling slot reservations
 * Отмена бронирования
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */
class CancelGateBookingUseCase(
  bookingRepository: GateBookingRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of canceling an upcoming gate booking slot
  def execute(command: CancelGateBookingCommand): Future[Either[String, CancelGateBookingResponse]] = {

    // 1. Асинхронно ищем бронирование в PostgreSQL по строгому ID
    bookingRepository.findById(command.bookingId).flatMap {
      case None =>
        Future.successful(Left(s"Gate booking with ID '${command.bookingId.value}' not found"))

      case Some(booking) =>
        // 2. Проверяем доменные инварианты: защищаем бизнес-логику от некорректных переводов статусов
        if (booking.status == GateBookingStatus.Canceled) {
          Future.successful(Left("This gate booking slot has already been canceled"))
        } else if (booking.status == GateBookingStatus.InProgress) {
          Future.successful(Left("Cannot cancel booking: truck is already at the dock and being processed"))
        } else if (booking.status == GateBookingStatus.Completed) {
          Future.successful(Left("Cannot cancel a historically completed log and closed booking"))
        } else {

          val now = Instant.now()

          // 3. Создаем иммутабельный слепок сущности со статусом CANCELED
          // Примечание: в зависимости от SLA, здесь можно генерировать вызов биллинга для списания штрафа за позднюю отмену
          val canceledBooking = booking.copy(
            status = GateBookingStatus.Canceled,
            updatedAt = now
          )

          // 4. Сохраняем обновленное состояние бронирования в базу данных
          bookingRepository.update(canceledBooking).map { _ =>
            Right(CancelGateBookingResponse(
              bookingId = canceledBooking.id.value,
              status = canceledBooking.status.code,
              canceledAt = canceledBooking.updatedAt
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to cancel gate booking due to database error: ${error.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class CancelGateBookingResponse(
  bookingId: String,
  status: String,
  canceledAt: Instant
)

