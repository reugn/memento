package com.github.reugn.memento.kafka

import com.github.reugn.memento.models.{Message, RecordContext}
import com.github.reugn.memento.state.DelayRegulator
import com.github.reugn.memento.utils
import com.github.reugn.memento.utils.{Serde, StateStoreAux, StateStoreProxy}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.processor.api.{Processor, ProcessorContext, Record}
import org.apache.kafka.streams.processor.internals.{AbstractProcessorContext, InternalProcessorContext}
import org.apache.kafka.streams.processor.{PunctuationType, Punctuator}
import org.apache.kafka.streams.state.KeyValueStore

import java.time.Duration
import javax.inject.Inject
import scala.language.postfixOps
import scala.util.Try

class SuspendingProcessor @Inject()(config: Config,
                                    proxy: StateStoreProxy,
                                    regulator: DelayRegulator)
  extends Processor[String, String, String, String] with StateStoreAux with LazyLogging {

  protected var store: KeyValueStore[Long, Array[Byte]] = _
  protected var context: InternalProcessorContext = _

  private val intervalMillis = Try {
    config.getLong("emit_interval_millis")
  }.getOrElse(1000L)

  override def init(context: ProcessorContext[String, String]): Unit = {
    super.init(context)
    this.context = context.asInstanceOf[InternalProcessorContext]
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

  override def process(record: Record[String, String]): Unit = {
    val message = Message(record.key, record.value,
      RecordContext.fromProcessorRecordContext(context.asInstanceOf[AbstractProcessorContext].recordContext()))
    store.synchronized(store.put(nextAvailableKey(getEmitTimestamp), Serde.serialize(message)))
    context.commit()
  }

  private def getEmitTimestamp: Long = {
    new String(context.headers().toArray.filter(_.key() == utils.TIMESTAMP_HEADER).apply(0).value()) toLong
  }
}
