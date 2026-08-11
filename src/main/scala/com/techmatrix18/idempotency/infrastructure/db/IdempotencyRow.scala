package com.techmatrix18.idempotency.infrastructure.db

import com.techmatrix18.idempotency.domain.{Idempotency, IdempotencyKey, IdempotencyStatus}
import java.time.Instant
import anorm.{RowParser, ~}
import anorm.SqlParser // Добавляем импорт самого объекта парсеров

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
    SqlParser.get[String]("idempotency_key") ~
    SqlParser.get[String]("request_payload_hash") ~
    SqlParser.get[String]("status") ~
    SqlParser.get[Option[Int]]("response_code") ~
    SqlParser.get[Option[String]]("response_body") ~
    SqlParser.get[java.time.Instant]("created_at") ~
    SqlParser.get[java.time.Instant]("expires_at") map {
      // Все переменные уникальны и идут строго по порядку полей вашего класса!
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

