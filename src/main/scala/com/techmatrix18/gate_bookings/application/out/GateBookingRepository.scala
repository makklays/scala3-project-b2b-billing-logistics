package com.techmatrix18.gate_bookings.application.out

import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import com.techmatrix18.gates.domain.GateId
import com.techmatrix18.companies.domain.CompanyId
import java.time.Instant
import scala.concurrent.Future

/**
 * Репозиторий для работы с бронированиями ворот.
 * Предоставляет методы для поиска, сохранения и удаления бронирований.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

trait GateBookingRepository {

  // Находит конкретную бронь по её строгому идентификатору
  def findById(bookingId: GateBookingId): Future[Option[GateBooking]]

  // Находит активное бронирование, которое прямо сейчас обрабатывается на воротах.
  // Жизненно необходимо для ReleaseGateUseCase (выезд фуры и остановка таймера биллинга).
  def findActiveByGateId(gateId: GateId): Future[Option[GateBooking]]

  // Сохраняет новое или обновляет существующее бронирование в PostgreSQL
  def create(booking: GateBooking): Future[GateBookingId]

  def update(booking: GateBooking): Future[Unit]

  // Удаляет бронирование (используется крайне редко, обычно в тестовых фикстурах)
  def delete(bookingId: GateBookingId): Future[Unit]

  // Универсальный Senior-метод для поиска и аналитики бронирований.
  // Позволяет делать выборки по временным окнам, конкретным компаниям или номерам фур.
  def findByFilter(filter: GateBookingFilter): Future[List[GateBooking]]

  // Динамически извлекает CompanyId для конкретного бронирования.
  // Позволяет раскрутить логистическую цепочку (GateBooking -> Gate -> Hub -> Company)
  // на уровне эффективного JOIN-запроса в PostgreSQL без денормализации бд
  def getCompanyIdForBooking(bookingId: GateBookingId): Future[Option[CompanyId]]
}

