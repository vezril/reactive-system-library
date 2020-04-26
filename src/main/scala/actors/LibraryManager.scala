package org.cference.library.actors

import java.util.Date

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.{ActorMaterializer, Materializer}
import org.cference.library.actors.LibraryActor.AddBookCommand
import org.cference.library.models.{Book, BookType, Language}

import scala.concurrent.ExecutionContext.Implicits.global


object LibraryManager {
  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    context.log.info("Starting up LibraryManager")
    val library = context.spawn(LibraryActor(), "library")

    implicit val system = context.system
    implicit val materializer = ActorMaterializer()(context.system.toClassic)
    implicit val classicSystem: akka.actor.ActorSystem = context.system.toClassic

    LibraryApi(library, context)
    Behaviors.empty
  }
}

class LibraryManager {

}
