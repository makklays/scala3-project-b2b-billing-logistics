package com.techmatrix18.gates.infrastructure.db

import com.techmatrix18.gates.application.out.{GateRepository, GateFilter}
import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus, GateType, WorkingHours}
import com.techmatrix18.hubs.domain.HubId
import com.techmatrix18.hubs.domain.HubId.*
import com.techmatrix18.gates.domain.GateId.*
import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * GateRow - Структура строки таблицы gates для работы с Anorm.
 * Полностью изолирует инфраструктурный слой от доменных моделей.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

case class GateRow(
  id: UUID,
  hubId: UUID,
  gateNumber: String,
  gateType: String,
  status: String,
  workingHours: String, // В БД пишется как плоская строка "HH:MM-HH:MM"
  hourlyRate: BigDecimal,
  overtimeHourlyRate: BigDecimal,
  createdAt: Instant,
  updatedAt: Instant
) {
  // Конвертация строки базы данных в чистый доменный агрегат (DDD)
  def toDomain: Gate = {
    // Парсим рабочие часы из формата "08:00-20:00"
    val hoursArray = workingHours.split("-")
    val parsedWorkingHours = if (hoursArray.length == 2) {
      WorkingHours(from = hoursArray(0), to = hoursArray(1))
    } else {
      WorkingHours(from = "00:00", to = "23:59") // Дефолтный фолбэк
    }

    Gate(
      id = GateId(id.toString),
      hubId = HubId(hubId.toString),
      gateNumber = gateNumber,
      gateType = GateType.values.find(_.code == gateType).getOrElse(GateType.Dry),
      status = GateStatus.values.find(_.code == status).getOrElse(GateStatus.Available),
      workingHours = parsedWorkingHours,
      hourlyRate = hourlyRate,
      overtimeHourlyRate = overtimeHourlyRate,
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }
}

object GateRow {
  // Anorm-парсер для автоматической сборки структуры GateRow из SQL-ответа
  val parser: RowParser[GateRow] = {
    get[UUID]("id") ~
    get[UUID]("hub_id") ~
    get[String]("gate_number") ~
    get[String]("gate_type") ~
    get[String]("status") ~
    get[String]("working_hours") ~
    get[BigDecimal]("hourly_rate") ~
    get[BigDecimal]("overtime_hourly_rate") ~
    get[Instant]("created_at") ~
    get[Instant]("updated_at") map {
      case id ~ hubId ~ gateNumber ~ gateType ~ status ~ workingHours ~ hourlyRate ~ overtimeHourlyRate ~ createdAt ~ updatedAt =>
        GateRow(id, hubId, gateNumber, gateType, status, workingHours, hourlyRate, overtimeHourlyRate, createdAt, updatedAt)
    }
  }

  // Сборка строки БД из иммутабельного доменного объекта перед записью в Postgres
  def fromDomain(gate: Gate): GateRow = GateRow(
    id = UUID.fromString(gate.id.value), // Используем метод расширения .raw для получения UUID ворот
    hubId = UUID.fromString(gate.hubId.value), // Метод расширения .raw для получения UUID хаба
    gateNumber = gate.gateNumber,
    gateType = gate.gateType.code,
    status = gate.status.code,
    workingHours = s"${gate.workingHours.from}-${gate.workingHours.to}", // Склеиваем объект в строку для БД
    hourlyRate = gate.hourlyRate,
    overtimeHourlyRate = gate.overtimeHourlyRate,
    createdAt = gate.createdAt,
    updatedAt = gate.updatedAt
  )
}

