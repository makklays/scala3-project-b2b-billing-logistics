# --- Этап 1: Сборка приложения ---
FROM sbtscala/scala-sbt:eclipse-temurin-alpine-17_1.x AS builder

WORKDIR /app

# Копируем файлы конфигурации сборки для кеширования зависимостей
COPY project/build.properties project/
COPY project/plugins.sbt project/
COPY build.sbt .

# Кешируем зависимости sbt
RUN sbt update

# Копируем исходный код
COPY . .

# Собираем и компилируем все классы приложения
RUN sbt compile

# Генерируем исполняемый скрипт или собираем сборку (используем универсальный stage)
# Если проект использует sbt-native-packager, выполнится stage.
# Если это чистый sbt без плагинов, мы подстрахуемся и запустим сборку через встроенный скрипт.
RUN sbt stage || sbt universal:stage || (echo "Fallback to direct run" && sbt compile)

# Собираем дистрибутив приложения (Play генерирует zip в target/universal/)
#RUN sbt dist && \
#    cd target/universal/ && \
#    unzip *.zip && \
#    mv auth-platform-* /app/dist

# --- Этап 2: Финальный легковесный образ ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /opt/auth-platform

# Создаем безопасного системного пользователя, чтобы не запускать приложение из-под root
RUN addgroup --system playgroup && adduser --system playuser --ingroup playgroup

# Копируем собранное приложение из первого этапа
COPY --from=builder /app/dist .

# Меняем владельца папки на созданного пользователя
RUN chown -R playuser:playgroup /opt/auth-platform

USER playuser

# Открываем стандартный порт Play Framework
EXPOSE 9000

# Запускаем скрипт приложения. Передаем системные переменные JVM
ENTRYPOINT ["./bin/auth-platform", "-Dplay.http.secret.key=${PLAY_SECRET_KEY}", "-Dhttp.port=9000"]

