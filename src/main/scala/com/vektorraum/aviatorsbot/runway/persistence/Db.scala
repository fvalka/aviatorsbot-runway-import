package com.vektorraum.aviatorsbot.runway.persistence


import com.vektorraum.aviatorsbot.runway.persistence.model.Airfield
import reactivemongo.api.commands.{RenameCollection, UnitBox}
import reactivemongo.api.{DefaultDB, FailoverStrategy, MongoConnection, MongoDriver}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros, document}
import reactivemongo.api.commands.bson.BSONRenameCollectionImplicits._
import reactivemongo.api.commands.bson.CommonImplicits._

import scala.concurrent.Future

/**
  * Created by fvalka on 25.05.2017.
  */
object Db {
  val mongoUri = "mongodb://localhost:27017/aviatorsbot?authMode=scram-sha1"

  import scala.concurrent.ExecutionContext.Implicits.global // use any appropriate context

  // Connect to the database: Must be done only once per application
  val driver = MongoDriver()
  val parsedUri = MongoConnection.parseURI(mongoUri)
  val connection = parsedUri.map(driver.connection(_))

  // Database and collections: Get references
  val futureConnection = Future.fromTry(connection)

  def db1: Future[DefaultDB] = futureConnection.flatMap(_.database("aviatorsbot"))
  def adminDb: Future[DefaultDB] = futureConnection.flatMap(_.database("admin"))

  def putDataLive(): Future[UnitBox.type] = {
    val cmd = RenameCollection("aviatorsbot.airfields_new", "aviatorsbot.airfields", true)
    adminDb flatMap { db =>
      db.runCommand(cmd, FailoverStrategy.default)
    }
  }

  def close(): Unit = {

    futureConnection map { conn =>
      conn.close()
      driver.close()
    }
  }

}
