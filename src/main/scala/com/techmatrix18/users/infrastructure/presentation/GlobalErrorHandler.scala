package com.techmatrix18.users.infrastructure.presentation

import com.techmatrix18.users.presentation.dto.AuthErrorResponse
import com.techmatrix18.users.presentation.dto.AuthJsonFormats.given // Импортируем Scala 3 given-форматы
import javax.inject.{Inject, Provider, Singleton}
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.{Environment, Logger, Mode}
import scala.concurrent.Future

/**
 * GlobalErrorHandler - Перехватчик всех необработанных исключений и ошибок маршрутизации.
 * Гарантирует отдачу строго типизированного JSON в любых аварийных ситуациях.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 09.08.2026
 */

@Singleton
class GlobalErrorHandler @Inject()(
  env: Environment
) extends HttpErrorHandler {

  private val logger = Logger(this.getClass)

  // Вызывается, когда произошла ошибка на уровне фреймворка (например, 404 Not Found или 400 Bad Request)
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    logger.warn(s"[HTTP Client Error] Status: $statusCode, Path: ${request.path}, Message: $message")

    val response = statusCode match {
      case play.api.http.Status.NOT_FOUND =>
        AuthErrorResponse(error = "Ресурс не найден", details = Some(s"Эндпоинт ${request.method} ${request.path} отсутствует"))

      case play.api.http.Status.BAD_REQUEST =>
        AuthErrorResponse(error = "Некорректный запрос", details = Some(message))

      case play.api.http.Status.FORBIDDEN =>
        AuthErrorResponse(error = "Доступ запрещен", details = Some("У вас нет прав для выполнения этой операции"))

      case _ =>
        AuthErrorResponse(error = "Ошибка клиентского запроса", details = Some(message))
    }

    Future.successful(Results.Status(statusCode)(Json.toJson(response)))
  }

  // Вызывается, когда в коде (Use Cases, Репозитории, Контроллеры) вылетает необработанный Exception (например, SQLException)
  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    logger.error(s"[HTTP Server Error] Критический краш на пути: ${request.method} ${request.path}", exception)

    // В режиме разработки (Dev) полезно видеть девелоперский трейс. В Prod — детали скрываются ради безопасности.
    val details = if (env.mode == Mode.Dev) {
      Some(s"${exception.getClass.getName}: ${exception.getMessage}")
    } else {
      Some("Пожалуйста, обратитесь в службу поддержки или проверьте идентификатор запроса")
    }

    val response = AuthErrorResponse(
      error = "Внутренняя ошибка сервера",
      details = details
    )

    Future.successful(Results.InternalServerError(Json.toJson(response)))
  }
}

