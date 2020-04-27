package org.cference.library.actors

import akka.actor.typed.{Behavior, PostStop}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import org.cference.library.routes.LibraryRoutes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object LibraryManager {

  sealed trait Message
  private final case class StartFailed(cause: Throwable) extends Message
  private final case class Started(binding: ServerBinding) extends Message
  case object Stop extends Message

  def apply(host: String = "0.0.0.0", port: Int = 8080): Behavior[Message] = Behaviors.setup { context =>
    context.log.info("Starting up LibraryManager")
    val library = context.spawn(LibraryEntity(), "library")

    implicit val system = context.system
    implicit val materializer = ActorMaterializer()(context.system.toClassic)
    implicit val classicSystem: akka.actor.ActorSystem = context.system.toClassic

    val routes = new LibraryRoutes(library)
    val serverBinding: Future[Http.ServerBinding] = Http.apply().bindAndHandle(routes.libraryRoutes, host, port)

    context.pipeToSelf(serverBinding) {
      case Success(binding) => Started(binding)
      case Failure(ex) => StartFailed(ex)
    }

    def running(binding: ServerBinding): Behavior[Message] = {
      Behaviors.receiveMessagePartial[Message] {
        case Stop => {
          context.log.info("Stopping server http://{}:{}/", binding.localAddress.getHostString, binding.localAddress.getPort)
          Behaviors.stopped
        }
      }.receiveSignal {
        case (_, PostStop) =>
          binding.unbind()
          Behaviors.same
      }
    }

    def starting(wasStopped: Boolean): Behaviors.Receive[Message] = {
      Behaviors.receiveMessage[Message] {
        case StartFailed(cause) => throw new RuntimeException("Server failed to start", cause)
        case Started(binding) => {
          context.log.info("Server online at http://{}:{}/", binding.localAddress.getHostString, binding.localAddress.getPort)
          if (wasStopped) context.self ! Stop
          running(binding)
        }
        case Stop => starting(wasStopped = true)
      }
    }
    starting(wasStopped = false)
  }
}
