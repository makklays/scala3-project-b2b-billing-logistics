package com.techmatrix18.users.infrastructure.http

import com.techmatrix18.users.application.in.*
import play.api.libs.json.{Json, OFormat}

/**
 * AuthJsonFormats
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

class AuthJsonFormats {

  // Scala 3 Given форматирование (аналог implicit val, но чище и современнее)
  given loginCommandFormat: OFormat[LoginCommand] = Json.format[LoginCommand]
  given refreshCommandFormat: OFormat[RefreshTokenCommand] = Json.format[RefreshTokenCommand]

  given loginResponseFormat: OFormat[AuthTokensResponse] = Json.format[AuthTokensResponse]
  given refreshResponseFormat: OFormat[RefreshTokensResponse] = Json.format[RefreshTokensResponse]

}

