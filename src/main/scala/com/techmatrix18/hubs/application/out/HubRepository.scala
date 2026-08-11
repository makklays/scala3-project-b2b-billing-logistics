package com.techmatrix18.hubs.application.out

import com.techmatrix18.hubs.domain.{Hub, HubId} // Добавлен импорт доменных сущностей
import scala.concurrent.Future

/**
 * HubRepository (Outbound Driven Port)
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

trait HubRepository {

  def findById(id: HubId): Future[Option[Hub]]

  def findAll(limit: Int, offset: Int): Future[List[Hub]]

  def update(hub: Hub): Future[Unit]

  def create(hub: Hub): Future[HubId]

  def delete(id: HubId): Future[Boolean]

  def findByFilter(filter: HubFilter): Future[List[Hub]]

  def findPage(page: Int): Future[List[Hub]]
}

