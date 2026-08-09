package com.techmatrix18.users.application.service

import com.techmatrix18.auth.application.in.RefreshSessionCommand
import com.techmatrix18.auth.application.out.{UserRepository, TokenRepository, JwtService}
import com.techmatrix18.auth.domain.{AuthToken, TokenId, UserId, UserRole, User}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import scala.concurrent.Future

/**
 * RefreshSessionUseCaseSpec - Комплект тестов для проверки безопасности ротации сессий.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 10.08.2026
 */

class RefreshSessionUseCaseSpec extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures {

  // Вспомогательные доменные данные для тестов
  private val targetUserId = UserId(42L)
  private val sampleUser = User(
    id = targetUserId,
    username = "driver_john",
    email = "john@techmatrix18.com",
    passwordHash = "$2a$12$MockHash...",
    role = UserRole.Driver,
    createdAt = Instant.now(),
    updatedAt = Instant.now()
  )

  private def createBaseToken(revoked: Boolean, expired: Boolean): AuthToken = {
    val expiry = if (expired) Instant.now().minus(1, ChronoUnit.HOURS) else Instant.now().plus(1, ChronoUnit.HOURS)
    AuthToken(
      id = TokenId(100L),
      userId = targetUserId,
      token = "old.access.jwt.string",
      expiredToken = Instant.now().minus(10, ChronoUnit.MINUTES),
      refreshToken = "valid_refresh_token_uuid",
      expiredRefreshToken = expiry,
      passwordResetToken = None,
      expiredPasswordResetToken = None,
      ipAddress = Some("192.168.1.1"),
      userAgent = Some("Mozilla/5.0"),
      revoked = revoked,
      createdAt = Instant.now().minus(1, ChronoUnit.DAYS),
      updatedAt = Instant.now().minus(1, ChronoUnit.DAYS)
    )
  }

  "RefreshSessionUseCase" should {

    // Сценарий 1: Штатная и безопасная замена старых токенов на новые
    "успешно выполнить ротацию токенов, если текущий Refresh-токен валиден" in {
      // Инициализируем моки зависимостей
      val userRepo = mock[UserRepository]
      val tokenRepo = mock[TokenRepository]
      val jwtService = mock[JwtService]

      val activeSession = createBaseToken(revoked = false, expired = false)
      val command = RefreshSessionCommand("valid_refresh_token_uuid", Some("192.168.1.5"), Some("Mozilla/5.0"))

      // Настраиваем поведение моков (Stubbing)
      when(tokenRepo.findByRefreshToken(command.refreshToken)).thenReturn(Future.successful(Some(activeSession)))
      when(userRepo.findById(activeSession.userId)).thenReturn(Future.successful(Some(sampleUser)))
      when(jwtService.generateToken(any[UserId], any[UserRole], any[Instant])).thenReturn("new.signed.jwt.string")
      when(tokenRepo.revokeToken(activeSession.id)).thenReturn(Future.successful(()))
      when(tokenRepo.create(any[AuthToken])).thenReturn(Future.successful(TokenId(101L)))

      val useCase = new RefreshSessionUseCase(userRepo, tokenRepo, jwtService)

      // Выполняем юзкейс и проверяем структуру успешного ответа
      useCase.execute(command).map { result =>
        result.isRight shouldBe true
        val authResult = result.getOrElse(fail("Ожидался успешный ответ"))
        authResult.accessToken shouldBe "new.signed.jwt.string"
        authResult.refreshToken should not be empty
        authResult.expiresIn shouldBe 900 // 15 минут

        // Проверяем, что старый токен действительно был отозван в БД
        verify(tokenRepo).revokeToken(activeSession.id)
      }
    }

    // Сценарий 2: Физическое истечение срока жизни сессии
    "вернуть ошибку, если срок действия Refresh-токена полностью истек" in {
      val userRepo = mock[UserRepository]
      val tokenRepo = mock[TokenRepository]
      val jwtService = mock[JwtService]

      val expiredSession = createBaseToken(revoked = false, expired = true)
      val command = RefreshSessionCommand("expired_refresh_token_uuid", Some("192.168.1.1"), Some("Mozilla/5.0"))

      when(tokenRepo.findByRefreshToken(command.refreshToken)).thenReturn(Future.successful(Some(expiredSession)))

      val useCase = new RefreshSessionUseCase(userRepo, tokenRepo, jwtService)

      useCase.execute(command).map { result =>
        result shouldBe Left("Срок действия сессии полностью истек. Пожалуйста, авторизуйтесь заново")
        // Убеждаемся, что мы не тратили ресурсы на генерацию новых токенов и чтение данных пользователя
        verifyZeroInteractions(userRepo)
        verifyZeroInteractions(jwtService)
      }
    }

    // Сценарий 3: Защита от хакеров (Reuse Detection)
    "сбросить ВСЕ активные сессии пользователя при попытке повторно использовать отозванный токен" in {
      val userRepo = mock[UserRepository]
      val tokenRepo = mock[TokenRepository]
      val jwtService = mock[JwtService]

      // Симулируем взлом: токен уже помечен как revoked (например, легитимный клиент обновился секунду назад)
      val compromisedSession = createBaseToken(revoked = true, expired = false)
      val command = RefreshSessionCommand("stolen_refresh_token_uuid", Some("203.0.113.50"), Some("HackerBrowser/1.0"))

      when(tokenRepo.findByRefreshToken(command.refreshToken)).thenReturn(Future.successful(Some(compromisedSession)))
      when(tokenRepo.revokeAllUserTokens(compromisedSession.userId)).thenReturn(Future.successful(()))

      val useCase = new RefreshSessionUseCase(userRepo, tokenRepo, jwtService)

      useCase.execute(command).map { result =>
        result.isLeft shouldBe true
        result.left.getOrElse("") should include("Внимание: Нарушение безопасности сессии")

        // ГЛАВНАЯ ПРОВЕРКА: Репозиторий обязан выбить пользователя со всех устройств ради безопасности
        verify(tokenRepo).revokeAllUserTokens(compromisedSession.userId)

        // База данных защищена, новые токены злоумышленнику не выданы
        verifyZeroInteractions(userRepo)
        verifyZeroInteractions(jwtService)
      }
    }
  }
}

