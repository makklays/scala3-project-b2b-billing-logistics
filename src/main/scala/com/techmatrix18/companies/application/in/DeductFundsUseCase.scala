package com.techmatrix18.companies.application.in

import com.techmatrix18.companies.domain.{Company, CompanyId, CompanyStatus}
import com.techmatrix18.companies.application.out.CompanyRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * DeductFundsUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

class DeductFundsUseCase(
  companyRepository: CompanyRepository
)(using ec: ExecutionContext) {

  // Executes automated funds deduction logic
  def execute(command: DeductFundsCommand): Future[Either[String, DeductFundsResponse]] = {

    // 1. Первичная валидация суммы на прикладном уровне
    if (command.amount <= 0) {
      Future.successful(Left("Deduction amount must be strictly greater than zero"))
    } else {

      // 2. Асинхронный поиск компании через Out-порт репозитория
      companyRepository.findById(command.companyId).flatMap {
        case None =>
          Future.successful(Left(s"Company with ID '${command.companyId.value}' not found"))

        case Some(company) =>
          // Проверяем, не заблокирована ли уже компания
          if (company.status == CompanyStatus.Deleted) {
            Future.successful(Left("Cannot deduct funds from a deleted company account"))
          }
          // 3. Проверяем доменный инвариант: хватает ли средств на балансе?
          else if (company.balance < command.amount) {
            // Бизнес-логика: если денег не хватает, мы уводим баланс в минус,
            // но автоматически меняем статус компании на Suspended (Блокировка ворот)
            val suspendedCompany = company.copy(
              balance = company.balance - command.amount,
              status = CompanyStatus.Inactive, // Меняем статус на неактивный
              updatedAt = Instant.now()
            )

            companyRepository.update(suspendedCompany).map { _ =>
              Left(s"Deduction forced balance into negative. Account suspended. New balance: ${suspendedCompany.balance}")
            }
          }
          else {
            // 4. Успешный сценарий: денег хватает, списываем иммутабельно
            val updatedCompany = company.copy(
              balance = company.balance - command.amount,
              updatedAt = Instant.now()
            )

            // Сохраняем обновленное состояние компании в PostgreSQL
            // В будущем здесь также будет генерироваться вызов BillingTransactionRepository для записи в Ledger лог
            companyRepository.update(updatedCompany).map { _ =>
              Right(DeductFundsResponse(
                companyId = updatedCompany.id.value,
                newBalance = updatedCompany.balance
              ))
            }.recover {
              case ex: Exception =>
                Left(s"Failed to process billing deduction due to database error: ${ex.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class DeductFundsResponse(
  companyId: String,
  newBalance: BigDecimal
)

