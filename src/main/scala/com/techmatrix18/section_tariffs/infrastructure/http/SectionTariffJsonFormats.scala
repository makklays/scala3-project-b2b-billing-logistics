package com.techmatrix18.section_tariffs.infrastructure.http

import com.techmatrix18.section_tariffs.application.in.*
import play.api.libs.json.{Json, OFormat}

/**
 * SectionTariffJsonFormats - Infrastructure driving adapter for Play JSON marshalling.
 * Provides implicit formats for core billing tariff commands and response DTOs using Scala 3 'given' syntax.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

object SectionTariffJsonFormats {

  // 1. Форматы для входящих JSON-команд (Финтех-управление контрактами из API)
  given createSectionTariffCommandFormat: OFormat[CreateSectionTariffCommand] = Json.format[CreateSectionTariffCommand]
  given cancelSectionTariffCommandFormat: OFormat[CancelSectionTariffCommand] = Json.format[CancelSectionTariffCommand]
  given updateSectionTariffRatesCommandFormat: OFormat[UpdateSectionTariffRatesCommand] = Json.format[UpdateSectionTariffRatesCommand]
  given extendSectionTariffValidityCommandFormat: OFormat[ExtendSectionTariffValidityCommand] = Json.format[ExtendSectionTariffValidityCommand]

  // 2. Форматы для исходящих DTO-ответов (Передача финансовых данных клиенту)
  given createSectionTariffResponseFormat: OFormat[CreateSectionTariffResponse] = Json.format[CreateSectionTariffResponse]
  given cancelSectionTariffResponseFormat: OFormat[CancelSectionTariffResponse] = Json.format[CancelSectionTariffResponse]
  given updateSectionTariffRatesResponseFormat: OFormat[UpdateSectionTariffRatesResponse] = Json.format[UpdateSectionTariffRatesResponse]
  given extendSectionTariffValidityResponseFormat: OFormat[ExtendSectionTariffValidityResponse] = Json.format[ExtendSectionTariffValidityResponse]
}

