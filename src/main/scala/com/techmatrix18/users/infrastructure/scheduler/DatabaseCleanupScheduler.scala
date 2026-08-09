package com.techmatrix18.users.infrastructure.scheduler

import javax.inject.{Inject, Singleton}
import org.apache.pekko.actor.ActorSystem
import play.api.db.Database
import play.api.Logger
import anorm.*
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

/**
 * DatabaseCleanupScheduler - Фоновый уборщик устаревших технических данных СУБД.
 * Аналог @Scheduled в Java. Очищает просроченные сессии и ключи идемпотентности.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 09.08.2026
 */

@Singleton
class DatabaseCleanupScheduler @Inject()(
  actorSystem: ActorSystem,
  db: Database
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  // Настройка расписания (Pekko Scheduler):
  // Задача стартует через 10 секунд после запуска сервера, далее повторяется каждый 1 час.
  actorSystem.scheduler.scheduleWithFixedDelay(
    initialDelay = 10.seconds,
    delay = 1.hour
  )(() => runCleanup())

  // Запуск пакетной очистки устаревших записей
  private def runCleanup(): Unit = {
    logger.info("[DB Cleanup] Запуск плановой очистки просроченных данных...")

    db.withConnection { implicit connection =>
      // 1. Очищаем ключи идемпотентности, у которых истек срок жизни (24 часа согласно миграции V10)
      val deletedIdempotencyRows =
        SQL"""
          DELETE FROM idempotency_records
          WHERE expires_at < NOW()
        """.executeUpdate()

      if (deletedIdempotencyRows > 0) {
        logger.info(s"[DB Cleanup] Удалено устаревших ключей идемпотентности: $deletedIdempotencyRows")
      }

      // 2. Очищаем старые, давно отозванные сессии пользователей, чтобы таблица tokens не росла бесконечно
      val deletedTokenRows =
        SQL"""
          DELETE FROM tokens
          WHERE revoked = true AND updated_at < NOW() - INTERVAL '30 days'
        """.executeUpdate()

      if (deletedTokenRows > 0) {
        logger.info(s"[DB Cleanup] Удалено старых архивных сессий (revoked > 30 дней): $deletedTokenRows")
      }
    }
  }
}

