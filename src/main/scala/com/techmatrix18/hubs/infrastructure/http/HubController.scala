package com.techmatrix18.hubs.infrastructure.http

import com.techmatrix18.hubs.application.in.*
import com.techmatrix18.hubs.infrastructure.http.HubJsonFormats.given
import play.api.libs.json.*
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * HubController - Driving HTTP Adapter for Logistics Hubs Domain.
 * Оркестрирует координацию распределительных центров, терминалов и их GPS-позиционирование.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class HubController @Inject()(
  val controllerComponents: ControllerComponents,
  createHubUseCase: CreateHubUseCase,
  updateHubProfileUseCase: UpdateHubProfileUseCase,
  updateHubGpsCoordinatesUseCase: UpdateHubGpsCoordinatesUseCase,
  activateHubUseCase: ActivateHubUseCase,
  putHubUnderMaintenanceUseCase: PutHubUnderMaintenanceUseCase,
  suspendHubUseCase: SuspendHubUseCase,
  deleteHubUseCase: DeleteHubUseCase
)(using ec: ExecutionContext) extends BaseController {

  /**
   * POST /api/v1/hubs
   * Registers a new logistical hub/terminal in the platform
   */
  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[HubCompanyCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        createHubUseCase.execute(command).map {
          case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
          case Right(response)     => Created(Json.toJson(response))
        }
    }
  }

  /**
   * PUT /api/v1/hubs/:id/profile
   * Updates hub metadata profiles (title, postal addresses)
   */
  def updateProfile(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[UpdateHubProfileCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.hubId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path param ID mismatch")))
        } else {
          updateHubProfileUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * PUT /api/v1/hubs/:id/gps
   * Updates hub geographical telemetry coordinates (latitude & longitude)
   */
  def updateGps(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[UpdateHubGpsCoordinatesCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.hubId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path param ID mismatch")))
        } else {
          updateHubGpsCoordinatesUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/hubs/:id/activate
   * Re-activates suspended or newly built hubs
   */
  def activate(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[ActivateHubCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.hubId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path param ID mismatch")))
        } else {
          activateHubUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/hubs/:id/maintenance
   * Urgently shuts down hub routing for hardware engineering or repairs
   */
  def putUnderMaintenance(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[PutHubUnderMaintenanceCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.hubId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path param ID mismatch")))
        } else {
          putHubUnderMaintenanceUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/hubs/:id/suspend
   * Suspends hub activity (e.g. for global company account lock)
   */
  def suspend(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[SuspendHubCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.hubId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path param ID mismatch")))
        } else {
          suspendHubUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * DELETE /api/v1/hubs/:id
   * Soft-deletes a logistics hub terminal from system configurations
   */
  def delete(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[DeleteHubCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.hubId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path param ID mismatch")))
        } else {
          deleteHubUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }
}

