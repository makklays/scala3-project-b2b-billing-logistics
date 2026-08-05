package com.techmatrix18.companies.application.in

import com.techmatrix18.companies.domain.CompanyId

/**
 * Command to delete a company.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class DeleteCompanyCommand(companyId: CompanyId)

