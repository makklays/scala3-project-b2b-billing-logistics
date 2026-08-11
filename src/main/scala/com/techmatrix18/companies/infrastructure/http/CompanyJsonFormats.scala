package com.techmatrix18.companies.infrastructure.http

import com.techmatrix18.companies.application.in.*
import play.api.libs.json.{Json, OFormat, Format}
import com.techmatrix18.companies.domain.CompanyId

/**
 * CompanyJsonFormats
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

object CompanyJsonFormats {

  given companyIdFormat: Format[CompanyId] =
    Format(
      play.api.libs.json.Reads.StringReads.map(s => s.asInstanceOf[CompanyId]),
      play.api.libs.json.Writes((id: CompanyId) => play.api.libs.json.JsString(id.asInstanceOf[String]))
    )

  // 1. Форматы для создания компании
  given createCommandFormat: OFormat[CreateCompanyCommand] = Json.format[CreateCompanyCommand]
  given createResponseFormat: OFormat[CreateCompanyResponse] = Json.format[CreateCompanyResponse]

  // 2. Форматы для финансовых операций (Биллинг и пополнение)
  given depositFundsCommandFormat: OFormat[DepositFundsCommand] = Json.format[DepositFundsCommand]
  given depositFundsResponseFormat: OFormat[DepositFundsResponse] = Json.format[DepositFundsResponse]

  given deductFundsCommandFormat: OFormat[DeductFundsCommand] = Json.format[DeductFundsCommand]
  given deductFundsResponseFormat: OFormat[DeductFundsResponse] = Json.format[DeductFundsResponse]

  // 3. Форматы для управления жизненным циклом (Активация, блокировка, мягкое удаление)
  given activateCompanyCommandFormat: OFormat[ActivateCompanyCommand] = Json.format[ActivateCompanyCommand]
  given activateCompanyResponseFormat: OFormat[ActivateCompanyResponse] = Json.format[ActivateCompanyResponse]

  given suspendCompanyCommandFormat: OFormat[SuspendCompanyCommand] = Json.format[SuspendCompanyCommand]
  given suspendCompanyResponseFormat: OFormat[SuspendCompanyResponse] = Json.format[SuspendCompanyResponse]

  given deleteCompanyCommandFormat: OFormat[DeleteCompanyCommand] = Json.format[DeleteCompanyCommand]
  given deleteCompanyResponseFormat: OFormat[DeleteCompanyResponse] = Json.format[DeleteCompanyResponse]

  // 4. Форматы для редактирования профиля компании
  given updateCompanyProfileCommandFormat: OFormat[UpdateCompanyCommand] = Json.format[UpdateCompanyCommand]
  given updateCompanyResponseFormat: OFormat[UpdateCompanyResponse] = Json.format[UpdateCompanyResponse]
}

