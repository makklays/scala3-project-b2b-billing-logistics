package com.techmatrix18.cargo_balances.application.out

import com.techmatrix18.cargo_balances.domain.{CargoBalance, CargoBalanceId}
import com.techmatrix18.hub_sections.domain.HubSectionId
import scala.concurrent.Future

/**
 * Filter criteria object for flexible cargo balance queries.
 * Предотвращает дублирование методов и раздувание инфраструктурного слоя.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class CargoBalanceFilter(
  hubSectionId: Option[HubSectionId] = None,
  clientName: Option[String] = None,
  hasPallets: Option[Boolean] = None // true - только заполненные ячейки, false - пустые
)

