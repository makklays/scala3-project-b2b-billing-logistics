package com.techmatrix18.users.application.out

/**
 * PasswordHasher - Outbound Port для криптографической защиты паролей.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 09.08.2026
 */

trait PasswordHasher {

  // Генерирует безопасный однонаправленный хэш на основе сырого пароля
  def hash(passwordRaw: String): String

  // Сверяет сырой пароль с ранее сохраненным в БД хэшем
  // @return true, если пароль верен
  def check(passwordRaw: String, passwordHash: String): Boolean
}

