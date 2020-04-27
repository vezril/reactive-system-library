package org.cference.library

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
import org.apache.logging.log4j.scala.Logging
import org.cference.library.actors.LibraryManager

object Main extends Logging with App {

  val config = ConfigFactory.load()

  override def main(args: Array[String]): Unit = {
    sys.props += "log4j2.contextSelector" -> classOf[AsyncLoggerContextSelector].getName
    val system = ActorSystem[LibraryManager.Message](LibraryManager(), "library-manager")
  }


}
