package com.techmatrix18.companies.application.in

import com.techmatrix18.companies.domain.{Company, CompanyId, CompanyStatus}
import com.techmatrix18.companies.application.out.CompanyRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * UpdateCompanyUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class UpdateCompanyUseCase( // Изменено с case class на обычный class
  companyRepository: CompanyRepository
)(using ec: ExecutionContext) {

  // Executes company profile updating scenario
  def execute(command: UpdateCompanyCommand): Future[Either[String, UpdateCompanyResponse]] = {

    // 1. Прикладная валидация входных данных
    if (command.title.trim.isEmpty) {
      Future.successful(Left("Company title cannot be empty"))
    } else if (command.taxNumber.trim.isEmpty) {
      Future.successful(Left("Tax number (CIF/NIF) is required for Spanish infrastructure billing"))
    } else {

      // 2. Асинхронный поиск сущности в PostgreSQL
      companyRepository.findById(command.companyId).flatMap {
        case None =>
          Future.successful(Left(s"Company with ID '${command.companyId.value}' not found"))

        case Some(company) =>
          // Проверяем, не удалена ли компания
          if (company.status == CompanyStatus.Deleted) {
            Future.successful(Left("Cannot update profile data for a soft-deleted company account"))
          } else {

            // 3. Создаем иммутабельную копию сущности домена с новыми реквизитами
            // Обратите внимание: поле в домене называется 'name', но команда прилетает с 'title'
            val updatedCompany = company.copy(
              title = command.title,
              taxNumber = command.taxNumber,
              updatedAt = Instant.now()
            )

            // 4. Сохраняем обновленные данные в базу
            companyRepository.update(updatedCompany).map { _ =>
              Right(UpdateCompanyResponse(
                companyId = updatedCompany.id.value, // Наш метод расширения для извлечения String
                title = updatedCompany.title,
                taxNumber = updatedCompany.taxNumber
              ))
            }.recover {
              case ex: Exception =>
                Left(s"Failed to update company data due to database error: ${ex.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class UpdateCompanyResponse(
  companyId: String,
  title: String,
  taxNumber: String
)

