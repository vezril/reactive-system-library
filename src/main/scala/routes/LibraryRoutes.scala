package org.cference.library.routes

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.ContentNegotiator.Alternative.ContentType
import akka.http.scaladsl.server.{Directive, PathMatcher, RequestContext, Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import org.cference.library.actors.LibraryActor
import org.cference.library.actors.LibraryActor._
import org.cference.library.models.Book
import org.cference.library.serializer.JsonSupport
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait MethodAndPathDirectives {
  def getPath[L](x: PathMatcher[L]): Directive[L] = get & path(x)
  def postPath[L](x: PathMatcher[L]): Directive[L] = post & path(x)
  def putPath[L](x: PathMatcher[L]): Directive[L] = put & path(x)
  def deletePath[L](x: PathMatcher[L]): Directive[L] = delete & path(x)
  def headPath[L](x: PathMatcher[L]): Directive[L] = head & path(x)
}

object LibraryRoutes {
  private def toHttpEntity(payload: String) = HttpEntity(ContentTypes.`application/json`, payload)

  private def findBookByTitle(title: String)(replyTo: ActorRef[LibraryActor.Reply]): FindBookByTitleCommand = FindBookByTitleCommand(title, replyTo)
  private def addBook(book: Book)(replyTo: ActorRef[LibraryActor.Reply]): AddBookCommand = AddBookCommand(book, replyTo)

}

class LibraryRoutes(library: ActorRef[LibraryActor.Command])(implicit system: ActorSystem[_])
  extends JsonSupport
    with DefaultJsonProtocol
    with MethodAndPathDirectives {

  import LibraryRoutes._

  implicit val timeout: Timeout = 2 seconds

  private def handleAddBook(book: Book): RequestContext => Future[RouteResult] = {
    onSuccess((library ? addBook(book))) {
      case AddBookSuccessReply => complete(StatusCodes.OK)
      case AddBookFailedReply => complete(StatusCodes.BadRequest)
    }
  }

  private def handleSearchAll: RequestContext => Future[RouteResult] = {
    onSuccess((library ? FindAllBooksCommand)) {
      case FindBookReply(books) => complete(toHttpEntity(books.toJson.prettyPrint))
    }
  }

  private def handleFindByTitle(title: String): RequestContext => Future[RouteResult] = {
    onSuccess((library ? findBookByTitle(title))) {
      case FindBookReply(books) => complete(toHttpEntity(books.toJson.prettyPrint))
    }
  }

  lazy val libraryRoutes2: Route =
    getPath("api" / "v1" / "books") {
      handleSearchAll
    }~
    getPath("api" / "v1" / "books" / Segment) { title =>
      handleFindByTitle(title)
    }~
    postPath("api" / "v1" / "books") {
      entity(as[Book]) { book =>
        handleAddBook(book)
      }
    }
}
