package com.techmatrix18.hubs.domain

import java.util.UUID
import java.time.Instant

/**
 * Hub
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */
case class Hub(
  id: HubId,             // Строгий типобезопасный ID хаба
  companyId: CompanyId,  // Связь с компанией-владельцем (Foreign Key)
  title: String,         // Название (синхронизировано с вашей миграцией)
  description: String,   // Описание (может быть пустым)
  hubType: HubType,
  status: HubStatus,

  // Географический блок для GPS-навигации фур по Испании
  countryCode: String,   // Например, "ES"
  city: String,          // Например, "Valencia"
  postalCode: String,
  addressLine: String,
  latitude: Double,      // Точные координаты
  longitude: Double,

  // Системный аудит (управляется бэкендом)
  createdAt: Instant,
  updatedAt: Instant
) {

  // Пример чистого доменного правила: можно ли принимать грузовики прямо сейчас?
  def isOperational: Boolean = status == HubStatus.Active
}

