package com.techmatrix18.gates.domain

import com.techmatrix18.hubs.domain.HubId
import java.util.UUID
import java.time.Instant

/**
 * Gate
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class Gate(
  id: GateId,
  hubId: HubId,

  gateNumber: String,              // «Gate 01», «Gate A-12»
  gateType: GateType,              // 'DRY', 'CHILLED', 'FREEZER'
  status: GateStatus,              // 'AVAILABLE', 'OCCUPIED'
  workingHours: WorkingHours,      // строго типизированный Value Object вместо String

  hourlyRate: BigDecimal,          // hourly rate for billing
  overtimeHourlyRate: BigDecimal,  // overtime hourly rate for billing

  // System audit (managed by the system, not by the user)
  createdAt: Instant,
  updatedAt: Instant
) {

  // Чистое доменное правило: открыты ли ворота для бронирования прямо сейчас?
  def isAvailableForBooking: Boolean = status == GateStatus.Available
}

