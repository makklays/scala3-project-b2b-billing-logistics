package com.techmatrix18.users.domain

import java.time.Instant

/**
 * TokenId - Строгая обертка для ID токена на базе Long (согласно BIGSERIAL в БД)
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

opaque type TokenId = Long

object TokenId {
  def apply(value: Long): TokenId = value
  extension (id: TokenId) {
    def raw: Long = id
  }
}

