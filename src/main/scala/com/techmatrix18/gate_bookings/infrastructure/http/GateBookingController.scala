package com.techmatrix18.gate_bookings.infrastructure.http

import com.techmatrix18.gate_bookings.application.in.*
import com.techmatrix18.gate_bookings.infrastructure.http.GateBookingJsonFormats.given // Импортируем given-форматы Scala 3
import play.api.mvc.*
import play.api.libs.json.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * GateBookingController - Driving HTTP Adapter for Transport Slot Bookings Context.
 * Оркестрирует резервирование окон разгрузки, интеграцию с датчиками КПП/IoT и финтех-штрафы за неявку.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class GateBookingController @Inject()(
  val controllerComponents: ControllerComponents,
  idempotencyAction: IdempotencyAction,                    // Проверка idempotency
  createBookingUseCase: CreateGateBookingUseCase,
  arriveTruckUseCase: ArriveTruckUseCase,
  departTruckUseCase: DepartTruckUseCase,
  cancelBookingUseCase: CancelGateBookingUseCase,
  rescheduleBookingUseCase: RescheduleGateBookingUseCase,
  markAsNoShowUseCase: MarkAsNoShowUseCase
)(using ec: ExecutionContext) extends BaseController {

  /**
   * POST /api/v1/bookings
   * Schedules and locks a new transport dock slot for a B2B client
   */
  def create(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CreateGateBookingCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        createBookingUseCase.execute(command).map {
          case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
          case Right(response)     => Created(Json.toJson(response))
        }
    }
  }

  /**
   * POST /api/v1/bookings/:id/arrive
   * Triggered by checkpoint IoT / camera to register actual truck arrival
   */
  def arriveTruck(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[ArriveTruckCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        // Защитная проверка: ID из URL-строки запроса должен строго совпадать с ID сущности в JSON
        if (command.bookingId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          arriveTruckUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/bookings/:id/depart
   * Triggered by WMS weight-bridge / sensors to close booking log session
   */
  def departTruck(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[DepartTruckCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.bookingId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          departTruckUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/bookings/:id/cancel
   * Cancels a planned slot registration early
   */
  def cancel(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CancelGateBookingCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.bookingId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          cancelBookingUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/bookings/:id/reschedule
   * Updates planned execution time boundaries for upcoming sessions
   */
  def reschedule(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[RescheduleGateBookingCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.bookingId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          rescheduleBookingUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }

  /**
   * POST /api/v1/bookings/:id/no-show
   * Automatically triggered by cron schedulers to process expired reservations and issue penalties
   */
  def markAsNoShow(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[MarkAsNoShowCommand] match {
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Invalid JSON payload", "details" -> JsError.toJson(errors))))
      case JsSuccess(command, _) =>
        if (command.bookingId.value != id) {
          Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> "Path parameter ID mismatch")))
        } else {
          markAsNoShowUseCase.execute(command).map {
            case Left(businessError) => BadRequest(Json.obj("status" -> "Fail", "message" -> businessError))
            case Right(response)     => Ok(Json.toJson(response))
          }
        }
    }
  }
}

