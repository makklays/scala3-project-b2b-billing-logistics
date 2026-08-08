package com.techmatrix18.idempotency.domain

import com.techmatrix18.idempotency.domain.IdempotencyStatus

/**
 * IdempotencyRecord Aggregate Root - Сетевой аудит-заслон на входе в систему.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

case class Idempotency(
  key: IdempotencyKey,
  requestPayloadHash: String,   // SHA-256 хэш тела запроса для защиты от подмены параметров
  status: IdempotencyStatus,
  responseCode: Option[Int],     // Заполняется только при переходе в Completed или Failed
  responseBody: Option[String],   // Закешированная JSON-строка ответа для мгновенного возврата дубликатам
  createdAt: Instant,
  expiresAt: Instant
) {

  // Бизнес-правило: Проверка, истек ли срок действия ключа в базе данных (24 часа)
  def isExpired(now: Instant): Boolean = now.isAfter(expiresAt)

  // Бизнес-правило: Можно ли повторно использовать или перезаписать этот ключ
  def canBeProcessed: Boolean = status == IdempotencyStatus.Failed
}

