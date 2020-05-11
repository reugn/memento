package com.github.reugn.memento.state

/**
 * Manages delayed messages in the state store
 */
trait DelayRegulator {

  /**
   * Decides if to emit ready-to-go messages
   * @return boolean
   */
  def shouldEmit(): Boolean
}
