package com.techmatrix18.users.infrastructure.db

import com.techmatrix18.users.application.out.UserRepository
import com.techmatrix18.users.domain.{User, UserId, UserRole}
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * PostgresUserRepository - Реализация порта вывода для сущности User
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

@Singleton
class PostgresUserRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends UserRepository {

  override def findById(id: UserId): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT id, username, email, role, mobile, gender, age, avatar, password,
               created_at as createdAt, updated_at as updatedAt
        FROM users
        WHERE id = ${id.raw}
      """.as(UserRow.parser.singleOpt).map(_.toDomain)
    }
  }

  override def findByEmail(email: String): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, username, email, role, mobile, gender, age, avatar, password,
                 created_at as createdAt, updated_at as updatedAt
          FROM users
          WHERE LOWER(email) = ${email.trim.toLowerCase}
        """.as(UserRow.parser.singleOpt).map(_.toDomain)
    }
  }

  override def findByUsername(username: String): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, username, email, role, mobile, gender, age, avatar, password,
                 created_at as createdAt, updated_at as updatedAt
          FROM users
          WHERE LOWER(username) = ${username.trim.toLowerCase}
        """.as(UserRow.parser.singleOpt).map(_.toDomain)
    }
  }

  override def create(user: User): Future[UserId] = Future {
    val row = UserRow.fromDomain(user)
    db.withConnection { implicit connection =>
      val generatedId =
        SQL"""
          INSERT INTO users (
            username, email, role, mobile, gender, age, avatar, password, created_at, updated_at
          ) VALUES (
            ${row.username},
            ${row.email},
            ${row.role},
            ${row.mobile},
            ${row.gender},
            ${row.age},
            ${row.avatar},
            ${row.password},
            ${row.createdAt},
            ${row.updatedAt}
          )
        """.executeInsert(scalar[Long].single) // Извлекаем сгенерированный базой автоинкрементный Long ID

      UserId(generatedId)
    }
  }

  override def update(user: User): Future[Unit] = Future {
    val row = UserRow.fromDomain(user)
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE users
          SET username = ${row.username},
              email = ${row.email},
              role = ${row.role},
              mobile = ${row.mobile},
              gender = ${row.gender},
              age = ${row.age},
              avatar = ${row.avatar},
              password = ${row.password},
              updated_at = ${row.updatedAt}
          WHERE id = ${row.id}
        """.executeUpdate()
      ()
    }
  }
}

