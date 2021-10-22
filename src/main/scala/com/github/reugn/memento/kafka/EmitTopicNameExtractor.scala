package com.github.reugn.memento.kafka

import com.github.reugn.memento.utils
import org.apache.kafka.streams.processor.{RecordContext, TopicNameExtractor}

class EmitTopicNameExtractor extends TopicNameExtractor[String, String] {

  /**
   * Extracts the topic name to send to from the 'origin' header. The topic name must already exist,
   * since the Kafka Streams library will not try to automatically create the topic with the extracted name.
   */
  override def extract(key: String, value: String, recordContext: RecordContext): String = {
    new String(recordContext.headers().toArray.filter(_.key() == utils.ORIGIN_TOPIC_HEADER).apply(0).value())
  }
}
