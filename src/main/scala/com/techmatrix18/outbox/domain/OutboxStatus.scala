package com.techmatrix18.outbox.domain

/**
 * OutboxStatus - Жизненный цикл доставки сообщения в брокер (Kafka/RabbitMQ).
 * 'PENDING'   - Событие сохранено в БД, но еще не отправлено релеем/шедулером.
 * 'PROCESSED' - Событие успешно подтверждено брокером.
 * 'FAILED'    - Доставка завершилась критической ошибкой после серии попыток.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

enum OutboxStatus(val code: String) {
  case Pending   extends OutboxStatus("PENDING")
  case Processed extends OutboxStatus("PROCESSED")
  case Failed    extends OutboxStatus("FAILED")
}

