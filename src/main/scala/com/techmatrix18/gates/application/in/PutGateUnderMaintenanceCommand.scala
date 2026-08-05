package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.GateId

/**
 * Command to put a gate under maintenance.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class PutGateUnderMaintenanceCommand(gateId: GateId, reason: String)

