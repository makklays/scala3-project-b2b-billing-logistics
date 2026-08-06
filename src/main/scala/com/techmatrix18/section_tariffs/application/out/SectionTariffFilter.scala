package com.techmatrix18.section_tariffs.application.out

import com.techmatrix18.section_tariffs.domain.{SectionTariff, SectionTariffId}
import com.techmatrix18.hub_sections.domain.HubSectionId
import java.time.Instant
import scala.concurrent.Future

/**
 * Filter criteria object for flexible tariff queries.
 * Позволяет находить активные контракты для конкретных клиентов и временных окон.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class SectionTariffFilter(
  hubSectionId: Option[HubSectionId] = None,
  clientName: Option[String] = None,
  activeAt: Option[Instant] = None             // Для проверки: действует ли тариф в указанный момент времени
)

