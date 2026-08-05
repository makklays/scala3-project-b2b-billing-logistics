package com.techmatrix18.companies.application.in

import com.techmatrix18.companies.domain.{Company, CompanyId, CompanyStatus}
import com.techmatrix18.companies.application.out.CompanyRepository
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * CreateCompanyUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

class CreateCompanyUseCase(
  companyRepository: CompanyRepository
)(using ec: ExecutionContext) { // Внедряем пул потоков для работы с Future

  // Бизнес-сценарий возвращает Future[Either[Ошибка, Успех]]
  def execute(command: CreateCompanyCommand): Future[Either[String, CreateCompanyResponse]] = {

    // Валидация входных данных на уровне прикладной логики
    if (command.name.trim.isEmpty) {
      Future.successful(Left("Company name cannot be empty"))
    } else if (command.taxNumber.trim.isEmpty) {
      Future.successful(Left("Tax number (CIF/NIF) is required for Spanish B2B infrastructure"))
    } else if (command.initialBalance < 0) {
      Future.successful(Left("Initial balance cannot be negative"))
    } else {

      // Сборка чистой доменной сущности (Aggregate Root)
      val newCompany = Company(
        id = CompanyId.generate(), // Используем наш безопасный генератор ID
        name = command.name,
        taxNumber = command.taxNumber,
        balance = command.initialBalance,
        status = CompanyStatus.Active, // Новая компания сразу активна
        createdAt = Instant.now(),
        updatedAt = Instant.now()
      )

      // Асинхронное сохранение в PostgreSQL через Out-порт репозитория
      companyRepository.create(newCompany).map { createdId =>
        // Мапим результат в успешный ответ
        Right(CreateCompanyResponse(
          companyId = createdId.value, // Достаем сырую строку через наш extension метод
          name = newCompany.name
        ))
      }.recover {
        // Защита от системных сбоев (например, дубликат tax_number в базе данных)
        case ex: Exception => Left(s"Failed to persist company due to database error: ${ex.getMessage}")
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class CreateCompanyResponse(
  companyId: String,
  name: String
)

