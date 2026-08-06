package com.techmatrix18.cargo_transactions.application.in

/**
 *
 */

case class LogCargoTransactionCommand(
  hubSectionId: String,
  gateBookingId: String,
  clientName: String,
  operationType: String,
  palletsDelta: Int
)

