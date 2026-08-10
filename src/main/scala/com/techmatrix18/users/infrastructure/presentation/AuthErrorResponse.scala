package com.techmatrix18.users.infrastructure.presentation

import play.api.libs.json.{Json, OFormat}

/**
 * AuthErrorResponse - Единый стандарт JSON-ответа при любых ошибках платформы.
 * Гарантирует Data Leak Protection и одинаковый контракт для фронтенда.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

case class AuthErrorResponse(
  error: String,
  details: Option[String] = None
)

