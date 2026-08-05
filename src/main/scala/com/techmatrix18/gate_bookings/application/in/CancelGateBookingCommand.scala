package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gate_bookings.domain.GateBookingId

/**
 * Command to cancel a gate booking.
 * Отмена бронирования
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class CancelGateBookingCommand(
  bookingId: GateBookingId,
  reason: String
)

