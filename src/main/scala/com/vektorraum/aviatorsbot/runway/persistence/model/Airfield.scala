package com.vektorraum.aviatorsbot.runway.persistence.model

/**
  * Created by fvalka on 25.05.2017.
  */
case class Airfield(icao: String, name: String, magVar: Double, runways: Seq[Runway]) {

}
