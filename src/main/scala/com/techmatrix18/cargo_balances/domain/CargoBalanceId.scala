package com.techmatrix18.cargo_balances.domain

import java.util.UUID

/**
 * CompanyId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

// Объявляем сам тип данных как непрозрачный алиас для String
opaque type CargoBalanceId = String

object CargoBalanceId {

  // Конструктор: создание CompanyId из обычного UUID
  def apply(value: String): CargoBalanceId = value

  // Генерация случайного ID (удобно для создания новых компаний)
  def generate(): CargoBalanceId = UUID.randomUUID().toString

  // Метод расширения (Extension Method), позволяющий достать сырой UUID,
  // когда это потребуется инфраструктурному слою (например, для записи в Postgres)
  extension (cargoBalanceId: CargoBalanceId) {
    def value: String = cargoBalanceId
  }
}

