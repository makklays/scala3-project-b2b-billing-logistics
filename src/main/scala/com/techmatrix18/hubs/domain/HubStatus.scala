package com.techmatrix18.hubs.domain

/**
 * HubStatus
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

enum HubStatus(val code: String) {
  case Active      extends HubStatus("ACTIVE")      // Хаб функционирует, ворота открыты для бронирования
  case Inactive    extends HubStatus("INACTIVE")    // Хаб временно отключен (например, за неуплату компании)
  case Suspended   extends HubStatus("SUSPENDED")   // Хаб полностью выведен из эксплуатации (мягкое удаление)
  case Maintenance extends HubStatus("MAINTENANCE") // Хаб на ремонте, завершает старые фуры, новые не пускает
}

