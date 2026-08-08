package com.techmatrix18.gates.infrastructure.delivery.http

import com.techmatrix18.gates.application.in.*
import com.techmatrix18.gates.infrastructure.delivery.http.GateJsonFormats.given // Импортируем given-форматы Scala 3
import play.api.mvc.*
import play.api.libs.json.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * GateController - Driving HTTP Adapter for Warehouse Loading Gates Domain.
 * Оркестрирует управление погрузочными доками, автоматизацию тарифов простоя и IoT-движение транспорта.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class GateController @Inject()(
  val controllerComponents: ControllerComponents,
  idempotencyAction: IdempotencyAction,                            // Проверка idempotency
  createGateUseCase: CreateGateUseCase,
  updateConfigurationUseCase: UpdateGateConfigurationUseCase,
  updateRatesUseCase: UpdateGateRatesUseCase,
  occupyGateUseCase: OccupyGateUseCase,
  releaseGateUseCase: ReleaseGateUseCase,
  putUnderMaintenanceUseCase: PutGateUnderMaintenanceUseCase,
  activateGateUseCase: ActivateGateUseCase,
  deleteGateUseCase: DeleteGateUseCase
)(using ec: ExecutionContext) extends BaseController {

  /**
   * POST /api/v1/gates
   * Registers a new warehouse loading gate in the system
   */
  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CreateGateCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        createGateUseCase.execute(command).map {
          case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
          case Right(response)     => Created(Json.toJson(response))
        }
    }
  }

  /**
   * PUT /api/v1/gates/:id/configuration
   * Updates physical configuration parameters (gate number, working hours)
   */
  def updateConfiguration(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[UpdateGateConfigurationCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.gateId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          updateConfigurationUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * PUT /api/v1/gates/:id/rates
   * Modifies commercial billing rates and overtime penalties for gate renting
   */
  def updateRates(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[UpdateGateRatesCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.gateId.value != id) {
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
   * POST /api/v1/gates/:id/occupy
   * Signals that a truck has physically arrived and blocked the loading dock
   */
  def occupy(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[OccupyGateCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.gateId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          occupyGateUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/gates/:id/release
   * Signals truck departure, stops billing timers, and processes cross-domain transaction deductions
   */
  def release(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[ReleaseGateCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.gateId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          releaseGateUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/gates/:id/maintenance
   * Moves a gate out of active routing for urgent infrastructure technical repair
   */
  def putUnderMaintenance(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[PutGateUnderMaintenanceCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.gateId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          putUnderMaintenanceUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/gates/:id/activate
   * Activates gate back to operational status after structural maintenance is complete
   */
  def activate(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[ActivateGateCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.gateId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          activateGateUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * DELETE /api/v1/gates/:id
   * Triggers a soft-decommissioning scenario for a gate entity
   */
  def delete(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[DeleteGateCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.gateId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          deleteGateUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }
}

