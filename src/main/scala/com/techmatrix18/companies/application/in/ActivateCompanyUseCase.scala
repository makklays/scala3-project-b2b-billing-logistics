package com.techmatrix18.companies.application.in

import com.techmatrix18.companies.domain.{Company, CompanyId, CompanyStatus}
import com.techmatrix18.companies.application.out.CompanyRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * ActivateCompanyUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class ActivateCompanyUseCase(
  companyRepository: CompanyRepository
)(using ec: ExecutionContext) {

  // Выполняет сценарий активации компании (например, после погашения задолженности B2B-клиентом)
  def execute(command: ActivateCompanyCommand): Future[Either[String, ActivateCompanyResponse]] = {
    companyRepository.findById(command.companyId).flatMap {
      case None =>
        Future.successful(Left(s"Company with ID '${command.companyId.value}' not found"))

      case Some(company) =>
        if (company.status == CompanyStatus.Active) {
          Future.successful(Left("Company is already active"))
        } else {
          // Создаем иммутабельную копию сущности с обновленным статусом
          val activatedCompany = company.copy(
            status = CompanyStatus.Active,
            updatedAt = Instant.now()
          )

          // Сохраняем измененное состояние в PostgreSQL через Out-порт
          companyRepository.update(activatedCompany).map { _ =>
            Right(ActivateCompanyResponse(
              companyId = activatedCompany.id.value, // Извлекаем сырую строку через extension-метод
              activatedAt = activatedCompany.updatedAt
            ))
          }.recover {
            case ex: Exception =>
              Left(s"Failed to activate company due to database error: ${ex.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class ActivateCompanyResponse(
  companyId: String,
  activatedAt: Instant
)

