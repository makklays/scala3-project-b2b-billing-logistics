package com.techmatrix18.hubs.application.in

/**
 * Command to update Hub.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class UpdateHubCommand(
  hubId: HubId,
  title: String,
  description: Option[String],
  addressLine: String,
  postalCode: String
)

