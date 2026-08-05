package com.techmatrix18.companies.domain

/**
 * CompanyStatus
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

enum CompanyStatus(val code: String) {
  case Active   extends CompanyStatus("ACTIVE")   // Компания активна, баланс положительный, доступ к хабам открыт
  case Inactive extends CompanyStatus("INACTIVE") // Компания временно заблокирована (например, баланс ушел в минус)
  case Deleted  extends CompanyStatus("DELETED")  // Компания мягко удалена (архивный статус для истории транзакций)
}

