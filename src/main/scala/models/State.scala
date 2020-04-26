package org.cference.library

import java.util.UUID

import org.cference.library.models.Book

final case class State(books: List[Map[UUID, Book]])
