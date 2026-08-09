package com.techmatrix18.users.infrastructure.security

import com.techmatrix18.auth.application.out.PasswordHasher
import org.mindrot.jbcrypt.BCrypt
import javax.inject.{Inject, Singleton}

/**
 * BCryptPasswordHasher - Инфраструктурный адаптер шифрования.
 * Использует стандартный алгоритм BCrypt с солью.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 09.08.2026
 */

@Singleton
class BCryptPasswordHasher @Inject()() extends PasswordHasher {

  // Рекомендуемый уровень сложности (Work Factor).
  // 10-12 обеспечивает идеальный баланс между безопасностью и нагрузкой на CPU.
  private val LogRounds = 12

  override def hash(passwordRaw: String): String = {
    val salt = BCrypt.gensalt(LogRounds)
    BCrypt.hashpw(passwordRaw, salt)
  }

  override def check(passwordRaw: String, passwordHash: String): Boolean = {
    try {
      BCrypt.checkpw(passwordRaw, passwordHash)
    } catch {
      case _: IllegalArgumentException =>
        // Защита на случай, если в БД оказался поврежденный или пустой хэш
        false
    }
  }
}

