package com.techmatrix18.gates.domain

/**
 * HubStatus
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

enum GateStatus(val code: String) {
  case Available   extends GateStatus("AVAILABLE")   // Ворота свободны, готовы принять фуру по брони
  case Occupied    extends GateStatus("OCCUPIED")    // У ворот прямо сейчас стоит грузовик (идет разгрузка)
  case Maintenance extends GateStatus("MAINTENANCE") // Ворота на техобслуживании (ремонт доклевеллера)
}

