package com.techmatrix18.companies.application.in

import com.techmatrix18.companies.domain.{Company, CompanyId, CompanyStatus}
import com.techmatrix18.companies.application.out.CompanyRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * SuspendCompanyUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class SuspendCompanyUseCase(
  companyRepository: CompanyRepository
)(using ec: ExecutionContext) {

  // Executes the company suspension scenario
  def execute(command: SuspendCompanyCommand): Future[Either[String, SuspendCompanyResponse]] = {
    companyRepository.findById(command.companyId).flatMap {
      case None =>
        Future.successful(Left(s"Company with ID '${command.companyId.value}' not found"))

      case Some(company) =>
        if (company.status == CompanyStatus.Inactive) {
          Future.successful(Left("Company is already suspended"))
        } else if (company.status == CompanyStatus.Deleted) {
          Future.successful(Left("Cannot suspend an already deleted company account"))
        } else {
          // Создаем иммутабельный слепок сущности со статусом Inactive (Блокировка)
          val suspendedCompany = company.copy(
            status = CompanyStatus.Inactive,
            updatedAt = Instant.now()
          )

          // Асинхронно сохраняем измененное состояние в PostgreSQL
          companyRepository.update(suspendedCompany).map { _ =>
            Right(SuspendCompanyResponse(
              companyId = suspendedCompany.id.value, // Извлекаем String через extension-метод
              suspendedAt = suspendedCompany.updatedAt
            ))
          }.recover {
            case ex: Exception =>
              Left(s"Failed to suspend company due to database error: ${ex.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class SuspendCompanyResponse(
  companyId: String,
  suspendedAt: Instant
)

