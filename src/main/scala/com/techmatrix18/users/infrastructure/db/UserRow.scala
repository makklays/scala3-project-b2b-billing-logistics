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
 * UserRow - Вспомогательная структура строки таблицы users для Anorm.
 * Изолирует базу данных от чистых моделей домена.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

private case class UserRow(
  id: Long,
  username: String,
  email: String,
  role: String, // В БД хранится как строка "USER" или "ADMIN,DRIVER"
  mobile: Option[String],
  gender: Option[String],
  age: Option[Int],
  avatar: Option[String],
  password: String,
  createdAt: Instant,
  updatedAt: Instant
) {
  // Трансформация строки БД в чистый DDD Агрегат
  def toDomain: User = {
    User(
      id = UserId(id),
      username = username,
      email = email,
      role = UserRole.valueOf(role),
      mobile = mobile,
      gender = gender,
      age = age,
      avatar = avatar,
      passwordHash = password,
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }
}

private object UserRow {
  // Anorm-парсер для автоматической сборки структуры UserRow из SQL-ответа
  val parser: RowParser[UserRow] = {
    SqlParser.get[Long]("id") ~
    SqlParser.get[String]("username") ~
    SqlParser.get[String]("email") ~
    SqlParser.get[String]("role") ~
    SqlParser.get[Option[String]]("mobile") ~
    SqlParser.get[Option[String]]("gender") ~
    SqlParser.get[Option[Int]]("age") ~
    SqlParser.get[Option[String]]("avatar") ~
    SqlParser.get[String]("password") ~
    SqlParser.get[Instant]("created_at") ~
    SqlParser.get[Instant]("updated_at") map {
      case id ~ username ~ email ~ role ~ mobile ~ gender ~ age ~ avatar ~ password ~ createdAt ~ updatedAt =>
        UserRow(id, username, email, role, mobile, gender, age, avatar, password, createdAt, updatedAt)
    }
  }

  // Сборка плоской строки БД из иммутабельного доменного объекта перед записью
  def fromDomain(user: User): UserRow = UserRow(
    id = user.id.raw,
    username = user.username,
    email = user.email,
    role = user.role.toString,
    mobile = user.mobile,
    gender = user.gender,
    age = user.age,
    avatar = user.avatar,
    password = user.passwordHash,
    createdAt = user.createdAt,
    updatedAt = user.updatedAt
  )
}

