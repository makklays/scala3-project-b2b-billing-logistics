package com.techmatrix18.billing_transactions.domain

import java.util.UUID

/**
 * BillingTransactionId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

// Объявляем сам тип данных как непрозрачный алиас для String
opaque type BillingTransactionId = String

object BillingTransactionId {

  // Конструктор: создание CompanyId из обычного UUID
  def apply(value: String): BillingTransactionId = value

  // Генерация случайного ID (удобно для создания новых компаний)
  def generate(): BillingTransactionId = UUID.randomUUID().toString

  // Метод расширения (Extension Method), позволяющий достать сырой UUID,
  // когда это потребуется инфраструктурному слою (например, для записи в Postgres)
  extension (billingTransactionId: BillingTransactionId) {
    def value: String = billingTransactionId
  }
}

