package com.techmatrix18.users.application.out

import com.techmatrix18.users.domain.{UserId, UserRole}
import java.time.Instant

/**
 * JwtService
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

trait JwtService {
  def generateToken(userId: UserId, role: UserRole, expiry: Instant): String
}

