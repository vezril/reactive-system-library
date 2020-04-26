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

  def apply(books: Map[UUID, Book] = Map.empty): Behavior[LibraryActor.Command] = {
    Behaviors.setup(context => {
      context.log.info("Staring LibraryActor")
      new LibraryActor(context)})
  }
}

class LibraryActor(context: ActorContext[LibraryActor.Command], books: Map[UUID, Book] = Map.empty) extends AbstractBehavior[LibraryActor.Command](context) {
  import LibraryActor._

  private def logCommand(c: Command): Unit = {
    context.log.info(s"${c.toString} Received")
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case c @ AddBookCommand(book, replyTo) => {
        logCommand(c)
        //replyTo ! AddBookSuccessReply
        Behaviors.same
      }
      case c @ FindBookByTitleCommand(title, replyTo) => {
        logCommand(c)
        Behaviors.same
      }
      case c @ FindAllBooksCommand(replyTo) => {
        logCommand(c)
        replyTo ! FindBookReply(books.values.toSeq)
        Behaviors.same
      }
    }
    this
  }
  // Check Configuration (context) ACtorContext[ConfigurationMessage for the state
}
