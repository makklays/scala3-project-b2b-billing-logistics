package com.techmatrix18.idempotency.application.out

import com.techmatrix18.idempotency.domain.{IdempotencyKey, Idempotency, IdempotencyStatus}
import scala.concurrent.Future

/**
 * IdempotencyRepository - Outbound Driven Port для управления записями идемпотентности.
 * Служит барьером против дублирования транзакций на уровне хранилища данных.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

trait IdempotencyRepository {

  /**
   * Пытается атомарно найти существующую запись или создать новую со статусом 'STARTED'.
   *
   * @return Future[Either[Idempotency, Idempotency]]
   *         - Left(record): Ключ УЖЕ существует в БД (обнаружен дубликат запроса). Возвращает старую запись.
   *         - Right(record): Ключ успешно зарезервирован (первичный запрос). Можно выполнять бизнес-логику.
   */
  def findOrInsert(key: IdempotencyKey, payloadHash: String): Future[Either[Idempotency, Idempotency]]

  // Обновляет статус записи на 'PROCESSING'. Используется при долгих операциях
  // для индикации того, что первый запрос все еще обрабатывается.
  def markAsProcessing(key: IdempotencyKey): Future[Unit]

  // Фиксирует успешное завершение обработки запроса.
  // Кеширует HTTP-код и тело ответа для мгновенной выдачи дубликатам.
  def complete(key: IdempotencyKey, responseCode: Int, responseBody: String): Future[Unit]

  // Переводит запись в статус 'FAILED' или удаляет её, если бизнес-логика упала
  // с системной ошибкой и клиент имеет право повторить этот же запрос заново.
  def markAsFailed(key: IdempotencyKey): Future[Unit]
}

