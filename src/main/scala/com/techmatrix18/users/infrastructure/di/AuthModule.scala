package com.techmatrix18.users.infrastructure.di

import com.google.inject.AbstractModule
import com.techmatrix18.auth.application.out.{UserRepository, TokenRepository, JwtService, PasswordHasher}
import com.techmatrix18.auth.infrastructure.db.{PostgresUserRepository, PostgresTokenRepository}
import com.techmatrix18.auth.infrastructure.security.{JwtServiceImpl, BCryptPasswordHasher}

/**
 * AuthModule - Главный конфигурационный файл внедрения зависимостей контура Auth.
 * Связывает абстрактные порты прикладного слоя с конкретными инфраструктурными адаптерами.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 09.08.2026
 */

class AuthModule extends AbstractModule {

  override def configure(): Unit = {
    // 1. Связываем криптографию и безопасность
    bind(classOf[PasswordHasher]).to(classOf[BCryptPasswordHasher])
    bind(classOf[JwtService]).to(classOf[JwtServiceImpl])

    // 2. Связываем репозитории работы с базой данных PostgreSQL (Anorm)
    bind(classOf[UserRepository]).to(classOf[PostgresUserRepository])
    bind(classOf[TokenRepository]).to(classOf[PostgresTokenRepository])

    // Принудительно запускаем шедулер очистки БД при старте приложения
    bind(classOf[DatabaseCleanupScheduler]).asEagerSingleton()

  }
}

