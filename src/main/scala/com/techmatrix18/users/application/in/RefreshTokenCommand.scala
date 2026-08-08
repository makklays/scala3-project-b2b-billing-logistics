package com.techmatrix18.users.application.in

/**
 * Команда для обновления пары токенов по истечении срока Access-токена
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

case class RefreshTokenCommand(
  refreshToken: String,
  ipAddress: Option[String],
  userAgent: Option[String]
)

