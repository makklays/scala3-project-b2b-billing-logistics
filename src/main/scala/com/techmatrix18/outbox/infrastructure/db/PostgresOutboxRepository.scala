package com.techmatrix18.outbox.infrastructure.db

import com.techmatrix18.outbox.application.out.OutboxRepository
import com.techmatrix18.outbox.domain.{OutboxEvent, OutboxEventId, OutboxStatus}
import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * PostgresOutboxRepository - Высокопроизводительная реализация паттерна Transactional Outbox.
 * Опирается на составной индекс (status, created_at) для выборки без Full Table Scan.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

@Singleton
class PostgresOutboxRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends OutboxRepository {

  override def save(event: OutboxEvent): Future[Unit] = Future {
    val row = OutboxRow.fromDomain(event)
    db.withConnection { implicit connection =>
      SQL"""
          INSERT INTO outbox_events (
            id, aggregate_type, aggregate_id, event_type, payload, status, created_at, processed_at
          ) VALUES (
            ${row.id}, ${row.aggregateType}, ${row.aggregateId}, ${row.eventType}, ${row.payload}, ${row.status}, ${row.createdAt}, ${row.processedAt}
          )
        """.executeInsert()
      ()
    }
  }

  override def fetchPendingEvents(limit: Int): Future[List[OutboxEvent]] = Future {
    db.withConnection { implicit connection =>
      // Используем сортировку по возрастанию времени для строгого соблюдения хронологии (FIFO)
      SQL"""
          SELECT id, aggregate_type, aggregate_id, event_type, payload, status, created_at, processed_at
          FROM outbox_events
          WHERE status = ${OutboxStatus.Pending.code}
          ORDER BY created_at ASC
          LIMIT ${limit}
        """.as(OutboxRow.parser.*).map(_.toDomain)
    }
  }

  override def updateStatus(id: OutboxEventId, status: OutboxStatus): Future[Unit] = Future {
    val now = if (status == OutboxStatus.Processed) Some(Instant.now()) else None
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE outbox_events
          SET status = ${status.code},
              processed_at = ${now}
          WHERE id = ${id.raw}
        """.executeUpdate()
      ()
    }
  }

  override def updateStatuses(ids: List[OutboxEventId], status: OutboxStatus): Future[Unit] = Future {
    if (ids.isEmpty) {
      Future.successful(())
    } else {
      val rawUuids: List[UUID] = ids.map(_.raw)
      val now = if (status == OutboxStatus.Processed) Some(Instant.now()) else None

      db.withConnection { implicit connection =>
        // Использование ParameterValue.toParameterValue позволяет Anorm автоматически раскрыть List[UUID] в синтаксис (uuid1, uuid2, ...)
        SQL"""
            UPDATE outbox_events
            SET status = ${status.code},
                processed_at = ${now}
            WHERE id IN (${rawUuids})
          """.executeUpdate()
        ()
      }
    }
  }
}

