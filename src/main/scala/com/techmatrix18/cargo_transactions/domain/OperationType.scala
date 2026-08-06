package com.techmatrix18.cargo_transactions.domain

/**
 * OperationType - определяет вектор движения груза на складе
 * (Используем идиоматичный CamelCase для Scala 3)
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 06.08.2026
 */

enum OperationType(val code: String) {
  case Supply   extends OperationType("SUPPLY")     // Приемка / Поступление палет на склад
  case Dispatch extends OperationType("DISPATCH")   // Отгрузка / Вывоз палет со склада
}

