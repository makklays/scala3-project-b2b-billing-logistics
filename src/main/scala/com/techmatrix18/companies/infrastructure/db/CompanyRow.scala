package com.techmatrix18.companies.infrastructure.db

import com.techmatrix18.companies.domain.{Company, CompanyId, CompanyStatus}
import java.time.Instant
import java.util.UUID
import anorm.*
import anorm.SqlParser.*

/**
 * CompanyRow - Инфраструктурный маппер для таблицы компаний
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
      id = CompanyId(id.toString),
      title = title,
      taxNumber = taxNumber,
      balance = balance,
      status = CompanyStatus.valueOf(status),
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }
}

object CompanyRow {

  // ИСПРАВЛЕНО: Явный, типобезопасный парсер, который вычитывает все 7 полей в правильном порядке
  val parser: RowParser[CompanyRow] = {
    SqlParser.get[UUID]("id") ~
      SqlParser.get[String]("title") ~
      SqlParser.get[String]("tax_number") ~ // Маппинг на колонку tax_number в СУБД
      SqlParser.get[BigDecimal]("balance") ~ // Читаем BigDecimal для баланса
      SqlParser.get[String]("status") ~
      SqlParser.get[Instant]("created_at") ~
      SqlParser.get[Instant]("updated_at") map {
      case id ~ title ~ taxNumber ~ balance ~ status ~ createdAt ~ updatedAt =>
        CompanyRow(id, title, taxNumber, balance, status, createdAt, updatedAt)
    }
  }

  // Конвертация доменной модели в строку БД перед сохранением
  def fromDomain(company: Company): CompanyRow = CompanyRow(
    id = UUID.fromString(company.id.value),
    title = company.title,
    taxNumber = company.taxNumber,
    balance = company.balance,
    status = company.status.toString,
    createdAt = company.createdAt,
    updatedAt = company.updatedAt
  )
}

