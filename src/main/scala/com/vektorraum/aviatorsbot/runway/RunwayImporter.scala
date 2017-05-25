package com.vektorraum.aviatorsbot.runway

import com.typesafe.scalalogging.Logger
import com.vektorraum.aviatorsbot.runway.persistence.{AirfieldsDAO, Db}
import com.vektorraum.aviatorsbot.runway.persistence.model.{Airfield, Runway}
import org.xml.sax.SAXParseException

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import scala.util.Success
import scala.xml.{Elem, NodeSeq}
import scala.concurrent.ExecutionContext.Implicits.global

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

    logger.info(s"Found ${airports.size} airfields")

    logger.info("Inserting airfields into mongodb")
    val insertFuture = AirfieldsDAO.insertAirfields(airports)
    val insertResult = Await.result(insertFuture, 2 minutes)
    logger.info(s"Insert completed with code: ${insertResult.code} and ${insertResult.writeErrors} write errors")

    if(insertResult.ok) {
      logger.info("Renaming collections to put data live")
      val putAndCloseFuture = Db.putDataLive() andThen {
        case _ => logger.info("Closing database connection")
           Db.close()
      }
      Await.result(putAndCloseFuture, 30 seconds)
    }

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
