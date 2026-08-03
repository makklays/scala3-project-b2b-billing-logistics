package com.techmatrix18.hubs.domain

import java.util.UUID

/**
 * HubId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

opaque type HubId = String

object HubId {

  // Конструктор: создание HubId из обычной строки (UUID)
  def apply(value: String): HubId = value

  // Генерация случайного ID (удобно для создания новых компаний)
  def generate(): HubId = UUID.randomUUID().toString

  // Метод расширения (Extension Method), позволяющий достать сырой UUID,
  // когда это потребуется инфраструктурному слою (например, для записи в Postgres)
  extension (hubId: HubId) {
    def value: String = hubId
  }
}

