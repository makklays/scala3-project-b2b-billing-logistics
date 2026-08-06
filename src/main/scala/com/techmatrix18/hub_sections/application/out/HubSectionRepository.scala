package com.techmatrix18.hub_sections.application.out

import com.techmatrix18.hub_sections.domain.{HubSection, HubSectionId, SectionType}
import com.techmatrix18.hubs.domain.HubId
import scala.concurrent.Future

/**
 * HubSectionRepository - Outbound Driven Port for database interactions.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

trait HubSectionRepository {

  // Находит конкретную секцию склада по её строгому идентификатору
  def findById(sectionId: HubSectionId): Future[Option[HubSection]]

  // Создает новую секцию хаба в PostgreSQL
  def create(section: HubSection): Future[HubSectionId]

  // Обновляет состояние существующей секции (название, емкость)
  def update(section: HubSection): Future[Unit]

  // Физическое или мягкое удаление секции из инфраструктуры
  def delete(sectionId: HubSectionId): Future[Unit]

  // Универсальный Senior-метод для поиска и аналитики секций хабов.
  // Позволяет делать выборки по конкретному хабу, типу зоны или названию.
  def findByFilter(filter: HubSectionFilter): Future[List[HubSection]]
}

