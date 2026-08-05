package com.techmatrix18.gate_bookings.domain

/**
 * GateBookingStatus - определяет жизненный цикл бронирования погрузочного дока.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 06.08.2026
 */

enum GateBookingStatus(val code: String) {                        // Используем val code: String для связи с PostgreSQL
  case Scheduled  extends GateBookingStatus("SCHEDULED")    // Бронь создана и подтверждена, ожидаем фуру
  case InProgress extends GateBookingStatus("IN_PROGRESS")  // Фура физически заехала в док, идет биллинг
  case Completed  extends GateBookingStatus("COMPLETED")    // Разгрузка завершена, деньги списаны, фура уехала
  case Canceled   extends GateBookingStatus("CANCELED")     // Бронь отменена клиентом или администратором
  case NoShow     extends GateBookingStatus("NO_SHOW")      // Фура не приехала в назначенное временное окно
}

