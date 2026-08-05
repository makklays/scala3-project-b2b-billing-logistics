package com.techmatrix18.gate_bookings.domain

import com.techmatrix18.gates.domain.GateId
import java.util.UUID
import java.time.Instant

/**
 * GateBooking Aggregate Root - управляет резервированием доков и финансовым следом фур.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class GateBooking(
  id: GateBookingId,
  gateId: GateId,

  clientName: String,
  truckLicensePlate: String,

  scheduledStartTime: Instant,
  scheduledEndTime: Instant,

  actualArrivalTime: Option[Instant],
  actualDepartureTime: Option[Instant],

  status: GateBookingStatus,

  // System audit (managed by the system, not by the user)
  createdAt: Instant,
  updatedAt: Instant
) {

  // Чистое доменное правило: начался ли процесс разгрузки/погрузки фактически?
  def isCurrentlyProcessing: Boolean = actualArrivalTime.isDefined && actualDepartureTime.isEmpty
}

