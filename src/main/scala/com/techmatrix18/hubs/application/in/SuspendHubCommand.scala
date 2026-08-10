package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.HubId

/**
 * Command to suspend Hub. Status - Suspend.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class SuspendHubCommand(
  hubId: HubId,
  reason: String
)

