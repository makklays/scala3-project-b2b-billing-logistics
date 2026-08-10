package com.techmatrix18.gate_bookings.application.out

import java.time.Instant
import com.techmatrix18.gates.domain.GateId
import com.techmatrix18.gate_bookings.domain.GateBookingStatus

/**
 * Filter criteria object for flexible gate booking queries.
 * Предотвращает раздувание интерфейса репозитория.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class GateBookingFilter(
  gateId: Option[GateId] = None,
  status: Option[GateBookingStatus] = None,
  truckLicensePlate: Option[String] = None,
  timeWindowStart: Option[Instant] = None,
  timeWindowEnd: Option[Instant] = None
)

