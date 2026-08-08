package com.techmatrix18.token.infrastructure.db

import com.techmatrix18.users.application.out.AuthTokenRepository
import com.techmatrix18.users.domain.UserId
import com.techmatrix18.token.domain.{AuthToken, TokenId}
import javax.inject.{Inject, Singleton}
import play.api.db.Database
import anorm.*
import anorm.SqlParser.*
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

/**
 * PostgresAuthTokenRepository - Реализация работы с JWT-сессиями через Anorm
 * Выдача JWT токена и обновление токена
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 08.08.2026
 */

@Singleton
class PostgresAuthTokenRepository @Inject()(
  db: Database
)(using ec: ExecutionContext) extends AuthTokenRepository {

  override def findByAccessToken(token: String): Future[Option[AuthToken]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT id, user_id, token, expired_token, refresh_token, expired_refresh_token,
               password_reset_token, expired_password_reset_token, ip_address, user_agent, revoked,
               created_at, updated_at
        FROM tokens
        WHERE token = ${token.trim} AND revoked = false
      """.as(AuthTokenRow.parser.singleOptional).map(_.toDomain)
    }
  }

  override def findByRefreshToken(refreshToken: String): Future[Option[AuthToken]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          SELECT id, user_id, token, expired_token, refresh_token, expired_refresh_token,
                 password_reset_token, expired_password_reset_token, ip_address, user_agent, revoked,
                 created_at, updated_at
          FROM tokens
          WHERE refresh_token = ${refreshToken.trim} AND revoked = false
        """.as(AuthTokenRow.parser.singleOptional).map(_.toDomain)
    }
  }

  override def create(authToken: AuthToken): Future[TokenId] = Future {
    val row = AuthTokenRow.fromDomain(authToken)
    db.withConnection { implicit connection =>
      val generatedId =
        SQL"""
          INSERT INTO tokens (
            user_id, token, expired_token, refresh_token, expired_refresh_token,
            password_reset_token, expired_password_reset_token, ip_address, user_agent, revoked, created_at, updated_at
          ) VALUES (
            ${row.userId}, ${row.token}, ${row.expiredToken}, ${row.refreshToken}, ${row.expiredRefreshToken},
            ${row.passwordResetToken}, ${row.expiredPasswordResetToken}, ${row.ipAddress}, ${row.userAgent}, ${row.revoked}, ${row.createdAt}, ${row.updatedAt}
          )
        """.executeInsert(scalar[Long].single)

      TokenId(generatedId)
    }
  }

  override def revokeToken(id: TokenId): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE tokens
          SET revoked = true, updated_at = NOW()
          WHERE id = ${id.raw}
        """.executeUpdate()
      ()
    }
  }

  override def revokeAllUserTokens(userId: UserId): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
          UPDATE tokens
          SET revoked = true, updated_at = NOW()
          WHERE user_id = ${userId.raw} AND revoked = false
        """.executeUpdate()
      ()
    }
  }
}

