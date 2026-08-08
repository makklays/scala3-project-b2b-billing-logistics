package com.techmatrix18.outbox.application.out

import com.techmatrix18.outbox.domain.{OutboxEvent, OutboxEventId, OutboxStatus}
import scala.concurrent.Future

/**
 * OutboxRepository - Outbound Driven Port для надежного сохранения и выборки
 * системных событий бизнес-логики.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

trait OutboxRepository {

  /**
   * Сохраняет событие в таблицу outbox_events.
   * Важно: Вызывается в рамках той же ACID-транзакции PostgreSQL,
   * что и основная бизнес-логика (например, создание пользователя).
   */
  def save(event: OutboxEvent): Future[Unit]

  /**
   * Выбирает пакет (batch) самых старых неотправленных сообщений со статусом 'PENDING'.
   * Результат сортируется по возрастанию (ASC) даты created_at, чтобы соблюсти
   * хронологический порядок отправки сообщений в брокер (Message Ordering).
   *
   * @param limit Ограничение на количество записей в рамках одного чанка (пакета)
   */
  def fetchPendingEvents(limit: Int): Future[List[OutboxEvent]]

  // Обновляет статус конкретного события (например, перевод в PROCESSED или FAILED)
  def updateStatus(id: OutboxEventId, status: OutboxStatus): Future[Unit]

  // Пакетное обновление статуса для успешно доставленных сообщений.
  // Оптимизирует количество сетевых round-trip запросов к СУБД из шедулера.
  def updateStatuses(ids: List[OutboxEventId], status: OutboxStatus): Future[Unit]
}

