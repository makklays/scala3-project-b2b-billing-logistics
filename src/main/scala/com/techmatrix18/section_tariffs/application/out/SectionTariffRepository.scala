package com.techmatrix18.section_tariffs.application.out

import com.techmatrix18.section_tariffs.domain.{SectionTariff, SectionTariffId}
import com.techmatrix18.hub_sections.domain.HubSectionId
import java.time.Instant
import scala.concurrent.Future

/**
 * SectionTariffRepository - Outbound Driven Port for financial tariff persistence.
 *
 * В коммерческих финтех- и B2B-платформах автоматизации логистики физическое удаление тарифов
 * через SQL-команду DELETE — это строжайшее табу (!)
 * И вот почему метод delete сознательно исключается из интерфейса SectionTariffRepository
 * Вместо удаления всегда применяется паттерн Мягкого закрытия интервала (Soft Expiration).
 * Если контракт аннулирован или цены изменились:
 *  1. Вызывается спроектированный нами ранее сценарий CancelSectionTariffUseCase
 *  2. Он берет текущий тариф и выставляет его поле validTo на текущую секунду (Instant.now())
 *  3. Тариф мгновенно становится «архивным». Новые биллинг-акторы его игнорируют, так как они ищут цены через
 *     условие valid_from <= NOW() AND valid_to >= NOW(), но для исторических финансовых отчетов за прошлые периоды
 *     он остается доступен в базе на 100%
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

trait SectionTariffRepository {

  // Находит конкретный тарифный контракт по его строгому идентификатору
  def findById(tariffId: SectionTariffId): Future[Option[SectionTariff]]

  // Создает (регистрирует) новый B2B тариф в PostgreSQL
  def create(tariff: SectionTariff): Future[SectionTariffId]

  // Обновляет состояние существующего тарифа (цены, сроки действия validTo)
  def update(tariff: SectionTariff): Future[Unit]

  // Универсальный Senior-метод для поиска и фильтрации тарифов.
  // Позволяет биллинг-акторам мгновенно находить кастомную цену для клиента.
  def findByFilter(filter: SectionTariffFilter): Future[List[SectionTariff]]
}

