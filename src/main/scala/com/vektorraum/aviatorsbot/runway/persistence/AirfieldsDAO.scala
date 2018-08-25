package com.vektorraum.aviatorsbot.runway.persistence

import com.vektorraum.aviatorsbot.runway.persistence.model.{Airfield, Coordinates, CoordinatesWriter, Runway}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{MultiBulkWriteResult, WriteResult}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentWriter, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fvalka on 25.05.2017.
  */
object AirfieldsDAO {
  def collection: Future[BSONCollection] = Db.aviatorsDb.map(_.collection("airfields_new"))
  implicit def runwayWriter: BSONDocumentWriter[Runway] = Macros.writer[Runway]
  implicit def airfieldsWriter: BSONDocumentWriter[Airfield] = Macros.writer[Airfield]
  implicit def coordinatesWriter: BSONDocumentWriter[Coordinates] = CoordinatesWriter

  def insertAirfields(airfields: List[Airfield]): Future[MultiBulkWriteResult] = {
    collection flatMap { collection =>
      val airfieldsAsDoc = airfields.map(implicitly[collection.ImplicitlyDocumentProducer](_))
      collection.bulkInsert(ordered = false)(airfieldsAsDoc: _*) map { writeResult =>
        collection.indexesManager.ensure(Index(Seq("coordinates" -> IndexType.Geo2DSpherical)))
        writeResult
      }
    }
  }

  def insertAirfield(airfield: Airfield): Future[WriteResult] = {
    collection flatMap { collection =>
      collection.insert(airfield)
    }
  }

//  def createPerson(airfield: Airfield): Future[Unit] =
//    collection.flatMap(_.insert(airfield).map(_ => {})) // use personWriter

}
