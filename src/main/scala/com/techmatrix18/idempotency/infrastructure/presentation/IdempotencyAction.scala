package com.techmatrix18.idempotency.infrastructure.presentation

import com.techmatrix18.idempotency.application.out.IdempotencyRepository
import com.techmatrix18.idempotency.domain.{IdempotencyKey, IdempotencyStatus}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.libs.streams.Accumulator
import org.apache.pekko.util.ByteString // Если используется Pekko, замените на org.apache.pekko.util.ByteString
import java.security.MessageDigest
import scala.concurrent.{ExecutionContext, Future}
import org.slf4j.MDC
import org.apache.pekko.stream.Materializer

/**
 * IdempotencyAction - Кастомный Action Refiner для Play Framework.
 * Атомарно защищает эндпоинты контроллеров от повторных сетевых вызовов.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 09.08.2026
 */

@Singleton
class IdempotencyAction @Inject()(
  repository: IdempotencyRepository,      // Проверка idempotency
  val parser: BodyParsers.Default
)(implicit ec: ExecutionContext, mat: Materializer) extends ActionBuilder[Request, AnyContent] {

  private def calculateHash(body: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(body.getBytes("UTF-8"))
    hashBytes.map("%02x".format(_)).mkString
  }

  // Основной метод перехвата жизненного цикла HTTP-запроса
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    request.headers.get("X-Idempotency-Key") match {
      // Если заголовка нет — просто пропускаем запрос без защиты (или возвращаем ошибку, если заголовок обязателен)
      case None => block(request)

      case Some(rawKey) =>
        val key = IdempotencyKey(rawKey)

        // 1. Помещаем ключ идемпотентности в диагностический контекст логирования
        MDC.put("traceId", key.raw)

        try {
          val bodyStr = request.body match {
            case anyContent: AnyContent => anyContent.asJson.map(_.toString).getOrElse(anyContent.toString)
            case other => other.toString
          }
          val currentHash = calculateHash(bodyStr)

          // Атомарный барьер в БД
          repository.findOrInsert(key, currentHash).flatMap {

            // ОБНАРУЖЕН ДУБЛИКАТ ЗАПРОСА: Возвращаем закешированный или конфликтный HTTP-ответ
            case Left(existingRecord) =>
              if (existingRecord.requestPayloadHash != currentHash) {
                Future.successful(Results.BadRequest(Json.obj(
                  "error" -> "Idempotency Conflict",
                  "details" -> "Ключ уже использован, но тело текущего запроса изменено"
                )))

              } else {
                existingRecord.status match {
                  case IdempotencyStatus.Completed =>
                    val code = existingRecord.responseCode.getOrElse(200)
                    val body = existingRecord.responseBody.getOrElse("{}")
                    Future.successful(Results.Status(code)(Json.parse(body)))

                  case IdempotencyStatus.Started | IdempotencyStatus.Processing =>
                    Future.successful(Results.Status(409)(Json.obj(
                      "error" -> "Conflict",
                      "details" -> "Запрос обрабатывается параллельным потоком. Пожалуйста, подождите"
                    )))

                  case IdempotencyStatus.Failed =>
                    // Разрешаем повторную попытку при предыдущем краше
                    runAndCache(key, request, block)
                }
              }

            // ПЕРВИЧНЫЙ ЗАПРОС: Выполняем контроллер и кешируем результат
            case Right(_) =>
              runAndCache(key, request, block)
          }
        } finally {
          // 2. Обязательно очищаем контекст после завершения потока, чтобы не отравить другие запросы
          MDC.remove("traceId")
        }
    }
  }

  // Выполняет блок контроллера, асинхронно вычитывает тело ответа и сохраняет его в БД.
  private def runAndCache[A](key: IdempotencyKey, request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    block(request).flatMap { result =>
      // Извлекаем строковый JSON-контент из тела ответа Play Result
      result.body.consumeData.flatMap { byteString =>
        val responseBodyStr = byteString.utf8String
        val responseStatus = result.header.status

        if (responseStatus >= 200 && responseStatus < 300) {
          // Запрос успешный (2xx) — переводим в COMPLETED и кешируем JSON-ответ
          repository.complete(key, responseStatus, responseBodyStr).map(_ => result)
        } else {
          // Ошибка бизнес-логики или авторизации — переводим в FAILED, чтобы клиент мог исправить данные и повторить
          repository.markAsFailed(key).map(_ => result)
        }
      }.recoverWith { case _ =>
        // При системном сбое стрима разбора тела помечаем ключ как FAILED
        repository.markAsFailed(key).map(_ => result)
      }
    }.recoverWith { case ex =>
      // Контроллер упал с необработанным исключением (Exception)
      repository.markAsFailed(key).flatMap(_ => Future.failed(ex))
    }
  }
}

