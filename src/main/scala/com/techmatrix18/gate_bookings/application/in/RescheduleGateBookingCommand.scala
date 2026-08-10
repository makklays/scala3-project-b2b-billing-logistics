package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gate_bookings.domain.GateBookingId
import java.time.Instant

/**
 * Command to reschedule a gate booking.
 * Перенос времени брони
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class RescheduleGateBookingCommand(
  bookingId: GateBookingId,
  newStartTime: Instant,
  newEndTime: Instant
)

