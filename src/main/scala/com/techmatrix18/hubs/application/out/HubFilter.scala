package com.techmatrix18.hubs.application.out

import com.techmatrix18.hubs.domain.{Hub, HubId}
import scala.concurrent.Future

/**
 * Объявление структуры фильтра для поиска хабов
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 11.08.2026
 */

case class HubFilter(
  title: Option[String] = None,
  status: Option[String] = None,
  companyId: Option[String] = None
)

