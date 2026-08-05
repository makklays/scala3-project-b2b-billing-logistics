package com.techmatrix18.companies.application.in

import com.techmatrix18.companies.domain.{Company, CompanyId, CompanyStatus}
import com.techmatrix18.companies.application.out.CompanyRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * DeleteCompanyUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class DeleteCompanyUseCase(
  companyRepository: CompanyRepository
)(using ec: ExecutionContext) {

  // Executes soft delete scenario by switching company status to 'Deleted'
  def execute(command: DeleteCompanyCommand): Future[Either[String, DeleteCompanyResponse]] = {
    companyRepository.findById(command.companyId).flatMap {
      case None =>
        Future.successful(Left(s"Company with ID '${command.companyId.value}' not found"))

      case Some(company) =>
        if (company.status == CompanyStatus.Deleted) {
          Future.successful(Left("Company is already deleted"))
        } else {
          // Создаем иммутабельный слепок сущности со статусом Deleted (Мягкое удаление)
          val deletedCompany = company.copy(
            status = CompanyStatus.Deleted,
            updatedAt = Instant.now()
          )

          // Сохраняем изменения состояния в PostgreSQL
          companyRepository.update(deletedCompany).map { _ =>
            Right(DeleteCompanyResponse(
              companyId = deletedCompany.id.value, // Извлекаем String через наш метод расширения
              deletedAt = deletedCompany.updatedAt
            ))
          }.recover {
            case ex: Exception =>
              Left(s"Failed to soft-delete company due to database error: ${ex.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class DeleteCompanyResponse(
  companyId: String,
  deletedAt: Instant
)

