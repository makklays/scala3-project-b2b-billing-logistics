package com.techmatrix18.gates.application.in

import com.techmatrix18.gate_bookings.domain.GateBookingId
import com.techmatrix18.gates.domain.GateId
import java.util.UUID

/**
 * Command to occupy a gate in the system.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class OccupyGateCommand(gateId: GateId, bookingId: GateBookingId)

