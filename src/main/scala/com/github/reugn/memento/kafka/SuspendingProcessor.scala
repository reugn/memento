package com.github.reugn.memento.kafka

import java.time.Duration

import com.github.reugn.memento.models.{Message, RecordContext}
import com.github.reugn.memento.state.DelayRegulator
import com.github.reugn.memento.utils
import com.github.reugn.memento.utils.{Serde, StateStoreAux, StateStoreProxy}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import org.apache.kafka.streams.processor.internals.AbstractProcessorContext
import org.apache.kafka.streams.processor.{AbstractProcessor, ProcessorContext, PunctuationType, Punctuator}
import org.apache.kafka.streams.state.KeyValueStore

import scala.language.postfixOps
import scala.util.Try

class SuspendingProcessor @Inject()(config: Config,
                                    proxy: StateStoreProxy,
                                    regulator: DelayRegulator)
  extends AbstractProcessor[String, String] with StateStoreAux with LazyLogging {

  protected var store: KeyValueStore[Long, Array[Byte]] = _

  private val intervalMillis = Try {
    config.getLong("emit_interval_millis")
  }.getOrElse(1000L)

  override def init(context: ProcessorContext): Unit = {
    super.init(context)
    // init a state store
    store = context.getStateStore(utils.STATE_STORE_NAME).asInstanceOf[KeyValueStore[Long, Array[Byte]]]
    proxy.setStore(store)

    // schedule an emit ready-to-go messages process
    this.context.schedule(Duration.ofMillis(intervalMillis), PunctuationType.WALL_CLOCK_TIME, (timestamp => {
      if (regulator.shouldEmit()) {
        store.synchronized {
          val it = store.range(0, timestamp)
          it forEachRemaining { entry =>
            val m = Serde.deserialize(entry.value).asInstanceOf[Message]
            this.context.asInstanceOf[AbstractProcessorContext].setRecordContext(m.recordContext.toProcessorRecordContext)
            this.context.forward(m.key, m.value)
            store.delete(entry.key)
          }
          it.close()
        }
      }
    }): Punctuator)
  }

  override def process(key: String, value: String): Unit = {
    val message = Message(key, value,
      RecordContext.fromProcessorRecordContext(context.asInstanceOf[AbstractProcessorContext].recordContext()))
    store.synchronized(store.put(nextAvailableKey(getEmitTimestamp), Serde.serialize(message)))
    context.commit()
  }

  private def getEmitTimestamp: Long = {
    new String(context().headers().toArray.filter(_.key() == utils.TIMESTAMP_HEADER).apply(0).value()) toLong
  }
}
