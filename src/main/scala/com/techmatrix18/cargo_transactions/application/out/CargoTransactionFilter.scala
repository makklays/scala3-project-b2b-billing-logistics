package com.techmatrix18.cargo_transactions.application.out

import com.techmatrix18.cargo_transactions.domain.{CargoTransaction, CargoTransactionId, OperationType}
import com.techmatrix18.hub_sections.domain.HubSectionId
import com.techmatrix18.gate_bookings.domain.GateBookingId
import java.time.Instant
import scala.concurrent.Future

/**
 * Filter criteria object for historical cargo ledger queries.
 * Используется для построения отчетов, сверки актов и финансового аудита.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class CargoTransactionFilter(
  hubSectionId: Option[HubSectionId] = None,
  gateBookingId: Option[GateBookingId] = None,
  clientName: Option[String] = None,
  operationType: Option[OperationType] = None,
  fromDate: Option[Instant] = None,
  toDate: Option[Instant] = None
)

