package com.github.reugn.memento.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.github.reugn.memento.models.{HttpMessage, Message, RawRecordHeader, RecordContext}
import com.github.reugn.memento.utils
import com.github.reugn.memento.utils.{Serde, StateStoreProxy}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

@Singleton
final class HttpServer @Inject()(config: Config,
                                 proxy: StateStoreProxy,
                                 implicit val actorSystem: ActorSystem) extends JsonSupport with LazyLogging {

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
      } ~
      path("store") {
        post {
          entity(as[HttpMessage]) { msg =>
            logger.trace("/store route call")
            val ctx = RecordContext(
              msg.ts,
              0,
              "http",
              0,
              Array(
                RawRecordHeader(utils.ORIGIN_TOPIC_HEADER, msg.origin.getBytes),
                RawRecordHeader(utils.TIMESTAMP_HEADER, String.valueOf(msg.ts).getBytes)
              )
            )
            proxy.put(msg.ts, Serde.serialize(Message(msg.key.orNull, msg.value, ctx)))
            complete("Message successfully settled")
          }
        }
      }
}
