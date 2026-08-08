package com.techmatrix18.idempotency.infrastructure.db

import com.techmatrix18.idempotency.application.out.IdempotencyRepository
import com.techmatrix18.idempotency.domain.{IdempotencyRecord, IdempotencyKey, IdempotencyStatus}
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * PostgresIdempotencyRepository - Инфраструктурная реализация сетевого заслона на Anorm SQL.
 * Гарантирует потокобезопасность обработки ключей за счет транзакционного контроля СУБД.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

@Singleton
class PostgresIdempotencyRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends IdempotencyRepository {

  override def findOrInsert(key: IdempotencyKey, payloadHash: String): Future[Either[IdempotencyRecord, IdempotencyRecord]] = Future {
    db.withTransaction { implicit connection =>
      // 1. Пытаемся найти существующий ключ в таблице
      val existingRecordOpt =
        SQL"""
          SELECT idempotency_key, request_payload_hash, status, response_code, response_body, created_at, expires_at
          FROM idempotency_records
          WHERE idempotency_key = ${key.raw}
        """.as(IdempotencyRow.parser.singleOptional).map(_.toDomain)

      existingRecordOpt match {
        // Ключ найден — это сетевой дубликат (или повторный запрос)
        case Some(existingRecord) =>
          Left(existingRecord)

        // Ключ не найден — это первичный уникальный запрос, атомарно резервируем его
        case None =>
          val now = Instant.now()
          val expiresAt = now.plus(24, ChronoUnit.HOURS) // Время жизни ключа согласно миграции — 24 часа

          SQL"""
              INSERT INTO idempotency_records (
                idempotency_key, request_payload_hash, status, response_code, response_body, created_at, expires_at
              ) VALUES (
                ${key.raw}, ${payloadHash}, ${IdempotencyStatus.Started.code}, NULL, NULL, ${now}, ${expiresAt}
              )
            """.executeInsert()

          val newRecord = IdempotencyRecord(
            key = key,
            requestPayloadHash = payloadHash,
            status = IdempotencyStatus.Started,
            responseCode = None,
            responseBody = None,
            createdAt = now,
            expiresAt = expiresAt
          )

          Right(newRecord)
      }
    }
  }

  override def markAsProcessing(key: IdempotencyKey): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE idempotency_records
          SET status = ${IdempotencyStatus.Processing.code}
          WHERE idempotency_key = ${key.raw}
        """.executeUpdate()
      ()
    }
  }

  override def complete(key: IdempotencyKey, responseCode: Int, responseBody: String): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE idempotency_records
          SET status = ${IdempotencyStatus.Completed.code},
              response_code = ${responseCode},
              response_body = ${responseBody}
          WHERE idempotency_key = ${key.raw}
        """.executeUpdate()
      ()
    }
  }

  override def markAsFailed(key: IdempotencyKey): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      // При системной ошибке переводим статус в FAILED, позволяя клиенту безопасно повторить попытку запроса
      SQL"""
          UPDATE idempotency_records
          SET status = ${IdempotencyStatus.Failed.code}
          WHERE idempotency_key = ${key.raw}
        """.executeUpdate()
      ()
    }
  }
}

