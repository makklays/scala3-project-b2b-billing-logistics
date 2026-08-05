package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus}
import com.techmatrix18.gates.application.out.{GateRepository, GateBookingRepository}
import com.techmatrix18.companies.application.in.{DeductFundsUseCase, DeductFundsCommand}
import com.techmatrix18.companies.domain.CompanyId
import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}

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
              // Защитный механизм: переводим ворота в Available, даже если бронь не найдена сбоем IoT
              val fixedGate = gate.copy(status = GateStatus.Available, updatedAt = Instant.now())
              gateRepository.update(fixedGate).map { _ =>
                Left(s"Gate released, but no active booking found in 'IN_PROGRESS' status for tracking history.")
              }

            case Some(booking) =>
              val now = Instant.now()

              // 3. Вычисляем финансовые показатели аренды (биллинг)
              val arrivalTime = booking.actualArrivalTime.getOrElse(booking.createdAt)
              val totalHours = Math.max(1L, ChronoUnit.HOURS.between(arrivalTime, now)) // минимум 1 час тарификации
              val totalCost = gate.hourlyRate * BigDecimal(totalHours)

              // 4. Формируем иммутабельные слепки для обновления инфраструктуры
              val releasedGate = gate.copy(status = GateStatus.Available, updatedAt = now)
              val completedBooking = booking.copy(
                status = "COMPLETED",
                actualDepartureTime = Some(now),
                updatedAt = now
              )

              // 5. Запускаем финансовую транзакцию списания денег через смежный домен
              val financialCommand = DeductFundsCommand(
                companyId = booking.companyId,
                amount = totalCost,
                category = "GATE_RENTAL",
                sourceId = Some(booking.id) // Передаем UUID брони как полиморфный источник финансового следа
              )

              deductFundsUseCase.execute(financialCommand).flatMap {
                case Left(financeError) =>
                  // Если биллинг заблокирован (например, аккаунт заблокирован), мы все равно фиксируем выезд,
                  // но логируем критическую ошибку задолженности
                  for {
                    _ <- gateRepository.update(releasedGate)
                    _ <- bookingRepository.update(completedBooking)
                  } yield Left(s"Gate released, but billing deduction failed: $financeError")

                case Right(_) =>
                  // Успешный финтех-цикл: деньги списаны, ворота свободны, бронь закрыта
                  for {
                    _ <- gateRepository.update(releasedGate)
                    _ <- bookingRepository.update(completedBooking)
                  } yield {
                    Right(ReleaseGateResponse(
                      gateId = releasedGate.id.value,
                      status = "AVAILABLE",
                      updatedAt = now
                    ))
                  }
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

