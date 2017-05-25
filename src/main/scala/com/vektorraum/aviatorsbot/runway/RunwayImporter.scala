package com.vektorraum.aviatorsbot.runway

import com.typesafe.scalalogging.Logger
import com.vektorraum.aviatorsbot.runway.persistence.model.{Airfield, Runway}
import org.xml.sax.SAXParseException

import scala.io.Source
import scala.xml.{Elem, NodeSeq}

/**
  * Created by fvalka on 25.05.2017.
  */
object RunwayImporter {
  val logger = Logger(getClass)

  def main(args: Array[String]): Unit = {
    val fileListPattern = "<a href=\"([A-Za-z]{2}_wpt.aip)\">".r
    val dirUrl = "https://www.openaip.net/customer_export_sdakjasd1fij1gnfvAFD32f/"
    //val fileList = Source.fromURL(dirUrl).getLines().mkString

    // FOR DEBUGGING ONLY
    val fileList = "\n<tr><td valign=\"top\"><img src=\"/icons/unknown.gif\" alt=\"[   ]\"></td><td><a href=\"at_wpt.aip\">at_wpt.aip</a></td><td align=\"right\">2017-05-21 04:01  </td><td align=\"right\"> 85K</td><td>&nbsp;</td></tr>"
    //println(fileList)

    logger.info("Converting files")
    val airports = fileListPattern.findAllIn(fileList).matchData flatMap { m =>
      val fileName = m.group(1)
      logger.info(s"Downloading and parsing file: $fileName")
      try {
        val file = xml.XML.load(dirUrl + fileName)
        (file \\ "AIRPORT") map { airport =>
          toCaseClass(airport)
        }
      } catch {
        case e: SAXParseException => logger.warn(s"Could not parse file $fileName")
          None
      }
    } filter { airfield =>
      airfield.runways.nonEmpty
    } toList

    logger.debug(airports mkString "\n")
  }

  def toCaseClass(xml: NodeSeq): Airfield = {
    val icao = (xml \ "ICAO").text
    val name = (xml \ "NAME").text

    val runways = xml \ "RWY" filter { rwy =>
      (rwy \ "@OPERATIONS").text == "ACTIVE"
    } map { rwy =>
      val name = (rwy \ "NAME").text
      val directions = (rwy \ "DIRECTION") map { dir =>
        dir \ "@TC"
      } map { dir =>
        dir.text.toInt
      }
      Runway(name, directions)
    }

    Airfield(icao, name, runways)
  }

}
