package com.techmatrix18.users.domain

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
  username: String,
  email: String,
  roles: List[UserRole], // Парсится из плоской строки базы данных через разделитель
  mobile: Option[String],
  gender: Option[String],
  age: Option[Int],
  avatar: Option[String],
  passwordHash: String,  // Поле password из БД
  createdAt: Instant,
  updatedAt: Instant
)

