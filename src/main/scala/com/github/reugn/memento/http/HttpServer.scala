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

  private val http_server_host = config.getString("http.server.host")
  private val http_server_port = config.getInt("http.server.port")
  private val http_store_route_enable = config.getBoolean("http.routes.store")

  def init(): Unit = {
    Http().newServerAt(http_server_host, http_server_port).bindFlow(getRoutes) onComplete {
      case Success(serverBinding) => logger.info(s"Server bound to ${serverBinding.localAddress}")
      case Failure(_) => logger.error(s"Failed to bind to $http_server_host:$http_server_port")
    }
  }

  private def getRoutes: Route = {
    if (http_store_route_enable)
      stdRoutes ~ storeRoute
    else
      stdRoutes
  }

  private val stdRoutes: Route =
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

  private val storeRoute: Route =
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
          complete("Message successfully stored")
        }
      }
    }
}
