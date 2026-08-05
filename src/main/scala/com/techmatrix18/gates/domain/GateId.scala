package com.techmatrix18.gates.domain

import java.util.UUID

/**
 * GateId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

opaque type GateId = String

object GateId {

  // Конструктор: создание GateId из обычной строки (UUID)
  def apply(value: String): GateId = value

  // Генерация случайного ID (удобно для создания новых компаний)
  def generate(): GateId = UUID.randomUUID().toString

  // Метод расширения (Extension Method), позволяющий достать сырой UUID,
  // когда это потребуется инфраструктурному слою (например, для записи в Postgres)
  extension (GateId: GateId) {
    def value: String = GateId
  }
}

