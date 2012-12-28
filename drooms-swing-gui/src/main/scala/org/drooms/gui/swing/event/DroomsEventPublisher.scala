package org.drooms.gui.swing.event

import scala.swing.Publisher
import scala.swing.event.Event
import org.drooms.gui.swing.TurnStep

sealed class DroomsEventPublisher extends Publisher {
}

object DroomsEventPublisher {
  val publisher = new DroomsEventPublisher

  def get(): DroomsEventPublisher = {
    publisher
  }
}

case class PlayersListUpdated extends Event
case class TurnStepPerformed(val turnStep: TurnStep) extends Event
