package com.techmatrix18.hub_sections.infrastructure.http

import com.techmatrix18.hub_sections.application.in.*
import com.techmatrix18.hub_sections.infrastructure.http.HubSectionJsonFormats.given // Импортируем given-форматы Scala 3
import play.api.mvc.*
import play.api.libs.json.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * HubSectionController - Driving HTTP Adapter for Hub Warehouse Sections Domain.
 * Оркестрирует внутреннее зонирование, конфигурацию и емкость складских площадей хаба.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class HubSectionController @Inject()(
  val controllerComponents: ControllerComponents,
  idempotencyAction: IdempotencyAction,                                  // Проверка idempotency
  createHubSectionUseCase: CreateHubSectionUseCase,
  updateHubSectionCapacityUseCase: UpdateHubSectionCapacityUseCase,
  renameHubSectionUseCase: RenameHubSectionUseCase,
  deleteHubSectionUseCase: DeleteHubSectionUseCase
)(using ec: ExecutionContext) extends BaseController {

  /**
   * POST /api/v1/sections
   * Allocates and registers a new storage zone/section within a logistics hub
   */
  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CreateHubSectionCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        createHubSectionUseCase.execute(command).map {
          case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
          case Right(response)     => Created(Json.toJson(response))
        }
    }
  }

  /**
   * PUT /api/v1/sections/:id/capacity
   * Updates total storage capacity limit for an active warehouse section
   */
  def updateCapacity(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[UpdateHubSectionCapacityCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        // Защитная проверка: ID в строке URL-запроса должен строго совпадать с ID сущности в JSON
        if (command.sectionId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          updateHubSectionCapacityUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * PUT /api/v1/sections/:id/rename
   * Renames a specific hub warehouse section for commercial or operational purposes
   */
  def rename(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[RenameHubSectionCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        if (command.sectionId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          renameHubSectionUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * DELETE /api/v1/sections/:id
   * Soft-deletes / decommissions an internal warehouse section from active routing
   */
  def delete(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[DeleteHubSectionCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "status" -> "Error",
          "message" -> "Invalid JSON payload",
          "details" -> JsError.toJson(errors)
        )))
      case JsSuccess(command, _) =>
        if (command.sectionId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          deleteHubSectionUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }
}

