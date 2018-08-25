package com.vektorraum.aviatorsbot.runway.persistence.model

import reactivemongo.bson.{BSONArray, BSONDocument, BSONDocumentWriter}

object CoordinatesWriter extends BSONDocumentWriter[Coordinates] {
  def write(coordinates: Coordinates): BSONDocument =
    BSONDocument("type" -> "Point", "coordinates" -> BSONArray(coordinates.lon, coordinates.lat))
}
