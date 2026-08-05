package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.GateId
import com.techmatrix18.gates.domain.WorkingHours

/**
 * Command to update gate configuration.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class UpdateGateConfigurationCommand(
  gateId: GateId,
  gateNumber: String,
  workingHours: WorkingHours
)

