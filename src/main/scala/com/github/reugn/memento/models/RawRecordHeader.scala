package com.github.reugn.memento.models

import org.apache.kafka.common.header.Header

case class RawRecordHeader(key: String, value: Array[Byte]) extends Header
