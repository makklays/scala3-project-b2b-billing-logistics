package com.techmatrix18.idempotency.domain

import java.time.Instant

/**
 * IdempotencyKey - Строгая непрозрачная обертка для ключа идемпотентности (обычно UUID с фронтенда).
 * Исключает случайное смешивание со стандартными String-полями на этапе компиляции.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

opaque type IdempotencyKey = String

object IdempotencyKey {
  def apply(value: String): IdempotencyKey = value

  extension (key: IdempotencyKey) {
    def raw: String = key
  }
}

