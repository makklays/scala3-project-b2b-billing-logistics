package com.techmatrix18.outbox.infrastructure.di

import com.google.inject.AbstractModule
import com.techmatrix18.outbox.application.out.OutboxRepository
import com.techmatrix18.outbox.infrastructure.db.PostgresOutboxRepository
import com.techmatrix18.outbox.infrastructure.scheduler.OutboxRelayScheduler

/**
 * OutboxModule
 * Чтобы фоновый движок стартовал сразу при запуске веб-сервера, его необходимо принудительно зарегистрировать
 * в модуле внедрения зависимостей (Guice DI Module) через метод asEagerSingleton()
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

class OutboxModule extends AbstractModule {

  override def configure(): Unit = {
    // Связываем интерфейс репозитория с его PostgreSQL-реализацией
    bind(classOf[OutboxRepository]).to(classOf[PostgresOutboxRepository])

    // Принудительно запускаем фоновый шедулер как Eager Singleton при старте приложения!
    bind(classOf[OutboxRelayScheduler]).asEagerSingleton()
  }
}

