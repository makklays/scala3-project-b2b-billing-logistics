package com.techmatrix18.users.application.out

import com.techmatrix18.users.domain.{AuthToken, TokenId, UserId}

import scala.concurrent.Future

/**
 * AuthTokenRepository - Driven Outbound Port для управления сессиями токенов
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

trait AuthTokenRepository {

  def findByAccessToken(token: String): Future[Option[AuthToken]]

  def findByRefreshToken(refreshToken: String): Future[Option[AuthToken]]

  def create(authToken: AuthToken): Future[TokenId]

  // Принудительный отзыв одной сессии (например, при Logout)
  def revokeToken(id: TokenId): Future[Unit]

  // Отзыв всех сессий пользователя (например, при смене пароля — «Выйти на всех устройствах»)
  def revokeAllUserTokens(userId: UserId): Future[Unit]

}

