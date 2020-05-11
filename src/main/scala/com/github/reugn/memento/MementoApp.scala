package com.github.reugn.memento

import com.github.reugn.memento.http.HttpServer
import com.github.reugn.memento.kafka.MementoConsumer
import com.google.inject.Guice
import com.typesafe.scalalogging.LazyLogging

object MementoApp extends App with LazyLogging {
  val injector = Guice.createInjector(new Module)
  injector.getInstance(classOf[MementoConsumer]).runFlow()
  injector.getInstance[HttpServer](classOf[HttpServer]).init()
  logger.info("Memento service started.")
}
