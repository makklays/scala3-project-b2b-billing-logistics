package com.techmatrix18.billing_transactions.infrastructure.http

import com.techmatrix18.billing_transactions.domain.BillingTransaction
import com.techmatrix18.billing_transactions.application.in.*
import play.api.libs.json.{Json, OFormat}

/**
 * BillingTransactionJsonFormats - Infrastructure driving adapter for Play JSON marshalling.
 * Provides implicit formats for financial ledger commands and response DTOs using Scala 3 'given' syntax.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

object BillingTransactionJsonFormats {

  // 1. Формат для входящей JSON-команды (Фиксация финансовой проводки)
  given logBillingTransactionCommandFormat: OFormat[LogBillingTransactionCommand] = Json.format[LogBillingTransactionCommand]

  // 2. Формат для исходящего JSON-ответа (Подтверждение записи в Ledger-книгу)
  given logBillingTransactionResponseFormat: OFormat[LogBillingTransactionResponse] = Json.format[LogBillingTransactionResponse]

  // 3. Формат для самого доменного объекта (уберет ошибку компиляции списка)
  given billingTransactionFormat: OFormat[BillingTransaction] = Json.format[BillingTransaction]
}

