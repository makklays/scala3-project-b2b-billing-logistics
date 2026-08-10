package com.techmatrix18.cargo_transactions.infrastructure.http

import com.techmatrix18.cargo_transactions.application.out.{CargoTransactionRepository, CargoTransactionFilter}
import com.techmatrix18.cargo_transactions.domain.CargoTransactionId
import com.techmatrix18.cargo_transactions.infrastructure.http.CargoTransactionJsonFormats.*
import com.techmatrix18.cargo_transactions.infrastructure.http.CargoTransactionJsonFormats.given // Импортируем given-форматы Scala 3
import play.api.mvc.*
import play.api.libs.json.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import com.techmatrix18.idempotency.infrastructure.presentation.IdempotencyAction

/**
 * CargoTransactionController - Driving HTTP Adapter for Read-Only Cargo Ledger Audit Trail.
 * Предоставляет внешнее API для выгрузки отчетов и исторической сверки движения палет.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class CargoTransactionController @Inject()(
  val controllerComponents: ControllerComponents,
  idempotencyAction: IdempotencyAction,                    // Проверка idempotency
  transactionRepository: CargoTransactionRepository        // Напрямую читаем из репозитория для Read-Only эндпоинтов
)(using ec: ExecutionContext) extends BaseController {

  /**
   * GET /api/v1/cargo-transactions
   * Retrieves a filtered list of historical warehouse pallet movements (Supply / Dispatch)
   */
  def list(): Action[AnyContent] = Action.async { request =>
    // Извлекаем Query-параметры из строки URL-запроса для сборки гибкого фильтра
    val clientNameOpt = request.queryString.get("clientName").flatMap(_.headOption)
    val operationTypeStr = request.queryString.get("operationType").flatMap(_.headOption)

    // Упаковываем параметры в строго типизированный DTO-фильтр прикладного уровня
    val filter = CargoTransactionFilter(
      hubSectionId = None, // При необходимости парсится аналогично
      gateBookingId = None,
      clientName = clientNameOpt,
      operationType = None, // Раскручивается через Enum-матчинг при глубокой фильтрации
      fromDate = None,
      toDate = None
    )

    // Асинхронно выкачиваем лог транзакций из PostgreSQL
    transactionRepository.findByFilter(filter).map { transactions =>
      Ok(Json.toJson(transactions))
    }.recover {
      case error: Exception =>
        InternalServerError(Json.obj(
          "status" -> "Error",
          "message" -> s"Failed to query cargo transaction ledger: ${error.getMessage}"
        ))
    }
  }

  /**
   * GET /api/v1/cargo-transactions/:id
   * Retrieves details of a specific immutable ledger transaction by its ID
   */
  def getById(id: String): Action[AnyContent] = Action.async { _ =>
    val targetTransactionId = CargoTransactionId(id)

    transactionRepository.findById(targetTransactionId).map {
      case None =>
        NotFound(Json.obj(
          "status" -> "Fail",
          "message" -> s"Cargo ledger transaction with ID '$id' not found"
        ))
      case Some(transaction) =>
        Ok(Json.toJson(transaction))
    }.recover {
      case error: Exception =>
        InternalServerError(Json.obj(
          "status" -> "Error",
          "message" -> s"Infrastructure read failure: ${error.getMessage}"
        ))
    }
  }
}

