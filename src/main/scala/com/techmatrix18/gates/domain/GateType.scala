package com.techmatrix18.gates.domain

/**
 * GateType
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.08.2026
 */

enum GateType(val code: String) {

  // Наземная логистика (интеграция с хабами типа LAND_PORT)
  case Dry            extends GateType("DRY")               // Сухой док
  case ColdStorage    extends GateType("COLD_STORAGE")      // Холодильные доки
  case Hazardous      extends GateType("HAZARDOUS")         // Опасные грузы
  case StandardGate   extends GateType("STANDARD_GATE")     // Стандартные доки
  case HeavyTruckGate extends GateType("HEAVY_TRUCK_GATE")  // Тяжелые грузовики
  case VanGate        extends GateType("VAN_GATE")          // Экспресс-доки

  // Морская логистика (интеграция с хабами типа SEA_PORT)
  case MarineBerth extends GateType("MARINE_BERTH")         // Морской док для контейнеровозов

  // Космическая логистика (интеграция с хабами типа AIRPORT и SPACEPORT)
  case SpaceGate extends GateType("SPACE_CARGO_GATE")       // Космический док для суборбитальных шаттлов

  // Железно дорожная логистика (интеграция с хабами типа RAILWAY_STATION)
  case RailwayGate extends GateType("RAILWAY_GATE")         // Железнодорожная станция
}

