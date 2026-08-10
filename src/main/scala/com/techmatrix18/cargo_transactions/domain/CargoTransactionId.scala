package com.techmatrix18.cargo_transactions.domain

/**
 * CargoTransactionId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 10.08.2026
 */

// Нативный Scala 3 строгий тип для ID транзакции груза
opaque type CargoTransactionId = String

object CargoTransactionId {
  def apply(value: String): CargoTransactionId = value
  extension (id: CargoTransactionId) {
    def raw: String = id
  }
}

