name := "scala3-project-b2b-billing-logistics"

version := "0.1.0-SNAPSHOT"

// Используем самую актуальную и стабильную версию Scala 3
scalaVersion := "3.3.4"

// Строгие настройки компилятора вынесены отдельно
scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

// Версии библиотек
val playVersion      = "3.0.6" // Актуальная версия Play Framework 3.x на базе Pekko
val logbackVersion   = "1.5.6"
val scalaTestVersion = "3.2.19"

libraryDependencies ++= Seq(

  // 1. Ядро Play Framework (включает встроенный Pekko)
  "org.playframework" %% "play" % playVersion,

  // 2. Play HTTP Сервер (на Pekko HTTP / Netty)
  "org.playframework" %% "play-server" % playVersion,

  // 3. Play JSON (для парсинга и сериализации B2B запросов)
  "org.playframework" %% "play-json" % playVersion,

  // 4. Логирование
  "ch.qos.logback" % "logback-classic" % logbackVersion,

  // 5. Модуль баз данных Play JDBC (Синхронизирован с основной playVersion)
  "org.playframework" %% "play-jdbc" % playVersion,

  // 6. Официальная библиотека Anorm для Scala 3 (Стабильный релиз)
  "org.playframework.anorm" %% "anorm" % "3.0.0",

  // Добавьте эту строчку в libraryDependencies вашего build.sbt
  "org.playframework" %% "play-guice" % playVersion,

  // 7. Тестирование Play-приложения
  "org.playframework" %% "play-test"  % playVersion      % Test,
  "org.scalatest"     %% "scalatest"  % scalaTestVersion % Test,

  // 8. Безопасное хэширование паролей пользователей (Blowfish алгоритм)
  "org.mindrot" % "jbcrypt" % "0.4",

  // 9. Генерация и валидация JWT токенов для Scala 3 / Play 3
  "com.github.jwt-scala" %% "jwt-play-json" % "10.0.1"
)

