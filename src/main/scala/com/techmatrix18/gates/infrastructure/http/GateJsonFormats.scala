package com.techmatrix18.gates.infrastructure.http

import com.techmatrix18.gates.application.in.*
import com.techmatrix18.gates.domain.WorkingHours
import play.api.libs.json.*
import java.time.Instant

/**
 * GateJsonFormats - Infrastructure driving adapter for Play JSON marshalling.
 * Provides implicit formats for core gate commands and DTO responses using Scala 3 'given' syntax.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */
object GateJsonFormats {

  // 1. Формат для Value Object рабочих часов
  given workingHoursFormat: Format[WorkingHours] = Json.format[WorkingHours]

  // 2. Форматы для входящих JSON-команд (используются при парсинге POST/PUT запросов)
  given createGateCommandFormat: OFormat[CreateGateCommand] = Json.format[CreateGateCommand]

  // Для команд, оперирующих доменными ID, мы парсим их на уровне роутера/контроллера из URL,
  // но если они прилетают в теле запроса, play-json автоматически смапит базовые типы.
  given occupyGateCommandFormat: OFormat[OccupyGateCommand] = Json.format[OccupyGateCommand]
  given releaseGateCommandFormat: OFormat[ReleaseGateCommand] = Json.format[ReleaseGateCommand]
  given putGateUnderMaintenanceCommandFormat: OFormat[PutGateUnderMaintenanceCommand] = Json.format[PutGateUnderMaintenanceCommand]
  given activateGateCommandFormat: OFormat[ActivateGateCommand] = Json.format[ActivateGateCommand]
  given updateGateRatesCommandFormat: OFormat[UpdateGateRatesCommand] = Json.format[UpdateGateRatesCommand]
  given updateGateConfigurationCommandFormat: OFormat[UpdateGateConfigurationCommand] = Json.format[UpdateGateConfigurationCommand]
  given deleteGateCommandFormat: OFormat[DeleteGateCommand] = Json.format[DeleteGateCommand]

  // 3. Форматы для исходящих DTO-ответов (превращают case-классы бэкенда в JSON для фронтенда/API)
  given createGateResponseFormat: OFormat[CreateGateResponse] = Json.format[CreateGateResponse]
  given occupyGateResponseFormat: OFormat[OccupyGateResponse] = Json.format[OccupyGateResponse]
  given releaseGateResponseFormat: OFormat[ReleaseGateResponse] = Json.format[ReleaseGateResponse]
  given putGateUnderMaintenanceResponseFormat: OFormat[PutGateUnderMaintenanceResponse] = Json.format[PutGateUnderMaintenanceResponse]
  given activateGateResponseFormat: OFormat[ActivateGateResponse] = Json.format[ActivateGateResponse]
  given updateGateRatesResponseFormat: OFormat[UpdateGateRatesResponse] = Json.format[UpdateGateRatesResponse]
  given updateGateConfigurationResponseFormat: OFormat[UpdateGateConfigurationResponse] = Json.format[UpdateGateConfigurationResponse]
  given deleteGateResponseFormat: OFormat[DeleteGateResponse] = Json.format[DeleteGateResponse]
}

