package com.techmatrix18.gates.application.in

import com.techmatrix18.gates.domain.WorkingHours

/**
 * Command containing payload to register a new warehouse gate / loading dock.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class CreateGateCommand(
  hubId: String,                  // ID хаба, к которому привязываем ворота (Foreign Key)
  gateNumber: String,             // Коммерческий номер ворот (например, "Dock-14")
  gateType: String,               // Строковой код типа (например, "COLD_STORAGE", "DRY")
  hourlyRate: BigDecimal,         // Стоимость базовой аренды ворот в час
  overtimeHourlyRate: BigDecimal, // Повышенный тариф за овертайм / простой фуры
  workingHours: WorkingHours      // Value Object с часами работы ворот (from, to)
)

