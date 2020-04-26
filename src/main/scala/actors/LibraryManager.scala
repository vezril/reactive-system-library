package org.cference.library.actors

import java.util.UUID

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import org.cference.library.models.Book

object LibraryManager {
  sealed trait Command
  final case class AddBookCommand(book: Book, replyTo: ActorRef[Reply]) extends Command
  final case class FindBookByTitleCommand(title: String, replyTo: ActorRef[Reply]) extends Command
  final case class FindAllBooksCommand(replyTo: ActorRef[Reply]) extends Command

  sealed trait Event
  final case class BookAddedEvent(id: UUID, book: Book) extends Event

  sealed trait Reply
  final case class AddBookSuccessReply(timestamp: Long) extends Reply
  final case class AddBookFailedReply(timestamp: Long) extends Reply
  final case class FindBookReply(book: List[Book]) extends Reply
}

class LibraryManager(context: ActorContext[LibraryManager.Command]) extends AbstractBehavior[LibraryManager.Command](context) {
  import LibraryManager._

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case AddBookCommand(book, replyTo) => ???
      case FindBookByTitleCommand(title, replyTo) => ???
      case FindAllBooksCommand(replyTo) => ???
    }
    this
  }

  // Check Configuration (context) ACtorContext[ConfigurationMessage for the state

}
