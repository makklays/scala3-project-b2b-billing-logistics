package com.techmatrix18.companies.application.in

/**
 * Command to create a new company.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

case class CreateCompanyCommand(
  title: String,               // Синхронизировано с полем 'name' в SQL и домене
  taxNumber: String,           // Обязательный налоговый код (CIF/NIF) для B2B Испании
  initialBalance: BigDecimal   // Начальный баланс при онбординге компании
)

