package com.techmatrix18.outbox.infrastructure.scheduler

import com.techmatrix18.outbox.application.out.OutboxRepository
import com.techmatrix18.outbox.domain.{OutboxEvent, OutboxStatus, OutboxEventId}
import javax.inject.{Inject, Singleton}
import org.apache.pekko.actor.ActorSystem // Для Play Framework на Pekko (в старых версиях используйте akka.actor)
import play.api.Logger
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.*
import scala.util.{Failure, Success}

/**
 * OutboxRelayScheduler - Автономный фоновый процессор событий.
 * Выгребает PENDING-записи из PostgreSQL и доставляет их в брокер.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

@Singleton
class OutboxRelayScheduler @Inject()(
  actorSystem: ActorSystem,
  outboxRepository: OutboxRepository
)(using ec: ExecutionContext) {

  private val logger = Logger(this.getClass)
  private val BatchSize = 100       // За один раз обрабатываем не более 100 сообщений, чтобы не грузить ОЗУ

  // Запуск периодической задачи: старт через 5 секунд после поднятия сервера, далее каждые 1500 миллисекунд
  actorSystem.scheduler.scheduleWithFixedDelay(
    initialDelay = 5.seconds,
    delay = 1500.milliseconds
  )(() => processOutboxQueue())

  /**
   * Основной рабочий цикл реле-процессора
   */
  private def processOutboxQueue(): Unit = {
    outboxRepository.fetchPendingEvents(BatchSize).onComplete {
      case Failure(ex) =>
        logger.error(s"[Outbox Scheduler] Ошибка при выборке событий из PostgreSQL: ${ex.getMessage}")

      case Success(events) if events.isEmpty =>
        // Очередь пуста, лог писать не нужно, чтобы не забивать stdout
        ()

      case Success(events) =>
        logger.info(s"[Outbox Scheduler] Найдено ${events.size} неотправленных событий. Запускаю реле...")

        // Передаем пачку событий в метод симуляции отправки в брокер
        publishBatchToBroker(events).onComplete {
          case Success(deliveredIds) if deliveredIds.nonEmpty =>
            // Атомарно переводим успешно доставленные события в статус PROCESSED за 1 SQL-запрос
            outboxRepository.updateStatuses(deliveredIds, OutboxStatus.Processed).onComplete {
              case Success(_) =>
                logger.info(s"[Outbox Scheduler] Успешно подтверждена доставка ${deliveredIds.size} сообщений в СУБД")
              case Failure(ex) =>
                logger.error(s"[Outbox Scheduler] Не удалось обновить статусы в базе данных: ${ex.getMessage}")
            }
          case Success(_) =>
            // Ни одно сообщение не смогло улететь (например, брокер лежит)
            ()
          case Failure(ex) =>
            logger.error(s"[Outbox Scheduler] Критический сбой конвейера отправки: ${ex.getMessage}")
        }
    }
  }

  /**
   * Имитация отправки пакета сообщений в шину данных (Kafka / RabbitMQ).
   * Возвращает список ID только тех событий, которые брокер успешно принял (Ack).
   */
  private def publishBatchToBroker(events: List[OutboxEvent]): Future[List[OutboxEventId]] = {
    // В реальном проекте здесь будет инжектиться KafkaProducer из Alpakka Kafka.
    // Мы моделируем асинхронную отправку всей пачки через Future.sequence.
    val publishingFutures = events.map { event =>
      simulateSingleMessagePublish(event).map {
        case true => Some(event.id)
        case false => None
      }
    }

    Future.sequence(publishingFutures).map(_.flatten)
  }

  /**
   * Симуляция отправки одного сообщения
   */
  private def simulateSingleMessagePublish(event: OutboxEvent): Future[Boolean] = Future {
    try {
      // Имитируем отправку payload строки в топик, названный в честь aggregateType
      // logger.debug(s"Отправка в топик [${event.aggregateType}]: ${event.payload}")

      // Возвращаем true — сообщение успешно доставлено и подтверждено брокером
      true
    } catch {
      case ex: Exception =>
        logger.error(s"Ошибка отправки события ${event.id.raw} в брокер: ${ex.getMessage}")
        false
    }
  }
}

