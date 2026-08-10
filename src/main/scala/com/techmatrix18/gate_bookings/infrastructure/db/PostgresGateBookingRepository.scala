package com.techmatrix18.gate_bookings.infrastructure.db

import com.techmatrix18.gate_bookings.application.out.{GateBookingRepository, GateBookingFilter}
import com.techmatrix18.gate_bookings.domain.{GateBooking, GateBookingId, GateBookingStatus}
import com.techmatrix18.gates.domain.GateId
import com.techmatrix18.gates.domain.GateId.*
import com.techmatrix18.companies.domain.CompanyId
import com.techmatrix18.companies.domain.CompanyId.*
import java.util.UUID
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database // Используем прямую зависимость Database, как в вашем примере
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * PostgresGateBookingRepository - Реализация Out-порта в вашем фирменном стиле Anorm
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.3
 * @since 06.08.2026
 */

@Singleton
class PostgresGateBookingRepository @Inject()(
  db: Database // Внедрение зависимости бд из вашего шаблона
)(using ec: ExecutionContext) extends GateBookingRepository {

  override def findById(bookingId: GateBookingId): Future[Option[GateBooking]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT id, gate_id as gateId, client_name as clientName, truck_license_plate as truckLicensePlate,
               scheduled_start_time as scheduledStartTime, scheduled_end_time as scheduledEndTime,
               actual_arrival_time as actualArrivalTime, actual_departure_time as actualDepartureTime,
               status, created_at as createdAt, updated_at as updatedAt
        FROM gate_bookings
        WHERE id = ${UUID.fromString(bookingId.value)}::uuid
      """.as(GateBookingRow.parser.singleOpt).map(_.toDomain)
    }
  }

  override def findActiveByGateId(gateId: GateId): Future[Option[GateBooking]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT id, gate_id as gateId, client_name as clientName, truck_license_plate as truckLicensePlate,
               scheduled_start_time as scheduledStartTime, scheduled_end_time as scheduledEndTime,
               actual_arrival_time as actualArrivalTime, actual_departure_time as actualDepartureTime,
               status, created_at as createdAt, updated_at as updatedAt
        FROM gate_bookings
        WHERE gate_id = ${gateId.value}::uuid AND status = 'IN_PROGRESS'
      """.as(GateBookingRow.parser.singleOpt).map(_.toDomain)
    }
  }

  override def create(booking: GateBooking): Future[GateBookingId] = Future {
    val row = GateBookingRow.fromDomain(booking)
    db.withConnection { implicit connection =>
      SQL"""
        INSERT INTO gate_bookings (
          id, gate_id, client_name, truck_license_plate,
          scheduled_start_time, scheduled_end_time, status, created_at, updated_at
        ) VALUES (
          ${row.id}::uuid,
          ${row.gateId}::uuid,
          ${row.clientName},
          ${row.truckLicensePlate},
          ${row.scheduledStartTime},
          ${row.scheduledEndTime},
          ${row.status},
          ${row.createdAt},
          ${row.updatedAt}
        )
      """.executeInsert()

      booking.id
    }
  }

  override def update(booking: GateBooking): Future[Unit] = Future {
    val row = GateBookingRow.fromDomain(booking)
    db.withConnection { implicit connection =>
      SQL"""
        UPDATE gate_bookings
        SET status = ${row.status},
            actual_arrival_time = ${row.actualArrivalTime},
            actual_departure_time = ${row.actualDepartureTime},
            updated_at = ${row.updatedAt}
        WHERE id = ${row.id}::uuid
      """.executeUpdate()
      () // Возвращаем Unit (void)
    }
  }

  override def delete(bookingId: GateBookingId): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        DELETE FROM gate_bookings
        WHERE id = ${UUID.fromString(bookingId.value)}::uuid
      """.executeUpdate()
      ()
    }
  }

  /**
   * ТОТ САМЫЙ КРИТИЧЕСКИЙ МЕТОД 3NF ДЛЯ ФИНАНСОВОГО ШТРАФОВАНИЯ
   * Быстрый точечный JOIN на уровне СУБД, возвращающий атомарный UUID владельца аккаунта
   *
   * Cуть: или использовать этот SQL запрос (может тормозить SQL запрос - проверить в продакшене)
   *       или добавлять companyId для сущности GateBooking (отказался, так как теряется целостность данных в БД)
   */
  override def getCompanyIdForBooking(bookingId: GateBookingId): Future[Option[CompanyId]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT h.company_id
        FROM gate_bookings gb
        JOIN gates g ON gb.gate_id = g.id
        JOIN hubs h ON g.hub_id = h.id
        WHERE gb.id = ${UUID.fromString(bookingId.value)}::uuid
      """
        .as(scalar[UUID].singleOpt) // Извлекаем исключительно одну ячейку типа UUID
        .map(companyUuid => CompanyId(companyUuid.toString)) // Заворачиваем обратно в непрозрачный opaque доменный тип
    }
  }

  override def findByFilter(filter: GateBookingFilter): Future[List[GateBooking]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM gate_bookings LIMIT 100"
        .as(GateBookingRow.parser.*).map(_.toDomain)
    }
  }
}

