package com.github.reugn.memento.kafka

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.apache.kafka.streams.state.{KeyValueStore, StoreBuilder}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}

import java.util.Properties
import javax.inject.{Inject, Provider}
import scala.concurrent.ExecutionContext

class MementoConsumer @Inject()(
                                 config: Config,
                                 processorProvider: Provider[SuspendingProcessor],
                                 storeBuilder: StoreBuilder[KeyValueStore[java.lang.Long, Array[Byte]]],
                                 implicit val ec: ExecutionContext
                               ) extends LazyLogging {

  protected val kafkaBroker: String = config.getString("kafka.bootstrap.servers")
  protected val parallelism: Int = config.getInt("kafka.parallelism.factor")
  protected val consumerGroup: String = config.getString("kafka.consumer.group")

  protected lazy val properties: Properties = {
    val props = new Properties
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, consumerGroup)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker)
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup)
    props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "3")
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, parallelism.toString)
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100")
    props
  }

  protected val inputTopic: String = config.getString(s"kafka.consumer.topic")
  protected val collectSupplier: ProcessorSupplier[String, String, String, String] = () => processorProvider.get()

  protected def createTopology(): KafkaStreams = {
    logger.info(s"Creating a topology for the topic $inputTopic")
    val builder = new Topology()
      .addSource("source", inputTopic)
      .addProcessor("collector", collectSupplier, "source")
      .addStateStore(storeBuilder, "collector")
      .addSink("sink", new EmitTopicNameExtractor, "collector")
    val stream = new KafkaStreams(builder, properties)
    stream
  }

  def runFlow(): Unit = {
    val stream = createTopology()
    stream.setUncaughtExceptionHandler((e: Throwable) => {
      logger.error(s"Error in ${getClass.getSimpleName}", e)
      stream.close()
      runFlow()
      StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
    })
    logger.info(s"Starting ${getClass.getSimpleName} flow")
    stream.cleanUp()
    stream.start()
  }

}
