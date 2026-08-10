package com.techmatrix18.cargo_transactions.application.in

/**
 * LogCargoTransactionCommand
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class LogCargoTransactionCommand(
  hubSectionId: String,
  gateBookingId: String,
  clientName: String,
  operationType: String,
  palletsDelta: Int
)

