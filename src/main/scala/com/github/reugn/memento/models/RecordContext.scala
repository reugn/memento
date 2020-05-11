package com.github.reugn.memento.models

import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.{RecordHeader, RecordHeaders}
import org.apache.kafka.streams.processor.internals.ProcessorRecordContext

case class RecordContext(
                          timestamp: Long,
                          offset: Long,
                          topic: String,
                          partition: Int,
                          headers: Array[RawRecordHeader]
                        ) {

  def toProcessorRecordContext: ProcessorRecordContext = {
    val rh = headers.map(h => new RecordHeader(h.key, h.value).asInstanceOf[Header])
    new ProcessorRecordContext(
      timestamp,
      offset,
      partition,
      topic,
      new RecordHeaders(rh)
    )
  }
}

object RecordContext {

  def fromProcessorRecordContext(ctx: ProcessorRecordContext): RecordContext = {
    RecordContext(
      ctx.timestamp(),
      ctx.offset(),
      ctx.topic(),
      ctx.partition(),
      ctx.headers().toArray.map(h => RawRecordHeader(h.key(), h.value()))
    )
  }
}
