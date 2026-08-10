package com.techmatrix18.companies.infrastructure.db

import com.techmatrix18.companies.domain.{Company, CompanyId}
import com.techmatrix18.companies.application.out.CompanyRepository
import play.api.db.Database
import anorm.*
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * PostgresCompanyRepository - Infrasructure DB Adapter for PostgreSQL
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

class PostgresCompanyRepository(
  db: Database
)(using ec: ExecutionContext) extends CompanyRepository { // Реализуем Out-порт из слоя Application

  override def findById(id: CompanyId): Future[Option[Company]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT id, title, tax_number as taxNumber, balance, status,
               created_at as createdAt, updated_at as updatedAt
        FROM companies
        WHERE id = ${UUID.fromString(id.value)}::uuid
      """.as(CompanyRow.parser.singleOpt).map(_.toDomain)
    }
  }

  override def update(company: Company): Future[Unit] = Future {
    val row = CompanyRow.fromDomain(company)
    db.withConnection { implicit connection =>
      SQL"""
        UPDATE companies
        SET title = ${row.title},
            balance = ${row.balance},
            status = ${row.status},
            updated_at = ${row.updatedAt}
        WHERE id = ${row.id}::uuid
      """.executeUpdate()
      () // Возвращаем Unit (void)
    }
  }

  override def create(company: Company): Future[CompanyId] = Future {
    val row = CompanyRow.fromDomain(company)
    db.withConnection { implicit connection =>
      SQL"""
        INSERT INTO companies (id, title, tax_number, balance, status, created_at, updated_at)
        VALUES (
          ${row.id}::uuid,
          ${row.title},
          ${row.taxNumber},
          ${row.balance},
          ${row.status},
          ${row.createdAt},
          ${row.updatedAt}
        )
      """.executeInsert()

      company.id // Возвращаем созданный CompanyId в Application слой
    }
  }
}

