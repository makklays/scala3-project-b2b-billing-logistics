package com.techmatrix18.gate_bookings.domain

import java.util.UUID

/**
 * GateBookingId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

opaque type GateBookingId = String

object GateBookingId {

  // Конструктор: создание GateBookingId из обычной строки (UUID)
  def apply(value: String): GateBookingId = value

  // Генерация случайного ID (удобно для создания новых компаний)
  def generate(): GateBookingId = UUID.randomUUID().toString

  // Метод расширения (Extension Method), позволяющий достать сырой UUID,
  // когда это потребуется инфраструктурному слою (например, для записи в Postgres)
  extension (gateBookingId: GateBookingId) {
    def value: String = gateBookingId
  }
}
