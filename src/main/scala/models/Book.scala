package org.cference.library.models

import java.util.{Date, UUID}

object BookType extends Enumeration {
  val Paperback, Hardcover, Digital = Value
}

object Language extends Enumeration {
  val English, French, Japanese, Latin, Norse = Value
}

final case class Book(
                       bookType: BookType.Value,
                       title: String,
                       year: Date,
                       publisher: String,
                       language: Language.Value,
                       isbn10: String,
                       isbn13: String
                     )
