package com.techmatrix18.billing_transactions.application.out

import java.util.UUID
import com.techmatrix18.companies.domain.CompanyId

/**
 * Filter criteria object for historical financial ledger queries.
 * Используется финансовым отделом для построения отчетов и выгрузки актов сверки.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class BillingTransactionFilter(
  companyId: Option[CompanyId] = None,
  category: Option[String] = None,
  currency: Option[String] = None,
  sourceId: Option[UUID] = None,
  fromDate: Option[Instant] = None,
  toDate: Option[Instant] = None
)

