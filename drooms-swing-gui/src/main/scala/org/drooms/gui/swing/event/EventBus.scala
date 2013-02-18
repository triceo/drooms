package org.drooms.gui.swing.event

import scala.swing.Publisher
import scala.swing.event.Event
import org.drooms.gui.swing.TurnStep
import java.io.File
import org.drooms.gui.swing.GameReport
import org.drooms.gui.swing.TurnState

trait EventBus extends Publisher

sealed case class DroomsEventBus extends EventBus

case class NoOpEventBus extends EventBus {
  override def publish(e: Event) = {
    // no operation
  }

  def apply() = NoOpEventBus
}

object NoOpEventBus extends EventBus {
  override def publish(e: Event) = {
    // no operation
  }
}

/**
 * Factory used for creating concrete bus implementations.
 * 
 * Currently there is:
 *   - standard DroomsEventBus which will propagate the events to
 *     all listeners.
 *   - NoOpEventBus (No Operation) which will do _nothing_ when publish method is called.
 */
object EventBusFactory {
  val bus = new DroomsEventBus()
  val noOpBus = NoOpEventBus

  def get(): EventBus = bus

  def getNoOp(): EventBus = noOpBus
}
