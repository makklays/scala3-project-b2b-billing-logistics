package com.techmatrix18.users.application.out

import com.techmatrix18.users.domain.{User, UserId}
import scala.concurrent.Future

/**
 * UserRepository - Outbound Driven Port для управления учетными записями пользователей.
 * Находится на стыке прикладного слоя и инфраструктуры СУБД.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 1.0.0
 * @since 08.08.2026
 */

trait UserRepository {

  // Поиск пользователя по строго типизированному Long-идентификатору (BIGSERIAL)
  def findById(id: UserId): Future[Option[User]]

  // Поиск пользователя по уникальному адресу электронной почты.
  // Реализация обязана приводить email к нижнему регистру перед поиском.
  def findByEmail(email: String): Future[Option[User]]

  // Поиск пользователя по уникальному логину (username) в системе
  def findByUsername(username: String): Future[Option[User]]

  // Сохраняет нового пользователя в базе данных и возвращает сгенерированный ID
  def create(user: User): Future[UserId]

  // Обновляет профильные данные существующего пользователя
  def update(user: User): Future[Unit]
}

