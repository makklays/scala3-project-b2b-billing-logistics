package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus}
import com.techmatrix18.gates.application.out.GateRepository
import com.techmatrix18.gate_bookings.application.out.GateBookingRepository
import com.techmatrix18.hubs.application.out.HubRepository
import com.techmatrix18.companies.application.in.DeductFundsUseCase
import com.techmatrix18.gates.application.in.ReleaseGateCommand
import com.techmatrix18.companies.application.in.DeductFundsCommand
import com.techmatrix18.companies.domain.CompanyId
import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}
import com.techmatrix18.gate_bookings.domain.GateBookingStatus

/**
 * ReleaseGateUseCase - Inbound Driving Service for truck departure and automated billing calculation
 * Сценарий освобождения ворот
 *
 * TODO: DeductFundsUseCase - Переделать сценарий списания средств с использованием Repositoty Списания Средств,
 * TODO: а не Use Case, чтобы не нарушать границы домена.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class ReleaseGateUseCase(
  gateRepository: GateRepository,
  bookingRepository: GateBookingRepository,
  hubRepository: HubRepository,
  deductFundsUseCase: DeductFundsUseCase // Внедряем финансовый сценарий для списания денег
)(using ec: ExecutionContext) {

  // Executes the scenario of releasing a gate and processing billing deductions
  def execute(command: ReleaseGateCommand): Future[Either[String, ReleaseGateResponse]] = {

    // 1. Поиск ворот в PostgreSQL
    gateRepository.findById(command.gateId).flatMap {
      case None =>
        Future.successful(Left(s"Warehouse gate with ID '${command.gateId.value}' not found"))

      case Some(gate) =>
        if (gate.status != GateStatus.Occupied) {
          Future.successful(Left(s"Warehouse gate '${gate.gateNumber}' is not currently occupied"))
        } else {

          // 2. Ищем активное бронирование, привязанное к этим воротам в статусе "IN_PROGRESS"
          bookingRepository.findActiveByGateId(gate.id).flatMap {
            case None =>
              // Защитный механизм: переводим ворота в Available, если бронь не найдена сбоем IoT
              val fixedGate = gate.copy(status = GateStatus.Available, updatedAt = Instant.now())
              gateRepository.update(fixedGate).map { _ =>
                Left(s"Gate released, but no active booking found in 'IN_PROGRESS' status for tracking history.")
              }

            case Some(booking) =>
              // 3. Загружаем Хаб по hubId из ворот, чтобы узнать компанию-владельца
              hubRepository.findById(gate.hubId).flatMap {
                case None =>
                  Future.successful(Left(s"Hub with ID '${gate.hubId.value}' not found for company billing validation"))

                case Some(hub) =>
                  val now = Instant.now()

                  // 4. Вычисляем финансовые показатели аренды ворот
                  val arrivalTime = booking.actualArrivalTime.getOrElse(booking.createdAt)
                  val totalHours = Math.max(1L, java.time.temporal.ChronoUnit.HOURS.between(arrivalTime, now))
                  val totalCost = gate.hourlyRate * BigDecimal(totalHours)

                  // 5. Формируем иммутабельные слепки для обновления инфраструктуры
                  val releasedGate = gate.copy(status = GateStatus.Available, updatedAt = now)
                  val completedBooking = booking.copy(
                    status = GateBookingStatus.Completed,
                    actualDepartureTime = Some(now),
                    updatedAt = now
                  )

                  // 6. Формируем финансовую команду: компания берётся из найденного ХАБА!
                  val financialCommand = DeductFundsCommand(
                    companyId = hub.companyId,
                    amount = totalCost,
                    category = "GATE_RENTAL",
                    sourceId = Some(booking.id.value)
                  )

                  // 7. Сохраняем изменения и запускаем списание денег
                  // КРИТИЧЕСКИ ВАЖНО: Явно аннотируем тип возвращаемого значения для for-comprehension
                  (for {
                    _ <- gateRepository.update(releasedGate)
                    _ <- bookingRepository.update(completedBooking)
                    billingResult <- deductFundsUseCase.execute(financialCommand)
                  //} yield billingResult.map(_ => ReleaseGateResponse(gate.id.value, "COMPLETED"))): Future[Either[String, ReleaseGateResponse]]
                  } yield billingResult.map(_ => ReleaseGateResponse(gate.id.value, "COMPLETED", now))): Future[Either[String, ReleaseGateResponse]]
              }
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class ReleaseGateResponse(
  gateId: String,
  status: String,
  updatedAt: Instant
)

