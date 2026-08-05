package com.techmatrix18.gates.domain

/**
 * WorkingHours
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

// Value Object для строгого управления рабочим временем ворот
case class WorkingHours(
  from: String,
  to: String
)

