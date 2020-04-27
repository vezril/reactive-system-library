package org.cference.library.actors

import java.util.UUID

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import org.cference.library.models.Book

object LibraryActor {
  sealed trait Command
  final case class AddBookCommand(book: Book, replyTo: ActorRef[Reply]) extends Command
  final case class FindBookByTitleCommand(title: String, replyTo: ActorRef[Reply]) extends Command
  final case class FindAllBooksCommand(replyTo: ActorRef[Reply]) extends Command

  sealed trait Event
  final case class BookAddedEvent(id: UUID, book: Book) extends Event

  sealed trait Reply
  final case object AddBookSuccessReply extends Reply
  final case object AddBookFailedReply extends Reply
  final case class FindBookReply(books: Seq[Book]) extends Reply

  def apply(books: Map[UUID, Book] = Map(UUID.randomUUID -> Book("1984", "YoMama", "10", "13"))): Behavior[Command] = Behaviors.receiveMessage {
    case c @ AddBookCommand(book, replyTo) => {
      //logCommand(c)
      replyTo ! AddBookSuccessReply
      LibraryActor(books ++ Map(UUID.randomUUID -> book))
      //Behaviors.same
    }
    case c @ FindBookByTitleCommand(title, replyTo) => {
      //logCommand(c)
      replyTo ! FindBookReply(books.values.find(_.title == title).toSeq)
      Behaviors.same
    }
    case c @ FindAllBooksCommand(replyTo) => {
      //logCommand(c)
      replyTo ! FindBookReply(books.values.toSeq)
      Behaviors.same
    }
  }
}
