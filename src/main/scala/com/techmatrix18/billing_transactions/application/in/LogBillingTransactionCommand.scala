package com.techmatrix18.billing_transactions.application.in

import java.util.UUID

/**
 * LogBillingTransactionCommand
 * Регистрация финансовой транзакции
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class LogBillingTransactionCommand(
  companyId: String,
  amount: BigDecimal,
  currency: String,
  category: String,
  sourceId: Option[UUID],
  description: Option[String]
)

