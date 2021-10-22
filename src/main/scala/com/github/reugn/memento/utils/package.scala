package com.github.reugn.memento

package object utils {

  // Required Kafka message header names
  val ORIGIN_TOPIC_HEADER = "origin"
  val TIMESTAMP_HEADER = "ts"

  val STATE_STORE_NAME = "events_store"
}
