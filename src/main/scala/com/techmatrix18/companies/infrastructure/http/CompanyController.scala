package com.techmatrix18.companies.infrastructure.http

import com.techmatrix18.companies.application.in.{CreateCompanyCommand, CreateCompanyUseCase}
import com.techmatrix18.companies.infrastructure.http.CompanyJsonFormats.given // Импортируем форматы Scala 3
import play.api.mvc.*
import play.api.libs.json.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * CompanyController - Driving HTTP Adapter for Company domain
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

@Singleton
class CompanyController @Inject()(
  val controllerComponents: ControllerComponents,
  createCompanyUseCase: CreateCompanyUseCase
)(using ec: ExecutionContext) extends BaseController {

  /**
   * POST /api/v1/companies
   * Registers a new B2B company in the logistics platform
   */
  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    // Пытаемся распарсить тело HTTP-запроса в нашу Command DTO
    request.body.validate[CreateCompanyCommand] match {

      // Если JSON не валиден (например, пропущено поле taxNumber или неверный формат)
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))

      // Если JSON валиден, передаем команду в Use Case прикладного уровня
      case JsSuccess(command, _) =>
        createCompanyUseCase.execute(command).map {

          // Если бизнес-логика Use Case вернула ошибку валидации (например, пустое имя)
          case Left(businessError) =>
            BadRequest(Json.obj(
              "status" -> "Fail",
              "message" -> businessError
            ))

          // Если компания успешно создана в PostgreSQL
          case Right(useCaseResponse) =>
            Created(Json.toJson(useCaseResponse))
        }
    }
  }
}

