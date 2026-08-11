package com.techmatrix18.billing_transactions.infrastructure.http

import com.techmatrix18.billing_transactions.application.in.*
import play.api.libs.json.{Json, OFormat, Format}
import com.techmatrix18.billing_transactions.domain.{BillingTransaction, BillingTransactionId}
import com.techmatrix18.billing_transactions.domain.BillingTransactionId.*
import com.techmatrix18.companies.domain.CompanyId

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

  given billingTransactionIdFormat: Format[BillingTransactionId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(BillingTransactionId(_)),
      play.api.libs.json.Writes((id: BillingTransactionId) => play.api.libs.json.JsString(id.value))
    )

  given companyIdFormat: Format[CompanyId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(s => s.asInstanceOf[CompanyId]),
      play.api.libs.json.Writes((id: CompanyId) => play.api.libs.json.JsString(id.asInstanceOf[String]))
    )

  // 1. Формат для входящей JSON-команды (Фиксация финансовой проводки)
  given logBillingTransactionCommandFormat: OFormat[LogBillingTransactionCommand] = Json.format[LogBillingTransactionCommand]

  // 2. Формат для исходящего JSON-ответа (Подтверждение записи в Ledger-книгу)
  given logBillingTransactionResponseFormat: OFormat[LogBillingTransactionResponse] = Json.format[LogBillingTransactionResponse]

  // 3. Формат для самого доменного объекта (уберет ошибку компиляции списка)
  given billingTransactionFormat: OFormat[BillingTransaction] = Json.format[BillingTransaction]
}

