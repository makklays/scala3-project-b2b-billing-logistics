package com.techmatrix18.idempotency.domain

/**
 * IdempotencyStatus - Жизненный цикл обработки уникального сетевого запроса.
 * Полностью соответствует CHECK-констрейнту в PostgreSQL миграции.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

enum IdempotencyStatus(val code: String) {
  case Started    extends IdempotencyStatus("STARTED")
  case Processing extends IdempotencyStatus("PROCESSING")
  case Completed  extends IdempotencyStatus("COMPLETED")
  case Failed     extends IdempotencyStatus("FAILED")
}

