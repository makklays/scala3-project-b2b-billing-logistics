package com.techmatrix18.billing_transactions.domain

import com.techmatrix18.companies.domain.CompanyId
import java.time.Instant
import java.util.UUID

/**
 * BillingTransaction Aggregate Root - Неизменяемая строка Ledger-журнала финансовых проводок.
 * Хранит перманентный исторический след каждого списания и пополнения для финтех-аудита.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 06.08.2026
 */

case class BillingTransaction(
  id: BillingTransactionId,
  companyId: CompanyId,
  amount: BigDecimal,           // Положительное число для пополнений (DEPOSIT), отрицательное для списаний (DEDUCT)
  currency: String,             // Код валюты, например, "EUR", "USD"
  category: String,             // Категория списания (GATE_RENTAL, GATE_NOSHOW_PENALTY, PROFILE_SUBSCRIPTION)
  sourceId: Option[UUID],       // Обернуто в Option для гибкой привязки к логистическим сущностям
  description: Option[String],  // Обернуто в Option для необязательных текстовых заметок

  // System audit (managed by the system, append-only, NO updatedAt field)
  createdAt: Instant
)

