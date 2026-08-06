package com.techmatrix18.cargo_transactions.domain

import com.techmatrix18.hub_sections.domain.HubSectionId
import com.techmatrix18.gate_bookings.domain.GateBookingId 
import java.time.Instant

/**
 * CargoTransaction Aggregate Root - Неизменяемая строка Ledger-книги товарного учета.
 * Хранит перманентный исторический след каждого перемещения палет для аудита.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 06.08.2026
 */

case class CargoTransaction(
  id: CargoTransactionId,
  hubSectionId: HubSectionId,    // Где физически размещен груз
  gateBookingId: GateBookingId,  // На основании какой брони/фуры произошло движение

  clientName: String,            // Владелец груза (например, "Inditex")
  operationType: OperationType,  // Направление операции (SUPPLY / DISPATCH)
  palletsDelta: Int,             // Количество перемещенных палет (всегда строго положительное число)

  // System audit (managed by the system, not by the user)
  createdAt: Instant
)

