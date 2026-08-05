package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.GateId

/**
 * Command to delete a gate from the system.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class DeleteGateCommand(gateId: GateId)

