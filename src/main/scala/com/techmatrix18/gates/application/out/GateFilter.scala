package com.techmatrix18.gates.application.out

import com.techmatrix18.gates.domain.{GateStatus, GateType}
import com.techmatrix18.hubs.domain.HubId

/**
 * Filter criteria object for flexible gate queries.
 * Clean separation from GateRepository interface to prevent file bloating.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 05.08.2026
 */

case class GateFilter(
  hubId: Option[HubId] = None,
  gateType: Option[GateType] = None,
  status: Option[GateStatus] = None,
  gateNumber: Option[String] = None
)

