package com.techmatrix18.companies.infrastructure.db

import com.techmatrix18.companies.domain.{Company, CompanyId, CompanyStatus}
import anorm.{Macro, RowParser, ~}
import java.time.Instant
import java.util.UUID

/**
 * CompanyRow
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

case class CompanyRow(
  id: UUID,
  title: String,
  taxNumber: String,
  balance: BigDecimal,
  status: String,
  createdAt: Instant,
  updatedAt: Instant
) {

  // Конвертация строки из БД в чистую доменную сущность
  def toDomain: Company = {
    Company(
      id = CompanyId(id),
      title = title,
      taxNumber = taxNumber,
      balance = balance,
      status = CompanyStatus.withName(status),
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }
}

object CompanyRow {
  // Автоматический парсер Anorm для маппинга полей SQL в case-класс CompanyRow
  val parser: RowParser[CompanyRow] = Macro.named[CompanyRow]

  // Конвертация доменной модели в строку БД перед сохранением
  def fromDomain(company: Company): CompanyRow = CompanyRow(
    id = UUID.fromString(company.id.value),  // Достаем UUID через метод расширения
    title = company.title,
    taxNumber = company.taxNumber,
    balance = company.balance,
    status = company.status.toString,        // Переводим Enum в VARCHAR для базы
    createdAt = company.createdAt,
    updatedAt = company.updatedAt
  )
}

