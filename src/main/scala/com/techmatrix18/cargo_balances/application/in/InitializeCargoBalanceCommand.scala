package com.techmatrix18.cargo_balances.application.in

/**
 * InitializeCargoBalanceCommand
 * Создание ячейки учета
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class InitializeCargoBalanceCommand(
  hubSectionId: String,
  clientName: String
)

