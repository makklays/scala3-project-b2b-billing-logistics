package com.techmatrix18.outbox.infrastructure.db

import com.techmatrix18.outbox.domain.{OutboxEvent, OutboxEventId, OutboxStatus}
import java.time.Instant
import java.util.UUID
import anorm.{RowParser, ~, get}

/**
 * OutboxRow - Вспомогательная структура строки таблицы outbox_events для Anorm.
 * Идеально синхронизирована с типами данных PostgreSQL миграции V11.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

case class OutboxRow(
  id: UUID,
  aggregateType: String,
  aggregateId: UUID,
  eventType: String,
  payload: String,
  status: String,
  createdAt: Instant,
  processedAt: Option[Instant]
) {

  // Трансформация плоской строки базы данных в чистый DDD Агрегат
  def toDomain: OutboxEvent = OutboxEvent(
    id = OutboxEventId(id),
    aggregateType = aggregateType,
    aggregateId = aggregateId,
    eventType = eventType,
    payload = payload,
    status = OutboxStatus.values.find(_.code == status.trim.toUpperCase).getOrElse(OutboxStatus.Pending),
    createdAt = createdAt,
    processedAt = processedAt
  )
}

object OutboxRow {

  // Нативный Scala 3 Anorm-парсер для автоматической сборки структуры OutboxRow из SQL-ответа.
  // Извлекает UUID и Instant напрямую, задействуя встроенные конвертеры Anorm.
  val parser: RowParser[OutboxRow] = {
    get[UUID]("id") ~
    get[String]("aggregate_type") ~
    get[UUID]("aggregate_id") ~
    get[String]("event_type") ~
    get[String]("payload") ~
    get[String]("status") ~
    get[Instant]("created_at") ~
    get[Option[Instant]]("processed_at") map {
      case id ~ aggregateType ~ aggregateId ~ eventType ~ payload ~ status ~ createdAt ~ processedAt =>
        OutboxRow(id, aggregateType, aggregateId, eventType, payload, status, createdAt, processedAt)
    }
  }

  // Сборка плоской строки БД из иммутабельного доменного агрегата перед записью
  def fromDomain(event: OutboxEvent): OutboxRow = OutboxRow(
    id = event.id.raw,
    aggregateType = event.aggregateType,
    aggregateId = event.aggregateId,
    eventType = event.eventType,
    payload = event.payload,
    status = event.status.code,
    createdAt = event.createdAt,
    processedAt = event.processedAt
  )
}

