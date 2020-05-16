package com.github.reugn.memento.utils

import org.apache.kafka.streams.state.KeyValueStore

final class StateStoreProxy extends StateStoreAux {

  protected var store: KeyValueStore[Long, Array[Byte]] = _

  def setStore(store: KeyValueStore[Long, Array[Byte]]): Unit = {
    this.store = store
  }

  def put(key: Long, value: Array[Byte]): Unit = {
    store.synchronized {
      store.put(nextAvailableKey(key), value)
    }
  }
}
