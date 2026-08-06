package com.techmatrix18.section_tariffs.application.in

import com.techmatrix18.section_tariffs.application.out.SectionTariffRepository
import com.techmatrix18.section_tariffs.domain.{SectionTariff, SectionTariffId}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * UpdateSectionTariffRatesUseCase - Inbound Driving Service for indexed price modifications
 * Индексация стоимости хранения
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class UpdateSectionTariffRatesUseCase(
  tariffRepository: SectionTariffRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of updating warehouse section billing rates
  def execute(command: UpdateSectionTariffRatesCommand): Future[Either[String, UpdateSectionTariffRatesResponse]] = {

    // 1. Прикладная валидация обновляемых тарифов
    if (command.newOccupiedRate <= 0) {
      Future.successful(Left("Indexed storage rate for occupied space must be strictly greater than zero"))
    } else if (command.newEmptyRate <= 0) {
      Future.successful(Left("Indexed reservation rate for empty space must be strictly greater than zero"))
    } else {

      // 2. Асинхронно ищем тариф в PostgreSQL по строгому ID через репозиторий
      tariffRepository.findById(command.tariffId).flatMap {
        case None =>
          Future.successful(Left(s"Section tariff contract with ID '${command.tariffId.value}' not found"))

        case Some(tariff) =>
          val now = Instant.now()

          // 3. Проверяем доменный инвариант: нельзя менять цены в уже завершенных (истекших) контрактах
          if (tariff.validTo.isBefore(now)) {
            Future.successful(Left("Cannot modify rates: this tariff contract has already historically expired"))
          } else {

            // 4. Создаем иммутабельный слепок сущности домена с новой тарифной сеткой
            val updatedTariff = tariff.copy(
              occupiedRatePerHour = command.newOccupiedRate,
              emptyReservationRatePerHour = command.newEmptyRate,
              updatedAt = now
            )

            // 5. Сохраняем измененный финансовый агрегат в PostgreSQL
            tariffRepository.update(updatedTariff).map { _ =>
              Right(UpdateSectionTariffRatesResponse(
                tariffId = updatedTariff.id.value, // Наш метод расширения (extension) из SectionTariffId
                newOccupiedRate = updatedTariff.occupiedRatePerHour,
                newEmptyRate = updatedTariff.emptyReservationRatePerHour,
                updatedAt = updatedTariff.updatedAt
              ))
            }.recover {
              case error: Exception =>
                Left(s"Failed to update section tariff rates due to database infrastructure error: ${error.getMessage}")
            }
          }
      }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class UpdateSectionTariffRatesResponse(
  tariffId: String,
  newOccupiedRate: BigDecimal,
  newEmptyRate: BigDecimal,
  updatedAt: Instant
)

