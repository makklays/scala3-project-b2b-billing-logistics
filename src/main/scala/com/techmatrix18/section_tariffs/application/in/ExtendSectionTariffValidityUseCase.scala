package com.techmatrix18.section_tariffs.application.in

import com.techmatrix18.section_tariffs.application.out.SectionTariffRepository
import com.techmatrix18.section_tariffs.domain.{SectionTariff, SectionTariffId}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * ExtendSectionTariffValidityUseCase - Inbound Driving Service for extending B2B tariff contracts
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class ExtendSectionTariffValidityUseCase(
  tariffRepository: SectionTariffRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of extending a section tariff expiration date
  def execute(command: ExtendSectionTariffValidityCommand): Future[Either[String, ExtendSectionTariffValidityResponse]] = {

    // 1. Прикладная валидация временного интервала пролонгации
    if (command.extendedValidTo.isBefore(Instant.now())) {
      Future.successful(Left("Cannot extend contract validity to a timestamp in the past"))
    } else {

      // 2. Асинхронно ищем тариф в PostgreSQL по строгому ID
      tariffRepository.findById(command.tariffId).flatMap {
        case None =>
          Future.successful(Left(s"Section tariff with ID '${command.tariffId.value}' not found"))

        case Some(tariff) =>
          // 3. Проверяем доменные инварианты: новая дата окончания должна быть позже даты начала тарифа
          if (command.extendedValidTo.isBefore(tariff.validFrom)) {
            Future.successful(Left("Invalid expiration date: extended visibility limit cannot be set before the contract start date"))
          } else {

            val now = Instant.now()

            // 4. Создаем иммутабельный слепок сущности домена с продленной датой окончания договора
            val extendedTariff = tariff.copy(
              validTo = command.extendedValidTo,
              updatedAt = now
            )

            // 5. Сохраняем обновленные данные в базу данных через репозиторий
            tariffRepository.update(extendedTariff).map { _ =>
              Right(ExtendSectionTariffValidityResponse(
                tariffId = extendedTariff.id.value, // Наш метод расширения из SectionTariffId
                extendedValidTo = extendedTariff.validTo,
                updatedAt = extendedTariff.updatedAt
              ))
            }.recover {
              case error: Exception =>
                Left(s"Failed to extend section tariff validity due to database error: ${error.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class ExtendSectionTariffValidityResponse(
  tariffId: String,
  extendedValidTo: Instant,
  updatedAt: Instant
)

