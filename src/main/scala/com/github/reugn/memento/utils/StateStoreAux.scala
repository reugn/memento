package com.github.reugn.memento.utils

import org.apache.kafka.streams.state.KeyValueStore

trait StateStoreAux {

  protected def store: KeyValueStore[Long, Array[Byte]]

  protected def nextAvailableKey(key: Long): Long = {
    Option(store.get(key)).fold(key)(_ => nextAvailableKey(key + 1))
  }
}
