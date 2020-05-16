package com.github.reugn.memento

import java.util

import akka.actor.ActorSystem
import com.github.reugn.memento.state.{DelayRegulator, LocalRegulator}
import com.github.reugn.memento.utils.StateStoreProxy
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.state.{KeyValueStore, StoreBuilder, Stores}

import scala.concurrent.ExecutionContext

class Module extends AbstractModule {

  override protected def configure(): Unit = {
    bind(classOf[ActorSystem]).toInstance(ActorSystem("default"))
    bind(classOf[Config]).toInstance(ConfigFactory.load().resolve())
    bind(classOf[ExecutionContext]).toInstance(scala.concurrent.ExecutionContext.global)
    bind(classOf[DelayRegulator]).to(classOf[LocalRegulator])
    bind(classOf[StateStoreProxy]).in(Scopes.SINGLETON)
    bind(new TypeLiteral[StoreBuilder[KeyValueStore[java.lang.Long, Array[Byte]]]] {}).toInstance(buildStore())
  }

  private def buildStore(): StoreBuilder[KeyValueStore[java.lang.Long, Array[Byte]]] = {
    val logConfig = new util.HashMap[String, String]
    val storeSupplier = Stores.inMemoryKeyValueStore(utils.STATE_STORE_NAME)
    Stores.keyValueStoreBuilder(storeSupplier, Serdes.Long, Serdes.ByteArray).withLoggingEnabled(logConfig)
  }
}
