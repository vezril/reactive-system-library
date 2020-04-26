package org.cference.library.actors

import java.util.Date

import akka.actor.typed.scaladsl.adapter._
import akka.actor.Scheduler
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model._
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.server.Directives.complete
import akka.util.Timeout
import org.apache.logging.log4j.scala.Logging
import org.cference.library.actors.LibraryActor.{AddBookCommand, FindAllBooksCommand}
import org.cference.library.models.{Book, BookType, Language}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.collection.immutable.Seq
import scala.util.{Failure, Success}

object LibraryApi extends Logging {
  val book = Book("Test", "CalvinInc",  "10", "13")

  val port = 8080

  def apply(library: ActorRef[LibraryActor.Command], context: ActorContext[Nothing])
           (implicit materializer: Materializer, executionContext: ExecutionContext): Unit = {

    implicit val timeout = Timeout(2 seconds)
    implicit val classicSystem: akka.actor.ActorSystem = context.system.toClassic

    logger.info(s"Starting up LibraryApi on port ${port}")
    Http()
      .bind(interface = "localhost", port = port)
      .to(Sink.foreach{ connection =>
        connection.handleWithAsyncHandler(handler(library, context)(materializer, executionContext, timeout))
      })
      .run()
  }

  def handler(library: ActorRef[LibraryActor.Command], context: ActorContext[Nothing])
                     (implicit materializer: Materializer,
                      executionContext: ExecutionContext,
                      timeout: Timeout
                     ): HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/api"), _, _, _) => {
      logger.info("Hit API")
      handleRoot()
    }
    case HttpRequest(HttpMethods.GET, Uri.Path("/api/books"), _, _, _) => {
      logger.info("Get all books received")
      handleFindAllBooks(library, context)
    }
    case r @ HttpRequest(_, uri, _, _, _) => {
      logger.info(s"Invalid request on ${uri}")
      r.discardEntityBytes()
      Future.successful(HttpResponse(StatusCodes.NotFound, entity = "Unknown Resource"))
    }
  }

  private def handleRoot(): Future[HttpResponse] = Future.successful(HttpResponse(status = StatusCodes.OK))

  private def handleFindAllBooks(library: ActorRef[LibraryActor.Command], context: ActorContext[Nothing])
                                (implicit timeout: Timeout,
                                 executionContext: ExecutionContext
                                ): Future[HttpResponse] = {

    implicit val scheduler = context.system.scheduler
    val result: Future[LibraryActor.Reply] = library ? FindAllBooksCommand

    result.onComplete {
      case Success(_) => Future.successful(HttpResponse(status = StatusCodes.OK))
      case Failure(ex) => Future.successful(HttpResponse(status = StatusCodes.InternalServerError))
    }

    Future.successful(HttpResponse(status = StatusCodes.OK))

  }
}
