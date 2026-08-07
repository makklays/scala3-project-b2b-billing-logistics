package com.techmatrix18.users.domain

import java.time.Instant

/**
 * UserId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

// Строгая обертка для ID пользователя на базе Long (BIGSERIAL)
opaque type UserId = Long

object UserId {

  def apply(value: Long): UserId = value

  extension (id: UserId) {
    def raw: Long = id
  }
}

