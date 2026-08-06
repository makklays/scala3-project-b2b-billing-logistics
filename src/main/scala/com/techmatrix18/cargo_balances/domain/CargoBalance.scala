package com.techmatrix18.cargo_balances.domain

import com.techmatrix18.hub_sections.domain.HubSectionId
import java.time.Instant

/**
 * CargoBalance Aggregate Root - Управляет текущими товарными остатками (балансом грузов)
 * B2B-клиентов внутри конкретных складских секций. Напрямую используется финтех-ядром
 * для начисления ежедневной/ежечасной платы за хранение палет.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class CargoBalance(
  id: CargoBalanceId,
  hubSectionId: HubSectionId,  // Идентификатор складской секции, где лежит груз
  clientName: String,          // Название компании-владельца груза (например, "Inditex")
  currentPallets: Int,         // Зафиксирован строго штучный тип данных для палет

  // System audit (managed by the system, not by the user)
  createdAt: Instant,
  updatedAt: Instant
) {

  // Чистое доменное правило: пуста ли данная ячейка хранения для этого клиента?
  def isEmpty: Boolean = currentPallets <= 0

  // Чистое доменное правило: иммутабельное прибавление палет при разгрузке фуры
  def credit(amount: Int): CargoBalance = {
    require(amount > 0, "Amount to credit must be strictly positive")
    this.copy(
      currentPallets = this.currentPallets + amount,
      updatedAt = Instant.now()
    )
  }

  // Чистое доменное правило: иммутабельное списание палет при погрузке и вывозе со склада
  def debit(amount: Int): Either[String, CargoBalance] = {
    require(amount > 0, "Amount to debit must be strictly positive")
    if (this.currentPallets < amount) {
      Left(s"Insufficient cargo balance. Available: ${this.currentPallets}, requested: $amount")
    } else {
      Right(this.copy(
        currentPallets = this.currentPallets - amount,
        updatedAt = Instant.now()
      ))
    }
  }
}

