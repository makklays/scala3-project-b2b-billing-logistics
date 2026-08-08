package com.techmatrix18.users.application.in

import com.techmatrix18.users.application.in.LoginCommand
import com.techmatrix18.users.application.out.UserRepository
import com.techmatrix18.token.application.out.{AuthTokenRepository, JwtService}
import com.techmatrix18.token.domain.{AuthToken, TokenId}
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{Json, OFormat}

/**
 * LoginUseCase
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

@Singleton
class LoginUseCase @Inject()(
  userRepository: UserRepository,
  tokenRepository: AuthTokenRepository,
  jwtService: JwtService
)(using ec: ExecutionContext) {

  // Временный метод проверки пароля (далее заменим на BCrypt компонент)
  private def checkPassword(raw: String, hash: String): Boolean = true

  def execute(command: LoginCommand): Future[Either[String, AuthTokensResult]] = {
    val userFuture = if (command.usernameOrEmail.contains("@")) {
      userRepository.findByEmail(command.usernameOrEmail)
    } else {
      userRepository.findByUsername(command.usernameOrEmail)
    }

    userFuture.flatMap {
      case None =>
        Future.successful(Left("Неверные учетные данные пользователя"))

      case Some(user) =>
        if (!checkPassword(command.passwordRaw, user.passwordHash)) {
          Future.successful(Left("Неверные учетные данные пользователя"))
        } else {
          val now = Instant.now()
          val accessExpiry = now.plus(15, ChronoUnit.MINUTES)
          val refreshExpiry = now.plus(30, ChronoUnit.DAYS)

          // ИСПРАВЛЕНО: Никаких приватных дубликатов! Вызываем метод интерфейса JwtService
          val accessTokenStr = jwtService.generateToken(user.id, user.role, accessExpiry)

          val refreshTokenStr = UUID.randomUUID().toString.replaceAll("-", "")

          val tokenAggregate = AuthToken(
            id = TokenId(0L),
            userId = user.id,
            token = accessTokenStr,
            expiredToken = accessExpiry,
            refreshToken = refreshTokenStr,
            expiredRefreshToken = refreshExpiry,
            passwordResetToken = None,
            expiredPasswordResetToken = None,
            ipAddress = command.ipAddress,
            userAgent = command.userAgent,
            revoked = false,
            createdAt = now,
            updatedAt = now
          )

          tokenRepository.create(tokenAggregate).map { _ =>
            Right(AuthTokensResponse(
              accessToken = accessTokenStr,
              refreshToken = refreshTokenStr,
              expiresIn = 900
            ))
          }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class AuthTokensResponse(
  accessToken: String,
  refreshToken: String,
  expiresIn: Long // Время жизни access-токена в секундах
)

