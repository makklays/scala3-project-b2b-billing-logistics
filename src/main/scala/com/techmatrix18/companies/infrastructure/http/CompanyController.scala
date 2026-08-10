package com.techmatrix18.companies.infrastructure.http

import com.techmatrix18.companies.application.in.*
import com.techmatrix18.companies.domain.CompanyId
import com.techmatrix18.companies.domain.CompanyId.*
import com.techmatrix18.companies.infrastructure.http.CompanyJsonFormats.given // Импортируем given-форматы Scala 3
import play.api.mvc.*
import play.api.libs.json.*
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import com.techmatrix18.idempotency.infrastructure.presentation.IdempotencyAction
import com.techmatrix18.companies.application.in.UpdateCompanyUseCase

/**
 * CompanyController - Driving HTTP Adapter for Company and Billing Domain.
 * Оркестрирует финансовые транзакции B2B-клиентов и их жизненный цикл.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.2
 * @since 06.08.2026
 */

@Singleton
class CompanyController @Inject()(
  val controllerComponents: ControllerComponents,
  idempotencyAction: IdempotencyAction,                         // Проверка idempotency
  createCompanyUseCase: CreateCompanyUseCase,
  updateCompanyUseCase: UpdateCompanyUseCase,
  depositFundsUseCase: DepositFundsUseCase,
  deductFundsUseCase: DeductFundsUseCase,
  activateCompanyUseCase: ActivateCompanyUseCase,
  suspendCompanyUseCase: SuspendCompanyUseCase,
  deleteCompanyUseCase: DeleteCompanyUseCase
)(using ec: ExecutionContext) extends BaseController {

  /**
   * POST /api/v1/companies
   * Registers a new B2B company in the logistics platform
   */
  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CreateCompanyCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        createCompanyUseCase.execute(command).map {
          case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
          case Right(response)     => Created(Json.toJson(response))
        }
    }
  }

  /**
   * PUT /api/v1/companies/:id/profile
   * Updates company metadata profile (title, tax registration)
   */
  def updateProfile(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[UpdateCompanyCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        // На уровне контроллера защищаем инвариант: ID из URL должен строго совпадать с ID в команде DTO
        if (command.companyId.toString != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID must match JSON entity body identifier")))
        } else {
          updateCompanyUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/companies/:id/deposit
   * Top-up company financial balance through billing integration
   */
  def deposit(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[DepositFundsCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.companyId != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path ID mismatch")))
        } else {
          depositFundsUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/companies/:id/deduct
   * Manual or automated fund deduction/withdrawal transaction from company ledger account
   */
  def deduct(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[DeductFundsCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.companyId != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path ID mismatch")))
        } else {
          deductFundsUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/companies/:id/activate
   * Activates suspended or newly registered company accounts
   */
  def activate(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[ActivateCompanyCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.companyId.toString != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path ID mismatch")))
        } else {
          activateCompanyUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/companies/:id/suspend
   * Manually blocks or suspends company operations
   */
  def suspend(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[SuspendCompanyCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.companyId.toString != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path ID mismatch")))
        } else {
          suspendCompanyUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * DELETE /api/v1/companies/:id
   * Executes a soft-deletion scenario for a B2B company account
   */
  def delete(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[DeleteCompanyCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.companyId.toString != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path ID mismatch")))
        } else {
          deleteCompanyUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }
}

