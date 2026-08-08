package com.techmatrix18.users.application.in

import com.techmatrix18.users.application.in.RefreshTokenCommand
import com.techmatrix18.users.application.out.UserRepository
import com.techmatrix18.token.application.out.AuthTokenRepository
import com.techmatrix18.users.domain.{User, UserId}
import com.techmatrix18.token.domain.{AuthToken, TokenId}
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * RefreshSessionUseCase - Высокозащищенный прикладной сервис ротации сессий.
 * Реализует паттерн RFC 6749 (Refresh Token Rotation & Reuse Detection).
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

@Singleton
class RefreshTokenUseCase @Inject()(
  userRepository: UserRepository,
  tokenRepository: AuthTokenRepository
)(using ec: ExecutionContext) {

  private val AccessTokenLifetimeMinutes = 15
  private val RefreshTokenLifetimeDays = 30

  def execute(command: RefreshTokenCommand): Future[Either[String, RefreshTokensResponse]] = {
    val now = Instant.now()

    // 1. Ищем сессию по предоставленному токену (даже среди отозванных для детекции атак)
    tokenRepository.findByRefreshToken(command.refreshToken).flatMap {
      case None =>
        Future.successful(Left("Сессия не найдена либо токен недействителен"))

      // КРИТИЧЕСКИЙ СЦЕНАРИЙ: Обнаружена попытка повторного использования токена (Reuse Detection)
      case Some(oldSession) if oldSession.revoked =>
        // Токен уже был использован ранее или отозван принудительно.
        // Вероятен перехват токена злоумышленником. Защищаем пользователя: сбрасываем ВСЕ его сессии.
        tokenRepository.revokeAllUserTokens(oldSession.userId).map { _ =>
          Left("Внимание: Нарушение безопасности сессии. Все активные входы для данного аккаунта аннулированы")
        }

      // СЦЕНАРИЙ: Срок действия токена ротации физически истек
      case Some(oldSession) if now.isAfter(oldSession.expiredRefreshToken) =>
        Future.successful(Left("Срок действия сессии полностью истек. Пожалуйста, авторизуйтесь заново"))

      // ШТАТНЫЙ СЦЕНАРИЙ: Токен валиден, выполняем безопасную ротацию
      case Some(oldSession) =>
        userRepository.findById(oldSession.userId).flatMap {
          case None =>
            Future.successful(Left("Пользователь данной сессии удален из системы"))

          case Some(user) =>
            val accessExpiry = now.plus(AccessTokenLifetimeMinutes, ChronoUnit.MINUTES)
            val refreshExpiry = now.plus(RefreshTokenLifetimeDays, ChronoUnit.DAYS)

            // Выпуск честного подписанного Auth через инфраструктурный порт
            val newAccessTokenStr = authService.generateToken(user.id, user.role, accessExpiry)
            val newRefreshTokenStr = UUID.randomUUID().toString.replaceAll("-", "")

            val newSession = AuthToken(
              id = TokenId(0L),                                  // Будет перетерто СУБД (BIGSERIAL)
              userId = oldSession.userId,
              token = newAccessTokenStr,
              expiredToken = accessExpiry,
              refreshToken = newRefreshTokenStr,
              expiredRefreshToken = refreshExpiry,
              passwordResetToken = None,
              expiredPasswordResetToken = None,
              ipAddress = command.ipAddress,                     // Фиксируем новый IP-адрес, если клиент в движении
              userAgent = command.userAgent,
              revoked = false,
              createdAt = now,
              updatedAt = now
            )

            // Выполняем цепочку изменений в рамках бизнес-транзакции
            for {
              _ <- tokenRepository.revokeToken(oldSession.id)    // Погасить текущую использованную сессию
              _ <- tokenRepository.create(newSession)            // Создать новую запись ротации
            } yield {
              Right(RefreshTokensResponse(
                accessToken = newAccessTokenStr,
                refreshToken = newRefreshTokenStr,
                expiresIn = AccessTokenLifetimeMinutes * 60
              ))
            }
        }
    }
  }
}

// DTO ответа (Data Transfer Object) уровня Application In
case class RefreshTokensResponse(
  accessToken: String,
  refreshToken: String,
  expiresIn: Long // Время жизни access-токена в секундах
)

