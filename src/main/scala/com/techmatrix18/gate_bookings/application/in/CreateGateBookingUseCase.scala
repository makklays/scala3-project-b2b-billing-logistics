package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.companies.domain.CompanyId
import com.techmatrix18.gate_bookings.application.out.GateBookingRepository
import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import com.techmatrix18.gates.application.out.GateRepository
import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus}
import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * CreateGateBookingUseCase - Inbound Driving Service for scheduling warehouse slot reservations
 * Создание брони / Резервирование слота
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */
class CreateGateBookingUseCase(
  bookingRepository: GateBookingRepository,
  gateRepository: GateRepository
)(using ec: ExecutionContext) {

  // Executes the gate booking reservation scenario
  def execute(command: CreateGateBookingCommand): Future[Either[String, CreateGateBookingResponse]] = {

    // 1. Прикладная валидация временного окна
    if (command.scheduledStartTime.isAfter(command.scheduledEndTime)) {
      Future.successful(Left("Invalid time window: scheduled start time cannot be after end time"))
    } else if (command.scheduledStartTime.isBefore(Instant.now())) {
      Future.successful(Left("Cannot book a warehouse slot in the past"))
    } else {

      // 2. Асинхронно проверяем существование и доступность физических ворот
      val targetGateId = GateId(UUID.fromString(command.gateId))

      gateRepository.findById(targetGateId).flatMap {
        case None =>
          Future.successful(Left(s"Target warehouse gate with ID '${command.gateId}' not found"))

        case Some(gate) =>
          // 3. Проверка доменного инварианта: ворота должны быть пригодны для эксплуатации
          if (gate.status == GateStatus.Maintenance) {
            Future.successful(Left(s"Cannot book gate '${gate.gateNumber}' because it is currently under maintenance"))
          } else {

            val now = Instant.now()
            val newBookingId = GateBookingId(UUID.randomUUID().toString) // Генерируем ID брони

            // 4. Сборка нового иммутабельного Aggregate Root домена GateBooking
            val newBooking = GateBooking(
              id = newBookingId,
              gateId = gate.id,
              companyId = CompanyId(UUID.fromString(command.companyId)), // Привязка к компании для биллинга
              clientName = command.clientName.trim,
              truckLicensePlate = command.truckLicensePlate.trim.toUpperCase,
              scheduledStartTime = command.scheduledStartTime,
              scheduledEndTime = command.scheduledEndTime,
              actualArrivalTime = None, // При создании фуры еще нет у ворот
              actualDepartureTime = None,
              status = GateBookingStatus.Scheduled, // Статус по умолчанию
              createdAt = now,
              updatedAt = now
            )

            // 5. Сохраняем бронирование в PostgreSQL через Out-порт репозитория
            bookingRepository.create(newBooking).map { generatedId =>
              Right(CreateGateBookingResponse(
                bookingId = generatedId.value, // Наш метод расширения для извлечения String
                gateId = gate.id.value,
                scheduledStartTime = newBooking.scheduledStartTime,
                status = newBooking.status.code
              ))
            }.recover {
              case error: Exception =>
                Left(s"Failed to persist gate booking slot due to database error: ${error.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class CreateGateBookingResponse(
  bookingId: String,
  gateId: String,
  scheduledStartTime: Instant,
  status: String
)

