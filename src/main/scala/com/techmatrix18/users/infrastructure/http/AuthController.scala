package com.techmatrix18.users.infrastructure.http

import com.techmatrix18.users.application.in.{LoginCommand, RefreshTokenCommand}
import com.techmatrix18.users.application.service.{LoginUseCase, RefreshTokenUseCase}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, JsError, JsSuccess, OFormat}
import play.api.mvc.{AbstractController, ControllerComponents, Action, AnyContent, Request}
import scala.concurrent.{ExecutionContext, Future}

// Вспомогательные DTO для десериализации входящих JSON-запросов
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
 */

@Singleton
class AuthController @Inject()(
  cc: ControllerComponents,
  loginUseCase: LoginUseCase,
  refreshTokenUseCase: RefreshTokenUseCase
)(using ec: ExecutionContext) extends AbstractController(cc) {

  /**
   * Вспомогательный метод для извлечения реального IP-адреса клиента.
   * Учитывает заголовки прокси-серверов (Nginx, Cloudflare), защищая от подмены IP.
   */
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
   * Аутентификация пользователя по логину/паролю
   */
  def login(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Отсутствует тело запроса в формате JSON")))

      case Some(json) =>
        json.validate[LoginRequest] match {
          case JsError(errors) =>
            Future.successful(BadRequest(Json.obj(
              "error" -> "Неверный формат JSON",
              "details" -> JsError.toJson(errors)
            )))

          case JsSuccess(req, _) =>
            val (ip, ua) = extractClientMetadata(request)
            val command = LoginCommand(
              usernameOrEmail = req.usernameOrEmail,
              passwordRaw = req.passwordRaw,
              ipAddress = ip,
              userAgent = ua
            )

            loginUseCase.execute(command).map {
              case Left(errorMsg) =>
                Unauthorized(Json.obj("error" -> errorMsg))

              case Right(tokensResult) =>
                Ok(Json.toJson(tokensResult)) // Автоматически сериализует благодаря нашему AuthTokensResult.format
            }
        }
    }
  }

  /**
   * POST /api/v1/auth/refresh
   * Безопасное обновление пары токенов (Refresh Token Rotation)
   */
  def refresh(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Отсутствует тело запроса в формате JSON")))

      case Some(json) =>
        json.validate[RefreshRequest] match {
          case JsError(errors) =>
            Future.successful(BadRequest(Json.obj(
              "error" -> "Неверный формат JSON",
              "details" -> JsError.toJson(errors)
            )))

          case JsSuccess(req, _) =>
            val (ip, ua) = extractClientMetadata(request)
            val command = RefreshTokenCommand(
              refreshToken = req.refreshToken,
              ipAddress = ip,
              userAgent = ua
            )

            refreshTokenUseCase.execute(command).map {
              case Left(errorMsg) =>
                // Возвращаем Forbidden (403), если сработал триггер компрометации (Reuse Detection) или токен просрочен
                Forbidden(Json.obj("error" -> errorMsg))

              case Right(tokensResult) =>
                Ok(Json.toJson(tokensResult))
            }
        }
    }
  }
}

