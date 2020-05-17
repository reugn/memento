package com.github.reugn.memento

import java.time.Duration
import java.util.Properties

import com.github.reugn.memento.kafka.{EmitTopicNameExtractor, SuspendingProcessor}
import com.github.reugn.memento.state.LocalRegulator
import com.github.reugn.memento.utils.StateStoreProxy
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization._
import org.apache.kafka.streams._
import org.apache.kafka.streams.processor.ProcessorSupplier
import org.apache.kafka.streams.state.{KeyValueStore, Stores}
import org.apache.kafka.streams.test.TestRecord
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MementoConsumerTest extends AnyFlatSpec with Matchers with BeforeAndAfter {

  private var testDriver: TopologyTestDriver = _
  private var store: KeyValueStore[Long, Array[Byte]] = _

  private val INPUT_TOPIC = "in"
  private val OUTPUT_TOPIC = "out"

  private val stringDeserializer = new StringDeserializer
  private val stringSerializer = new StringSerializer

  private var recordFactory: TestInputTopic[String, String] = _
  private var recordSink: TestOutputTopic[String, String] = _

  behavior of "Processor"

  before {
    val config = ConfigFactory.empty()
    val collectSupplier: ProcessorSupplier[String, String] = () => new SuspendingProcessor(config,
      new StateStoreProxy, new LocalRegulator)
    val storeBuilder = Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(utils.STATE_STORE_NAME),
      Serdes.Long, Serdes.ByteArray)
    val topology = new Topology
    topology.addSource("source", INPUT_TOPIC)
    topology.addProcessor("processor", collectSupplier, "source")
    topology.addStateStore(storeBuilder, "processor")
    topology.addSink("sink", new EmitTopicNameExtractor, "processor")

    // setup test driver
    val props = new Properties
    props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "storeTest")
    props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "host:9092")
    props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    testDriver = new TopologyTestDriver(topology, props)
    recordFactory = testDriver.createInputTopic(INPUT_TOPIC, stringSerializer, stringSerializer)
    recordSink = testDriver.createOutputTopic(OUTPUT_TOPIC, stringDeserializer, stringDeserializer)
  }

  it should "emit all records" in {
    populateTopic()
    // Advance the internally mocked wall-clock time
    testDriver.advanceWallClockTime(Duration.ofMinutes(1))
    val records = recordSink.readRecordsToList()
    records.size() shouldBe 10
  }

  private def populateTopic(): Unit = {
    val headers = new RecordHeaders()
    headers.add(utils.ORIGIN_TOPIC_HEADER, OUTPUT_TOPIC.getBytes)
    for (i <- 1 to 10) {
      headers.remove(utils.TIMESTAMP_HEADER)
      headers.add(utils.TIMESTAMP_HEADER, i.toString.getBytes)
      val record = new TestRecord(i.toString, i.toString, headers)
      recordFactory.pipeInput(record)
    }
  }
}
