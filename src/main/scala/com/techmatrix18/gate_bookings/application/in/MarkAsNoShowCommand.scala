package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gate_bookings.domain.GateBookingId

/**
 * Command to mark a gate booking as no-show.
 * Фиксация неприбытия
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class MarkAsNoShowCommand(bookingId: GateBookingId)

