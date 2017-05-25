package com.vektorraum.aviatorsbot.runway.persistence

import com.vektorraum.aviatorsbot.runway.persistence.model.{Airfield, Runway}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.bson.{BSONDocumentWriter, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fvalka on 25.05.2017.
  */
object AirfieldsDAO {
  def collection: Future[BSONCollection] = Db.db1.map(_.collection("airfields_new"))
  implicit def runwayWriter: BSONDocumentWriter[Runway] = Macros.writer[Runway]
  implicit def airfieldsWriter: BSONDocumentWriter[Airfield] = Macros.writer[Airfield]

  def insertAirfields(airfields: List[Airfield]): Future[MultiBulkWriteResult] = {
    collection flatMap { collection =>
      val airfieldsAsDoc = airfields.map(implicitly[collection.ImplicitlyDocumentProducer](_))
      collection.bulkInsert(ordered = false)(airfieldsAsDoc: _*)
    }
  }

//  def createPerson(airfield: Airfield): Future[Unit] =
//    collection.flatMap(_.insert(airfield).map(_ => {})) // use personWriter

}
