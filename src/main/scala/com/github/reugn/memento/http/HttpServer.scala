package com.github.reugn.memento.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

@Singleton
final class HttpServer @Inject()(config: Config,
                                 implicit val actorSystem: ActorSystem) extends LazyLogging {

  private implicit val materializer: Materializer = Materializer.createMaterializer(actorSystem)
  private implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  private val http_server_host = config.getString("http_server_host")
  private val http_server_port = config.getInt("http_server_port")

  def init(): Unit = {
    Http().bindAndHandle(route, http_server_host, http_server_port) onComplete {
      case Success(serverBinding) => logger.info(s"Server bound to ${serverBinding.localAddress}")
      case Failure(_) => logger.error(s"Failed to bind to $http_server_host:$http_server_port")
    }
  }

  private val route: Route =
    path("") {
      get {
        logger.trace("/ route call")
        complete("Memento service")
      }
    } ~
      path("health") {
        get {
          logger.trace("/health route call")
          complete("Ok")
        }
      } ~
      path("ready") {
        get {
          logger.trace("/ready route call")
          complete("Ok")
        }
      }
}
