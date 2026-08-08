package com.techmatrix18.users.infrastructure.http

import com.techmatrix18.users.application.in.{LoginCommand, RefreshTokenCommand}
import com.techmatrix18.users.application.service.{LoginUseCase, RefreshTokenUseCase}
import com.techmatrix18.idempotency.infrastructure.presentation.IdempotencyAction // Наш экшн-заслон
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, JsError, JsSuccess, OFormat}
import play.api.mvc.{AbstractController, ControllerComponents, Action, AnyContent, Request}
import scala.concurrent.{ExecutionContext, Future}

private case class LoginRequest(usernameOrEmail: String, passwordRaw: String)
private object LoginRequest { implicit val format: OFormat[LoginRequest] = Json.derived }

private case class RefreshRequest(refreshToken: String)
private object RefreshRequest { implicit val format: OFormat[RefreshRequest] = Json.derived }

/**
 * AuthController - Контроллер управления сессиями и JWT-токенами.
 * Обеспечивает безопасный разбор заголовков и потоков данных.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

@Singleton
class AuthController @Inject()(
  cc: ControllerComponents,
  idempotencyAction: IdempotencyAction, // Внедряем автоматический заслон
  loginUseCase: LoginUseCase,
  refreshTokenUseCase: RefreshTokenUseCase
)(using ec: ExecutionContext) extends AbstractController(cc) {

  private def extractClientMetadata(request: Request[?]): (Option[String], Option[String]) = {
    val ip = request.headers.get("X-Forwarded-For")
      .flatMap(_.split(",").headOption)
      .map(_.trim)
      .orElse(Some(request.remoteAddress))
    val userAgent = request.headers.get("User-Agent")
    (ip, userAgent)
  }

  /**
   * POST /api/v1/auth/login
   * Идемпотентная авторизация пользователя. Защищает финтех-платформу от лагов сети фронтенда.
   */
  def login(): Action[AnyContent] = idempotencyAction.async { implicit request =>
    request.body.asJson match {
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Отсутствует тело запроса в формате JSON")))

      case Some(json) =>
        json.validate[LoginRequest] match {
          case JsError(errors) =>
            Future.successful(BadRequest(Json.obj("error" -> "Неверный формат JSON", "details" -> JsError.toJson(errors))))

          case JsSuccess(req, _) =>
            val (ip, ua) = extractClientMetadata(request)
            val command = LoginCommand(req.usernameOrEmail, req.passwordRaw, ip, ua)

            loginUseCase.execute(command).map {
              case Left(errorMsg) => Unauthorized(Json.obj("error" -> errorMsg))
              case Right(tokensResult) => Ok(Json.toJson(tokensResult)) // Этот JSON автоматически запишется в БД
            }
        }
    }
  }

  /**
   * POST /api/v1/auth/refresh
   * Идемпотентное обновление сессии. Исключает инвалидацию токенов при двойном клике на клиенте.
   */
  def refresh(): Action[AnyContent] = idempotencyAction.async { implicit request =>
    request.body.asJson match {
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Отсутствует тело запроса в формате JSON")))

      case Some(json) =>
        json.validate[RefreshRequest] match {
          case JsError(errors) =>
            Future.successful(BadRequest(Json.obj("error" -> "Неверный формат JSON", "details" -> JsError.toJson(errors))))

          case JsSuccess(req, _) =>
            val (ip, ua) = extractClientMetadata(request)
            val command = RefreshTokenCommand(req.refreshToken, ip, ua)

            refreshTokenUseCase.execute(command).map {
              case Left(errorMsg) => Forbidden(Json.obj("error" -> errorMsg))
              case Right(tokensResult) => Ok(Json.toJson(tokensResult)) // Этот JSON автоматически запишется в БД
            }
        }
    }
  }
}

