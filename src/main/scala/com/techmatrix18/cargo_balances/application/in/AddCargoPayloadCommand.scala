package com.techmatrix18.cargo_balances.application.in

/**
 * AddCargoPayloadCommand
 * Прием груза на склад
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class AddCargoPayloadCommand(
  balanceId: CargoBalanceId,
  palletsAdded: Int
)

