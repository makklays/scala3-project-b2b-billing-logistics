package com.techmatrix18.cargo_balances.infrastructure.http

import com.techmatrix18.cargo_balances.application.in.*
import com.techmatrix18.cargo_balances.infrastructure.http.CargoBalanceJsonFormats.given // Импортируем given-форматы Scala 3
import play.api.mvc.*
import play.api.libs.json.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * CargoBalanceController - Driving HTTP Adapter for WMS Warehouse Cargo Balances Domain.
 * Оркестрирует управление товарными остатками и движение палет по ячейкам хранения.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class CargoBalanceController @Inject()(
  val controllerComponents: ControllerComponents,
  idempotencyAction: IdempotencyAction,                    // проверка idempotency
  initializeUseCase: InitializeCargoBalanceUseCase,
  addPayloadUseCase: AddCargoPayloadUseCase,
  removePayloadUseCase: RemoveCargoPayloadUseCase
)(using ec: ExecutionContext) extends BaseController {

  /**
   * POST /api/v1/cargo-balances
   * Initializes a new stock tracking cell for a B2B client in a specific section
   */
  def initialize(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[InitializeCargoBalanceCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        initializeUseCase.execute(command).map {
          case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
          case Right(response)     => Created(Json.toJson(response))
        }
    }
  }

  /**
   * POST /api/v1/cargo-balances/:id/add
   * Registers arrival and placing of cargo pallets to an existing balance account
   */
  def addPayload(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[AddCargoPayloadCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        // Защитная проверка: ID в строке URL-запроса должен строго совпадать с ID сущности в JSON
        if (command.balanceId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          addPayloadUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/cargo-balances/:id/remove
   * Registers shipment and removal of cargo pallets from warehouse slot balance
   */
  def removePayload(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[RemoveCargoPayloadCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        if (command.balanceId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          removePayloadUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }
}

