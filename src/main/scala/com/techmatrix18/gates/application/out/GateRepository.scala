package com.techmatrix18.gates.application.out

import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus, GateType}
import com.techmatrix18.hubs.domain.HubId 
import scala.concurrent.Future

/**
 * GateRepository - Outbound Driven Port for database infrastructure interaction.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 05.08.2026
 */

trait GateRepository {

  // Находит конкретные ворота по их строгому идентификатору
  def findById(gateId: GateId): Future[Option[Gate]]

  def create(gate: Gate): Future[GateId]

  def update(gate: Gate): Future[Unit]

  def delete(gateId: GateId): Future[Unit]

  // Универсальный метод для поиска и фильтрации ворот
  // Если передать пустой GateFilter(), вернет все ворота системы (getAllGates)
  def findByFilter(filter: GateFilter): Future[List[Gate]]

}

