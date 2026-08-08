package com.techmatrix18.outbox.domain

import com.techmatrix18.outbox.domain.OutboxStatus
import java.time.Instant
import java.util.UUID

/**
 * OutboxEvent Aggregate Root - Запись технического события для гарантированной доставки (At-Least-Once)
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

case class OutboxEvent(
  id: OutboxEventId,
  aggregateType: String,       // Тип доменного агрегата (например, 'USER', 'PAYMENT')
  aggregateId: UUID,           // Идентификатор конкретной бизнес-сущности
  eventType: String,           // Название события для брокера ('USER_REGISTERED', 'PAYMENT_SUCCESS')
  payload: String,             // Сериализованный в JSON технический payload события
  status: OutboxStatus,        // Текущий статус отправки
  createdAt: Instant,          // Таймстамп генерации события
  processedAt: Option[Instant] // Таймстамп, когда фоновый релей подтвердил доставку
) {

  // Бизнес-правило: Переводит событие в статус успешной доставки
  def markAsProcessed(now: Instant): OutboxEvent = {
    this.copy(
      status = OutboxStatus.Processed,
      processedAt = Some(now)
    )
  }

  // Бизнес-правило: Помечает событие как упавшее (если исчерпаны лимиты попыток отправки)
  def markAsFailed: OutboxEvent = {
    this.copy(status = OutboxStatus.Failed)
  }
}

