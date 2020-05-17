package com.github.reugn.memento.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.reugn.memento.models.HttpMessage
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val httpMessageFormat: RootJsonFormat[HttpMessage] = jsonFormat4(HttpMessage)
}
