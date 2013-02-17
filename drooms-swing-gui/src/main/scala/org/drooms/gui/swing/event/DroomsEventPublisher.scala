package org.drooms.gui.swing.event

import scala.swing.Publisher
import scala.swing.event.Event
import org.drooms.gui.swing.TurnStep
import java.io.File
import org.drooms.gui.swing.GameReport
import org.drooms.gui.swing.TurnState

sealed class DroomsEventPublisher extends Publisher

object DroomsEventPublisher {
  val publisher = new DroomsEventPublisher

  def get(): DroomsEventPublisher = {
    publisher
  }
}
trait DroomsEvent extends Event

case class NewGameReportChosen(val gameReport: GameReport, val file: File) extends DroomsEvent

case class PlayersListUpdated extends DroomsEvent

case class GameRestarted extends DroomsEvent
case class GameFinished extends DroomsEvent
case class ReplayInitiated extends DroomsEvent
case class ReplayPaused extends DroomsEvent
case class ReplayContinued extends DroomsEvent

case class TurnStepPerformed(val turnStep: TurnStep) extends DroomsEvent
case class NextTurnInitiated extends DroomsEvent
case class PreviousTurn extends DroomsEvent
case class GoToTurn(turnNo: Int) extends DroomsEvent
case class GoToTurnState(turnNo: Int, state: TurnState) extends DroomsEvent
case class TurnDelayChanged(val value: Int) extends DroomsEvent

case class CoordinantsVisibilityChanged(val value: Boolean) extends DroomsEvent
case class PlaygroundGridDisabled extends DroomsEvent
case class PlaygroundGridEnabled extends DroomsEvent

case class UpdatePlayers extends DroomsEvent

