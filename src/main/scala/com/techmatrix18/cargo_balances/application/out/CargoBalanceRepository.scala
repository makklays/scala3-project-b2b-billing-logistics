package com.techmatrix18.cargo_balances.application.out

import com.techmatrix18.cargo_balances.domain.{CargoBalance, CargoBalanceId}
import com.techmatrix18.hub_sections.domain.HubSectionId
import scala.concurrent.Future

/**
 * CargoBalanceRepository - Outbound Driven Port for warehouse inventory tracking.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

trait CargoBalanceRepository {

  // Находит конкретную ячейку учета остатков по её строгому идентификатору
  def findById(balanceId: CargoBalanceId): Future[Option[CargoBalance]]

  // Инициализирует (создает) новую запись баланса груза в PostgreSQL
  def create(balance: CargoBalance): Future[CargoBalanceId]

  // Обновляет количество палет и метки времени существующего баланса
  def update(balance: CargoBalance): Future[Unit]

  // Физическое или мягкое удаление ячейки учета из инфраструктуры
  // Используется крайне редко (например, при полной перемаркировке зон хаба).
  def delete(balanceId: CargoBalanceId): Future[Unit]

  // Универсальный Senior-метод для поиска и аналитики товарных остатков.
  // Позволяет фоновым биллинг-акторам мгновенно находить все палеты конкретного клиента.
  def findByFilter(filter: CargoBalanceFilter): Future[List[CargoBalance]]
}

