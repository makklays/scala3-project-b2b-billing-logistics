package com.techmatrix18.users.domain

/**
 * Доступные роли сотрудников в системе
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

enum UserRole(val code: String) {
  case PlatformAdmin   extends UserRole("ADMIN")
  case CompanyManager  extends UserRole("COMPANY_MANAGER")
  case HubDispatcher   extends UserRole("DISPATCHER")
  case Driver          extends UserRole("DRIVER")
  case StandardUser    extends UserRole("USER")
}

