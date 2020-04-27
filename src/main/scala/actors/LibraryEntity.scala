package org.cference.library.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.RecoveryCompleted
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import org.apache.logging.log4j.scala.Logging
import org.cference.library.models.Book

object LibraryEntity extends Logging {
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

  final case class State(books: Map[UUID, Book] = Map.empty)

  private def logCommand(c: Command): Unit = {
    logger.info(s"Received ${c.toString} Command")
  }

  val eventSourcedBehavior = EventSourcedBehavior[Command, Event, State](
    persistenceId = PersistenceId.ofUniqueId("library-entity"),
    emptyState = State(Map.empty),
    commandHandler = commandHandler,
    eventHandler = eventHandler
  )//.receiveSignal {
    //case (state, RecoveryCompleted)
  //}

  val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case c @ AddBookCommand(book, replyTo) => {
        logCommand(c)
        Effect
          .persist(BookAddedEvent(UUID.randomUUID, book))
          .thenRun { _ =>
            replyTo ! AddBookSuccessReply
          }
      }
      case c @ FindBookByTitleCommand(title, replyTo) => {
        logCommand(c)
        Effect
          .none
          .thenRun {_ =>
            replyTo ! FindBookReply(state.books.values.find(_.title == title).toSeq)
          }
      }
      case c @ FindAllBooksCommand(replyTo) => {
        logCommand(c)
        Effect
          .none
          .thenRun {_ =>
            replyTo ! FindBookReply(state.books.values.toSeq)
          }
      }
    }
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      case BookAddedEvent(id, book) => State(state.books ++ Map(id -> book))
    }
  }

  def apply(): Behavior[Command] = {
    Behaviors.setup{ context =>
      context.log.info(s"[${eventSourcedBehavior.persistenceId.id}] starting up")
      eventSourcedBehavior
    }
  }

  def apply2(books: Map[UUID, Book] = Map(UUID.randomUUID -> Book("1984", "YoMama", "10", "13"))): Behavior[Command] = Behaviors.receiveMessage {
    case c @ AddBookCommand(book, replyTo) => {
      logCommand(c)
      replyTo ! AddBookSuccessReply
      LibraryEntity.apply2(books ++ Map(UUID.randomUUID -> book))
      //Behaviors.same
    }
    case c @ FindBookByTitleCommand(title, replyTo) => {
      logCommand(c)
      replyTo ! FindBookReply(books.values.find(_.title == title).toSeq)
      Behaviors.same
    }
    case c @ FindAllBooksCommand(replyTo) => {
      logCommand(c)
      replyTo ! FindBookReply(books.values.toSeq)
      Behaviors.same
    }
  }
}
