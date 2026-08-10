package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gate_bookings.application.out.GateBookingRepository
import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import com.techmatrix18.companies.application.in.{DeductFundsUseCase, DeductFundsCommand}
import com.techmatrix18.gates.application.out.GateRepository
import java.time.Instant
import java.util.UUID
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}

/**
 * MarkAsNoShowUseCase - Inbound Driving Service for processing expired and unfulfilled bookings
 * Фиксация неприбытия
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class MarkAsNoShowUseCase(
  bookingRepository: GateBookingRepository,
  gateRepository: GateRepository,
  deductFundsUseCase: DeductFundsUseCase // Внедряем финансовый модуль для автоматического штрафования
)(using ec: ExecutionContext) {

  // Executes the no-show scenario for a specific expired gate booking slot
  def execute(command: MarkAsNoShowCommand): Future[Either[String, MarkAsNoShowResponse]] = {

    // 1. Асинхронный поиск брони в PostgreSQL
    bookingRepository.findById(command.bookingId).flatMap {
      case None =>
        Future.successful(Left(s"Gate booking with ID '${command.bookingId.value}' not found"))

      case Some(booking) =>
        // 2. Валидация инвариантов времени и статусов
        if (booking.status == GateBookingStatus.NoShow) {
          Future.successful(Left("This booking slot has already been marked as NO_SHOW"))
        } else if (booking.status != GateBookingStatus.Scheduled) {
          Future.successful(Left(s"Cannot apply no-show logic to booking in status '${booking.status.code}'"))
        } else if (booking.scheduledEndTime.isAfter(Instant.now())) {
          Future.successful(Left("Cannot mark booking as NO_SHOW before its scheduled time window expires"))
        } else {

          // 3. Асинхронно запрашиваем сущность ворот, чтобы узнать коммерческий тариф hourlyRate
          gateRepository.findById(booking.gateId).flatMap {
            case None =>
              Future.successful(Left(s"Associated gate with ID '${booking.gateId.value}' not found for pricing check"))

            case Some(gate) =>
              // 4. Динамически извлекаем CompanyId через созданный нами SQL-мост в репозитории (Чистая 3NF)
              bookingRepository.getCompanyIdForBooking(booking.id).flatMap {
                case None =>
                  Future.successful(Left(s"Could not resolve financial owner (Company ID) for booking '${booking.id.value}'"))

                case Some(resolvedCompanyId) =>
                  val now = Instant.now()
                  // Вычисляем количество забронированных часов
                  val bookedHours = Math.max(1L, ChronoUnit.HOURS.between(booking.scheduledStartTime, booking.scheduledEndTime))
                  val penaltyAmount = gate.hourlyRate * BigDecimal(bookedHours) // 👈 Исправлено: теперь gate доступен в области видимости!

                  // 5. Формируем строгую финансовую команду списания штрафа
                  val financialCommand = DeductFundsCommand(
                    companyId = resolvedCompanyId,
                    amount = penaltyAmount,
                    category = "GATE_NOSHOW_PENALTY",
                    sourceId = Some(UUID.fromString(booking.id.value)) // 👈 Исправлено: безопасный парсинг String-ID в UUID
                  )

                  // 6. Вызываем междоменное финансовое списание
                  deductFundsUseCase.execute(financialCommand).flatMap {
                    case Left(financeError) =>
                      // Фиксируем неявку, даже если биллинг вернул ошибку (аккаунт уйдет в минус автоматически)
                      val updatedBooking = booking.copy(status = GateBookingStatus.NoShow, updatedAt = now)
                      bookingRepository.update(updatedBooking).map { _ =>
                        Left(s"Booking marked as NO_SHOW, but penalty billing failed: $financeError")
                      }

                    case Right(_) =>
                      // Успешный финтех-цикл: штраф ушел в Ledger, статус обновлен
                      val updatedBooking = booking.copy(status = GateBookingStatus.NoShow, updatedAt = now)
                      bookingRepository.update(updatedBooking).map { _ =>
                        Right(MarkAsNoShowResponse(
                          bookingId = updatedBooking.id.value,
                          status = updatedBooking.status.code,
                          penaltyApplied = penaltyAmount,
                          updatedAt = now
                        ))
                      }
                  }
              }
          }
        }
    }.recover {
      case error: Exception =>
        Left(s"Failed to process no-show transaction due to database error: ${error.getMessage}")
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class MarkAsNoShowResponse(
  bookingId: String,
  status: String,
  penaltyApplied: BigDecimal,
  updatedAt: Instant
)

