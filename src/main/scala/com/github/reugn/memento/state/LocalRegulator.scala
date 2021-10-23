package com.github.reugn.memento.state

class LocalRegulator extends DelayRegulator {

  /**
   * Emit all.
   * @return boolean
   */
  override def shouldEmit(): Boolean = true
}
