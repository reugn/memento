package com.github.reugn.memento.state

class LocalRegulator extends DelayRegulator {

  /**
   * Emit for each
   * @return boolean
   */
  override def shouldEmit(): Boolean = true
}
