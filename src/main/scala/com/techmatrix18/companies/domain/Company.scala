package com.techmatrix18.companies.domain

import java.util.UUID
import java.time.Instant

/**
 * Company
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */
case class Company(
  id: CompanyId,
  title: String,
  taxNumber: String,
  balance: BigDecimal,
  status: CompanyStatus,
  createdAt: Instant,
  updatedAt: Instant
)

