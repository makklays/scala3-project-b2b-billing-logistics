package com.techmatrix18.outbox.domain

import java.time.Instant
import java.util.UUID

/**
 * OutboxEventId - Типобезопасная обертка для ID события на базе UUID
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

opaque type OutboxEventId = UUID

object OutboxEventId {
  def apply(value: UUID): OutboxEventId = value

  def generate(): OutboxEventId = UUID.randomUUID()

  extension (id: OutboxEventId) {
    def raw: UUID = id
  }
}

