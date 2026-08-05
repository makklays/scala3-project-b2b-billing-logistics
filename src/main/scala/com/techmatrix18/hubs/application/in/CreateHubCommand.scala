package com.techmatrix18.hubs.application.in

/**
 * Command to create a new Hub.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

case class CreateHubCommand(
  companyId: String,
  title: String,
  description: Option[String] = None,  // Защита от отсутствия поля или null в JSON
  hubType: String,                     // Передается как строка (например, "SEA_PORT")
  countryCode: String,                 // Например, "ES"
  city: String,                        // Например, "Valencia"
  postalCode: String,
  addressLine: String,
  latitude: BigDecimal,                // Точные GPS-координаты
  longitude: BigDecimal
)

