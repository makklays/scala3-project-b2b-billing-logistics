package com.techmatrix18.hub_sections.domain

/**
 * SectionType - определяет специфику хранения внутри секции хаба
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.3
 * @since 06.08.2026
 */

enum SectionType(val code: String) {
  case PalletZone    extends SectionType("PALLET_ZONE")
  case ColdZone      extends SectionType("COLD_ZONE")
  case BulkZone      extends SectionType("BULK_ZONE")
  case HazardousZone extends SectionType("HAZARDOUS_ZONE")
}

