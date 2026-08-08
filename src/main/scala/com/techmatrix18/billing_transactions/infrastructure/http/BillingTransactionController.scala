package com.techmatrix18.billing_transactions.infrastructure.http

import com.techmatrix18.billing_transactions.application.out.{BillingTransactionRepository, BillingTransactionFilter}
import com.techmatrix18.billing_transactions.domain.BillingTransactionId
import com.techmatrix18.companies.domain.CompanyId
import com.techmatrix18.billing_transactions.infrastructure.http.BillingTransactionJsonFormats.given // Импортируем given-форматы Scala 3
import play.api.mvc.*
import play.api.libs.json.*
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * BillingTransactionController - Driving HTTP Adapter for Read-Only Financial Ledger Audit Trail.
 * Предоставляет внешнее API для выгрузки отчетов, налоговых аудит-проверок и бухгалтерских сверк.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class BillingTransactionController @Inject()(
  val controllerComponents: ControllerComponents,
  transactionRepository: BillingTransactionRepository,   // Напрямую читаем из репозитория для Read-Only эндпоинтов
  idempotencyAction: IdempotencyAction                   // проверка idempotency
)(using ec: ExecutionContext) extends BaseController {

  /**
   * GET /api/v1/billing-transactions
   * Retrieves a filtered list of historical billing transactions (deposits / deductions)
   */
  def list(): Action[AnyContent] = Action.async { request =>
    // Извлекаем Query-параметры из строки URL-запроса для сборки гибкого финансового фильтра
    val companyIdStrOpt = request.queryString.get("companyId").flatMap(_.headOption)
    val categoryOpt = request.queryString.get("category").flatMap(_.headOption)
    val currencyOpt = request.queryString.get("currency").flatMap(_.headOption)

    // Преобразуем строковой ID компании в строгий доменный CompanyId, если он передан
    val resolvedCompanyId = companyIdStrOpt.map(idStr => CompanyId(UUID.fromString(idStr)))

    // Упаковываем параметры в строго типизированный DTO-фильтр прикладного уровня
    val filter = BillingTransactionFilter(
      companyId = resolvedCompanyId,
      category = categoryOpt.map(_.trim.toUpperCase),
      currency = currencyOpt.map(_.trim.toUpperCase),
      sourceId = None, // При необходимости парсится аналогично из UUID
      fromDate = None,
      toDate = None
    )

    // Асинхронно выкачиваем лог финансовых транзакций из PostgreSQL
    transactionRepository.findByFilter(filter).map { transactions =>
      Ok(Json.toJson(transactions))
    }.recover {
      case error: Exception =>
        InternalServerError(Json.obj(
          "status" -> "Error",
          "message" -> s"Failed to query financial billing transaction ledger: ${error.getMessage}"
        ))
    }
  }

  /**
   * GET /api/v1/billing-transactions/:id
   * Retrieves details of a specific immutable financial transaction by its ID
   */
  def getById(id: String): Action[AnyContent] = Action.async { _ =>
    val targetTransactionId = BillingTransactionId(id)

    transactionRepository.findById(targetTransactionId).map {
      case None =>
        NotFound(Json.obj(
          "status" -> "Fail",
          "message" -> s"Financial ledger transaction with ID '$id' not found"
        ))
      case Some(transaction) =>
        Ok(Json.toJson(transaction))
    }.recover {
      case error: Exception =>
        InternalServerError(Json.obj(
          "status" -> "Error",
          "message" -> s"Financial infrastructure read failure: ${error.getMessage}"
        ))
    }
  }
}

