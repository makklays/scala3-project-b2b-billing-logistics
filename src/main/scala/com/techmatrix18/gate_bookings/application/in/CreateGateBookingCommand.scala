package com.techmatrix18.gate_bookings.application.in

import com.techmatrix18.gates.domain.GateId

/**
 * Command to create a new gate booking.
 * Создание брони / Резервирование слота
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class CreateGateBookingCommand(
  gateId: GateId,
  clientName: String,
  truckLicensePlate: String,
  scheduledStartTime: Instant,
  scheduledEndTime: Instant
)

