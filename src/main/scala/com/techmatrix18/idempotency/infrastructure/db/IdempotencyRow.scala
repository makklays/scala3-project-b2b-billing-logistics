package com.techmatrix18.idempotency.infrastructure.db

import com.techmatrix18.idempotency.domain.{Idempotency, IdempotencyKey, IdempotencyStatus}
import java.time.Instant
import anorm.{RowParser, ~, get}

/**
 * IdempotencyRow - Вспомогательная структура строки таблицы idempotency_records для Anorm.
 * Полностью изолирует базу данных от чистых моделей домена.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

case class IdempotencyRow(
  idempotencyKey: String,
  requestPayloadHash: String,
  status: String,
  responseCode: Option[Int],
  responseBody: Option[String],
  createdAt: Instant,
  expiresAt: Instant
) {

  // Трансформация плоской строки базы данных в чистый DDD Агрегат
  def toDomain: Idempotency = Idempotency(
    key = IdempotencyKey(idempotencyKey),
    requestPayloadHash = requestPayloadHash,
    status = IdempotencyStatus.values.find(_.code == status.trim.toUpperCase).getOrElse(IdempotencyStatus.Started),
    responseCode = responseCode,
    responseBody = responseBody,
    createdAt = createdAt,
    expiresAt = expiresAt
  )
}

object IdempotencyRow {

  // Нативный Scala 3 Anorm-парсер для автоматической сборки структуры IdempotencyRow из SQL-ответа
  val parser: RowParser[IdempotencyRow] = {
    get[String]("idempotency_key") ~
    get[String]("request_payload_hash") ~
    get[String]("status") ~
    get[Option[Int]]("response_code") ~
    get[Option[String]]("response_body") ~
    get[Instant]("created_at") ~
    get[Instant]("expires_at") map {
      case idempotencyKey ~ requestPayloadHash ~ status ~ responseCode ~ responseBody ~ createdAt ~ expiresAt =>
        IdempotencyRow(idempotencyKey, requestPayloadHash, status, responseCode, responseBody, createdAt, expiresAt)
    }
  }

  // Сборка плоской строки БД из иммутабельного доменного агрегата перед записью
  def fromDomain(record: Idempotency): IdempotencyRow = IdempotencyRow(
    idempotencyKey = record.key.raw,
    requestPayloadHash = record.requestPayloadHash,
    status = record.status.code,
    responseCode = record.responseCode,
    responseBody = record.responseBody,
    createdAt = record.createdAt,
    expiresAt = record.expiresAt
  )
}

