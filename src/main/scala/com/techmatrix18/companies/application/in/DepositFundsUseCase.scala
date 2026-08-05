package com.techmatrix18.companies.application.in

import com.techmatrix18.companies.domain.{Company, CompanyId, CompanyStatus}
import com.techmatrix18.companies.application.out.CompanyRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * DepositFundsUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class DepositFundsUseCase(
  companyRepository: CompanyRepository
)(using ec: ExecutionContext) {

  def execute(command: DepositFundsCommand): Future[Either[String, DepositFundsResponse]] = {

    // 1. Валидация входных данных на прикладном уровне
    if (command.amount <= 0) {
      Future.successful(Left("Deposit amount must be strictly greater than zero"))
    } else {

      // 2. Асинхронный поиск компании через Out-порт репозитория
      companyRepository.findById(command.companyId).flatMap {
        case None =>
          Future.successful(Left(s"Company with ID '${command.companyId.value}' not found"))

        case Some(company) =>
          // 3. Создаем мутировавшую иммутабельную копию сущности с обновленным балансом
          val updatedCompany = company.copy(
            balance = company.balance + command.amount,
            updatedAt = Instant.now()
          )

          // 4. Сохраняем измененное состояние обратно в PostgreSQL
          companyRepository.update(updatedCompany).map { _ =>
            Right(DepositFundsResponse(
              companyId = updatedCompany.id.value,
              newBalance = updatedCompany.balance
            ))
          }.recover {
            case ex: Exception =>
              Left(s"Failed to update company balance due to an infrastructure error: ${ex.getMessage}")
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class DepositFundsResponse(
  companyId: String,
  newBalance: BigDecimal
)

