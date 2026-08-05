package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gate_bookings.domain.GateBookingId

/**
 * Command to depart a truck from the gate booking system.
 * Регистрация выезда фуры
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class DepartTruckCommand(bookingId: GateBookingId)

