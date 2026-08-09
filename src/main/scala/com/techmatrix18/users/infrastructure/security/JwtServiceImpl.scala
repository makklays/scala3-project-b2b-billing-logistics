package com.techmatrix18.users.infrastructure.security

import com.techmatrix18.users.application.out.JwtService
import com.techmatrix18.users.domain.{UserId, UserRole}
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.{Json, JsObject}
import pdi.jwt.{JwtJson, JwtAlgorithm}

/**
 * JwtServiceImpl - Инфраструктурная реализация криптографического порта.
 * Отвечает за генерацию криптоустойчивых stateless-токенов доступа.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 09.08.2026
 */

@Singleton
class JwtServiceImpl @Inject()(
  config: Configuration
) extends JwtService {

  // Вытаскиваем системный секретный ключ проекта из application.conf.
  // Если ключ не задан, используем безопасный дефолт для локальной разработки.
  private val secretKey: String = config.getOptional[String]("play.http.secret.key")
    .getOrElse("super-secure-techmatrix-local-secret-key-2026")

  // Будем использовать самый популярный и производительный алгоритм подписи
  private val algorithm = JwtAlgorithm.HS256

  override def generateToken(userId: UserId, role: UserRole, expiry: Instant): String = {
    // 1. Формируем JSON-нагрузку (Claims) токена
    val claims: JsObject = Json.obj(
      "sub" -> userId.raw, // Идентификатор субъекта (User ID)
      "role" -> role.code, // Текстовый код роли (например, "ADMIN")
      "iat" -> Instant.now().getEpochSecond, // Время выпуска (Issued At)
      "exp" -> expiry.getEpochSecond // Время смерти токена (Expiration Time)
    )

    // 2. Криптографически подписываем строку алгоритмом HMAC-SHA256
    JwtJson.encode(claims, secretKey, algorithm)
  }
}

