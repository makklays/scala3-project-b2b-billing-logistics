package com.techmatrix18.hubs.domain

/**
 * HubType
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 04.08.2026
 */
enum HubType(val code: String) {
  case LandTerminal     extends HubType("LAND_TERMINAL")
  case SeaPort          extends HubType("SEA_PORT")
  case Airport          extends HubType("AIRPORT")
  case Spaceport        extends HubType("SPACEPORT")
  case RailwayStation   extends HubType("RAILWAY_STATION")
  case BusTerminal      extends HubType("BUS_TERMINAL")
  case FerryTerminal    extends HubType("FERRY_TERMINAL")
  case CargoTerminal    extends HubType("CARGO_TERMINAL")
  case LogisticsCenter  extends HubType("LOGISTICS_CENTER")
}

