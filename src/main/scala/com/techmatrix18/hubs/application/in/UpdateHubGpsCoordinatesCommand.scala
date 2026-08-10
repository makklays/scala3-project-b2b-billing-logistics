package com.techmatrix18.hubs.application.in

import com.techmatrix18.hubs.domain.HubId

/**
 * Command to update Hub GPS Coordinates.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class UpdateHubGpsCoordinatesCommand(
  hubId: HubId,
  latitude: BigDecimal,
  longitude: BigDecimal
)

