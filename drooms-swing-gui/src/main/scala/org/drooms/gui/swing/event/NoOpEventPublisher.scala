package org.drooms.gui.swing.event

import scala.swing.Publisher
import scala.swing.event.Event

class NoOpEventPublisher extends Publisher {
    override def publish(e: Event) = {
      // no operation
    }
    
    def apply() = NoOpEventPublisher
}

object NoOpEventPublisher extends Publisher {
  override def publish(e: Event) = {
    // no operation
  }
}