package com.techmatrix18.section_tariffs.infrastructure.http

import com.techmatrix18.section_tariffs.application.in.*
import com.techmatrix18.section_tariffs.infrastructure.http.SectionTariffJsonFormats.given // Импортируем given-форматы Scala 3
import play.api.mvc.*
import play.api.libs.json.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * SectionTariffController - Driving HTTP Adapter for Financial Section Tariffs Domain.
 * Оркестрирует коммерческое управление тарифной сеткой, контрактами и стоимостью хранения палет.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class SectionTariffController @Inject()(
  val controllerComponents: ControllerComponents,
  idempotencyAction: IdempotencyAction,                          // Проверка idempotency
  createTariffUseCase: CreateSectionTariffUseCase,
  updateRatesUseCase: UpdateSectionTariffRatesUseCase,
  extendValidityUseCase: ExtendSectionTariffValidityUseCase,
  cancelTariffUseCase: CancelSectionTariffUseCase
)(using ec: ExecutionContext) extends BaseController {

  /**
   * POST /api/v1/tariffs
   * Assigns and registers a new custom storage tariff for a B2B client
   */
  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CreateSectionTariffCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        createTariffUseCase.execute(command).map {
          case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
          case Right(response)     => Created(Json.toJson(response))
        }
    }
  }

  /**
   * PUT /api/v1/tariffs/:id/rates
   * Modifies commercial billing rates (occupied space and empty reservation) for an active contract
   */
  def updateRates(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[UpdateSectionTariffRatesCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        // Защитная проверка: ID в строке URL-запроса должен строго совпадать с ID сущности в JSON
        if (command.tariffId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          updateRatesUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/tariffs/:id/extend
   * Extends the contract expiration date (validTo limit) for an existing custom tariff
   */
  def extendValidity(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[ExtendSectionTariffValidityCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        if (command.tariffId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          extendValidityUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/tariffs/:id/cancel
   * Early cancellation of a tariff contract by resetting validTo to Instant.now() without physical deletion
   */
  def cancel(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CancelSectionTariffCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        if (command.tariffId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          cancelTariffUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }
}

