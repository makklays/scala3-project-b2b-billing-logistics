package com.techmatrix18.companies.infrastructure.http

import com.techmatrix18.companies.application.in.{CreateCompanyCommand, CreateCompanyResponse}
import play.api.libs.json.{Json, OFormat}

/**
 * CompanyJsonFormats
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

object CompanyJsonFormats {

  // Автоматический формат для парсинга входящего JSON в команду
  given createCommandFormat: OFormat[CreateCompanyCommand] = Json.format[CreateCompanyCommand]

  // Автоматический формат для превращения ответа Use Case в исходящий JSON
  given createResponseFormat: OFormat[CreateCompanyResponse] = Json.format[CreateCompanyResponse]
}

