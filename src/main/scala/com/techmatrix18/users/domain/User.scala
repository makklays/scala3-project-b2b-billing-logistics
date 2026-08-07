package com.techmatrix18.users.domain

import com.techmatrix18.users.domain.{UserId, UserRole}
import java.time.Instant

/**
 * User Aggregate Root - скорректирован под миграцию V12
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */
case class User(
  id: UserId,

  // Fields for login
  username: String,
  email: String,

  role: UserRole, // Парсится из плоской строки базы данных через разделитель
  mobile: Option[String],
  gender: Option[String],
  age: Option[Int],
  avatar: Option[String],
  passwordHash: String,  // Поле password из БД

  // System audit (managed by the system, not by the user)
  createdAt: Instant,
  updatedAt: Instant
)

