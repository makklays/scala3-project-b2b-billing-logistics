package com.techmatrix18.section_tariffs.domain

import java.util.UUID

/**
 * SectionTariffId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

opaque type SectionTariffId = String

object SectionTariffId {

  // Конструктор: создание HubId из обычной строки (UUID)
  def apply(value: String): SectionTariffId = value

  // Генерация случайного ID (удобно для создания новых компаний)
  def generate(): SectionTariffId = UUID.randomUUID().toString

  // Метод расширения (Extension Method), позволяющий достать сырой UUID,
  // когда это потребуется инфраструктурному слою (например, для записи в Postgres)
  extension (sectionTariffId: SectionTariffId) {
    def value: String = sectionTariffId
  }
}