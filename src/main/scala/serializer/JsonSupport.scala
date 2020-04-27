package org.cference.library.serializer

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.cference.library.models.Book
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val bookFormat = jsonFormat4(Book)
}