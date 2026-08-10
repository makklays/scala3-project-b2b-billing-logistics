package com.techmatrix18.cargo_transactions.infrastructure.http

import com.techmatrix18.cargo_transactions.domain.CargoTransaction
import com.techmatrix18.cargo_transactions.application.in.*
import play.api.libs.json.{Json, OFormat}

/**
 * CargoTransactionJsonFormats - Infrastructure driving adapter for Play JSON marshalling.
 * Provides implicit formats for cargo ledger commands and response DTOs using Scala 3 'given' syntax.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

object CargoTransactionJsonFormats {

  // 1. Формат для входящей JSON-команды (Фиксация операции перемещения груза)
  given logCargoTransactionCommandFormat: OFormat[LogCargoTransactionCommand] = Json.format[LogCargoTransactionCommand]

  // 2. Формат для исходящего JSON-ответа (Подтверждение записи в Ledger-книгу)
  given logCargoTransactionResponseFormat: OFormat[LogCargoTransactionResponse] = Json.format[LogCargoTransactionResponse]

  // 3. Специфический формат для доменной модели грузов на Scala 3
  given cargoTransactionFormat: OFormat[CargoTransaction] = Json.format[CargoTransaction]
}

