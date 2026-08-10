package com.techmatrix18.gates.infrastructure.db

import com.techmatrix18.gates.application.out.{GateRepository, GateFilter}
import com.techmatrix18.gates.domain.{Gate, GateId, GateStatus, GateType, WorkingHours}
import com.techmatrix18.gates.domain.GateId.*
import com.techmatrix18.hubs.domain.HubId
import com.techmatrix18.hubs.domain.HubId.*
import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * PostgresGateRepository - Реализация порта вывода для ворот склада (Anorm SQL)
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 06.08.2026
 */

@Singleton
class PostgresGateRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends GateRepository {

  override def findById(gateId: GateId): Future[Option[Gate]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT id, hub_id as hubId, gate_number as gateNumber, gate_type as gateType, status,
               working_hours as workingHours, hourly_rate as hourlyRate,
               overtime_hourly_rate as overtimeHourlyRate, created_at as createdAt, updated_at as updatedAt
        FROM gates
        WHERE id = ${gateId.value}::uuid
      """.as(GateRow.parser.singleOpt).map(_.toDomain)
    }
  }

  override def create(gate: Gate): Future[GateId] = Future {
    val row = GateRow.fromDomain(gate)
    db.withConnection { implicit connection =>
      SQL"""
          INSERT INTO gates (
            id, hub_id, gate_number, gate_type, status,
            working_hours, hourly_rate, overtime_hourly_rate, created_at, updated_at
          ) VALUES (
            ${row.id}::uuid,
            ${row.hubId}::uuid,
            ${row.gateNumber},
            ${row.gateType},
            ${row.status},
            ${row.workingHours},
            ${row.hourlyRate},
            ${row.overtimeHourlyRate},
            ${row.createdAt},
            ${row.updatedAt}
          )
        """.executeInsert()

      gate.id
    }
  }

  override def update(gate: Gate): Future[Unit] = Future {
    val row = GateRow.fromDomain(gate)
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE gates
          SET gate_number = ${row.gateNumber},
              status = ${row.status},
              working_hours = ${row.workingHours},
              hourly_rate = ${row.hourlyRate},
              overtime_hourly_rate = ${row.overtimeHourlyRate},
              updated_at = ${row.updatedAt}
          WHERE id = ${row.id}::uuid
        """.executeUpdate()
      () // Возвращаем Unit
    }
  }

  override def delete(gateId: GateId): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          DELETE FROM gates
          WHERE id = ${gateId.value}::uuid
        """.executeUpdate()
      ()
    }
  }

  override def findByFilter(filter: GateFilter): Future[List[Gate]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, hub_id as hubId, gate_number as gateNumber, gate_type as gateType, status,
                 working_hours as workingHours, hourly_rate as hourlyRate,
                 overtime_hourly_rate as overtimeHourlyRate, created_at as createdAt, updated_at as updatedAt
          FROM gates
          LIMIT 100
        """.as(GateRow.parser.*).map(_.toDomain)
    }
  }
}

