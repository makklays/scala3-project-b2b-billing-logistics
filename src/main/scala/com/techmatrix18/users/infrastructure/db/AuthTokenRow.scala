package com.techmatrix18.users.infrastructure.db

import com.techmatrix18.users.domain.{AuthToken, TokenId, UserId}
import java.time.Instant

/**
 * AuthTokenRow - Отражение структуры таблицы tokens в СУБД для Anorm
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

case class AuthTokenRow(
  id: Long,
  userId: Long,
  token: String,
  expiredToken: Instant,
  refreshToken: String,
  expiredRefreshToken: Instant,
  passwordResetToken: Option[String],
  expiredPasswordResetToken: Option[Instant],
  ipAddress: Option[String],
  userAgent: Option[String],
  revoked: Boolean,
  createdAt: Instant,
  updatedAt: Instant
) {
  def toDomain: AuthToken = AuthToken(
    id = TokenId(id),
    userId = UserId(userId),
    token = token,
    expiredToken = expiredToken,
    refreshToken = refreshToken,
    expiredRefreshToken = expiredRefreshToken,
    passwordResetToken = passwordResetToken,
    expiredPasswordResetToken = expiredPasswordResetToken,
    ipAddress = ipAddress,
    userAgent = userAgent,
    revoked = revoked,
    createdAt = createdAt,
    updatedAt = updatedAt
  )
}

object AuthTokenRow {
  val parser: RowParser[AuthTokenRow] = {
    get[Long]("id") ~
      get[Long]("user_id") ~
      get[String]("token") ~
      get[Instant]("expired_token") ~
      get[String]("refresh_token") ~
      get[Instant]("expired_refresh_token") ~
      get[Option[String]]("password_reset_token") ~
      get[Option[Instant]]("expired_password_reset_token") ~
      get[Option[String]]("ip_address") ~
      get[Option[String]]("user_agent") ~
      get[Boolean]("revoked") ~
      get[Instant]("created_at") ~
      get[Instant]("updated_at") map {
      case id ~ userId ~ token ~ expiredToken ~ refreshToken ~ expiredRefreshToken ~
        passwordResetToken ~ expiredPasswordResetToken ~ ipAddress ~ userAgent ~ revoked ~ createdAt ~ updatedAt =>
        AuthTokenRow(
          id, userId, token, expiredToken, refreshToken, expiredRefreshToken,
          passwordResetToken, expiredPasswordResetToken, ipAddress, userAgent, revoked, createdAt, updatedAt
        )
    }
  }

  def fromDomain(d: AuthToken): AuthTokenRow = AuthTokenRow(
    id = d.id.raw,
    userId = d.userId.raw,
    token = d.token,
    expiredToken = d.expiredToken,
    refreshToken = d.refreshToken,
    expiredRefreshToken = d.expiredRefreshToken,
    passwordResetToken = d.passwordResetToken,
    expiredPasswordResetToken = d.expiredPasswordResetToken,
    ipAddress = d.ipAddress,
    userAgent = d.userAgent,
    revoked = d.revoked,
    createdAt = d.createdAt,
    updatedAt = d.updatedAt
  )
}

