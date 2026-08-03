package com.techmatrix18.companies.application.out

import com.techmatrix18.companies.domain.{Company, CompanyId}
import scala.concurrent.Future

/**
 * CompanRepository - В интерфейсе репозитория (порте) мы также переходим на CompanyId
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */

trait CompanyRepository {

  // Поиск компании по её строгому типобезопасному доменному ID
  def findById(id: CompanyId): Future[Option[Company]]

  // Обновление состояния компании (например, изменение баланса при биллинге)
  def update(company: Company): Future[Unit]

  // Регистрация новой компании на B2B-платформе с возвратом созданного ID
  def create(company: Company): Future[CompanyId]
}

