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
  roles: String, // В БД хранится как строка "USER" или "ADMIN,DRIVER"
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
    val parsedRoles = roles.split(",")
      .map(_.trim)
      .flatMap(code => UserRole.values.find(_.code == code))
      .toList

    User(
      id = UserId(id),
      username = username,
      email = email,
      roles = if (parsedRoles.isEmpty) List(UserRole.StandardUser) else parsedRoles,
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
    get[Long]("id") ~
      get[String]("username") ~
      get[String]("email") ~
      get[String]("roles") ~
      get[Option[String]]("mobile") ~
      get[Option[String]]("gender") ~
      get[Option[Int]]("age") ~
      get[Option[String]]("avatar") ~
      get[String]("password") ~
      get[Instant]("created_at") ~
      get[Instant]("updated_at") map {
      case id ~ username ~ email ~ roles ~ mobile ~ gender ~ age ~ avatar ~ password ~ createdAt ~ updatedAt =>
        UserRow(id, username, email, roles, mobile, gender, age, avatar, password, createdAt, updatedAt)
    }
  }

  // Сборка плоской строки БД из иммутабельного доменного объекта перед записью
  def fromDomain(user: User): UserRow = UserRow(
    id = user.id.raw,
    username = user.username,
    email = user.email,
    roles = user.roles.map(_.code).mkString(","), // Склеиваем список ролей в строку через запятую
    mobile = user.mobile,
    gender = user.gender,
    age = user.age,
    avatar = user.avatar,
    password = user.passwordHash,
    createdAt = user.createdAt,
    updatedAt = user.updatedAt
  )
}

