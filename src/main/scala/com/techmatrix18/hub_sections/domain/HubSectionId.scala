package com.techmatrix18.hub_sections.domain

import java.util.UUID

/**
 * HubSectionId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

opaque type HubSectionId = String

object HubSectionId {

  // Конструктор: создание HubSectionId из обычной строки (UUID)
  def apply(value: String): HubSectionId = value

  // Генерация случайного ID (удобно для создания новых компаний)
  def generate(): HubSectionId = UUID.randomUUID().toString

  // Метод расширения (Extension Method), позволяющий достать сырой UUID,
  // когда это потребуется инфраструктурному слою (например, для записи в Postgres)
  extension (hubSectionId: HubSectionId) {
    def value: String = hubSectionId
  }
}

