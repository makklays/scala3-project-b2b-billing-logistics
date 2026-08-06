package com.techmatrix18.section_tariffs.application.in

import com.techmatrix18.section_tariffs.application.out.SectionTariffRepository
import com.techmatrix18.section_tariffs.domain.{SectionTariff, SectionTariffId}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

/**
 * CancelSectionTariffUseCase - Inbound Driving Service for early tariff cancellation
 * Аннулирование / Досрочное расторжение контракта
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

class CancelSectionTariffUseCase(
  tariffRepository: SectionTariffRepository
)(using ec: ExecutionContext) {

  // Executes the scenario of canceling a section tariff early
  def execute(command: CancelSectionTariffCommand): Future[Either[String, CancelSectionTariffResponse]] = {

    // 1. Асинхронно ищем тариф в PostgreSQL через репозиторий по строгому ID
    tariffRepository.findById(command.tariffId).flatMap {
      case None =>
        Future.successful(Left(s"Section tariff with ID '${command.tariffId.value}' not found"))

      case Some(tariff) =>
        val now = Instant.now()

        // 2. Проверяем доменные инварианты: защищаем бизнес-логику от некорректных изменений
        if (tariff.validTo.isBefore(now)) {
          Future.successful(Left("Cannot cancel tariff: this contract has already historically expired"))
        } else {

          // 3. Создаем иммутабельный слепок сущности с досрочно завершенным сроком действия контракта
          val canceledTariff = tariff.copy(
            validTo = now, // Принудительно закрываем тариф текущим моментом времени
            updatedAt = now
          )

          // 4. Сохраняем обновленное состояние тарифа в базу данных
          tariffRepository.update(canceledTariff).map { _ =>
            Right(CancelSectionTariffResponse(
              tariffId = canceledTariff.id.value, // Наш метод расширения из SectionTariffId
              canceledAt = now
            ))
          }.recover {
            case error: Exception =>
              Left(s"Failed to cancel section tariff due to database error: ${error.getMessage}")
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class CancelSectionTariffResponse(
  tariffId: String,
  canceledAt: Instant
)

