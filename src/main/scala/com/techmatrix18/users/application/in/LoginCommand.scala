package com.techmatrix18.users.application.in

/**
 * Команда для инициализации сессии пользователя через логин/пароль
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

case class LoginCommand(
  usernameOrEmail: String,
  passwordRaw: String,
  ipAddress: Option[String],
  userAgent: Option[String]
)

