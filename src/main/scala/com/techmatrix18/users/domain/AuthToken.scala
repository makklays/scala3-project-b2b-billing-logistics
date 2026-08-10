package com.techmatrix18.users.domain

import com.techmatrix18.users.domain.UserId
import java.time.Instant

/**
 * AuthToken Aggregate Root - Сущность управления сессиями и токенами доступа.
 * Изолирует правила валидации токенов внутри доменного слоя.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

case class AuthToken(
  id: TokenId,
  userId: UserId,                           // Связь с UserId (BIGINT) из домена User
  token: String,                            // Access Token (строка или JWT)

  expiredToken: Instant,                    // Время истечения Access Token
  refreshToken: String,                     // Уникальный Refresh Token для обновления сессии
  expiredRefreshToken: Instant,             // Время истечения Refresh Token
  passwordResetToken: Option[String],       // Опциональный токен сброса пароля
  expiredPasswordResetToken: Option[Instant],
  ipAddress: Option[String],                // IPv4/IPv6 адрес клиента для аудита безопасности
  userAgent: Option[String],                // Браузер / Устройство клиента
  revoked: Boolean,                         // Флаг принудительного отзыва сессии (например, при Logout)

  // System audit (managed by the system, not by the user)
  createdAt: Instant,
  updatedAt: Instant
) {

  // Бизнес-правило: Проверка, действителен ли токен доступа в данный момент
  def isAccessTokenActive(now: Instant): Boolean = !revoked && now.isBefore(expiredToken)

  // Бизнес-правило: Проверка, можно ли использовать Refresh Token для выпуска новой пары
  def isRefreshTokenValid(now: Instant): Boolean = !revoked && now.isBefore(expiredRefreshToken)

  // Бизнес-правило: Проверка валидности токена сброса пароля
  def isPasswordResetValid(now: Instant): Boolean =
    !revoked && passwordResetToken.isDefined && expiredPasswordResetToken.exists(now.isBefore)
}

