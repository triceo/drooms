package org.drooms.gui.swing.event

import scala.swing.event.Event
import org.drooms.gui.swing.GameReport
import java.io.File
import org.drooms.gui.swing.TurnStep
import org.drooms.gui.swing.TurnState
import org.drooms.gui.swing.NewGameConfig

/**
 * Parent trait for all Drooms Swing App related events.
 *
 * Events are used by individual components to communicate with each other.
 * Component that wants to listen to certain events has to call method {@code listenTo} 
 * {@link EventBus} implementation and then register the reactions for those events. The
 * 
 *
 * @see EventBus
 */
trait DroomsEvent extends Event

case object NewGameRequested extends DroomsEvent
case class NewGameCreated(val config: NewGameConfig) extends DroomsEvent
case object BeforeNewReportChosen extends DroomsEvent
case class NewGameReportChosen(val gameReport: GameReport, val file: File) extends DroomsEvent
case object AfterNewReportChosen extends DroomsEvent

case object PlayersListUpdated extends DroomsEvent

case object StartGame extends DroomsEvent
case object PauseGame extends DroomsEvent
case object ContinueGame extends DroomsEvent

case object GameStarted extends DroomsEvent
case object GameRestarted extends DroomsEvent
case object GameFinished extends DroomsEvent
case object ReplayInitiated extends DroomsEvent
case object ReplayPaused extends DroomsEvent
case object ReplayContinued extends DroomsEvent

case class TurnStepPerformed(val turnStep: TurnStep) extends DroomsEvent
case object NextTurnInitiated extends DroomsEvent
case class NextTurnPerformed(val turnNo: Int) extends DroomsEvent
case object NextTurnAvailable extends DroomsEvent
//case object PreviousTurnAvailabe extends DroomsEvent

case object PreviousTurn extends DroomsEvent
case class GoToTurn(turnNo: Int) extends DroomsEvent
case class GoToTurnState(turnNo: Int, state: TurnState) extends DroomsEvent
case class TurnDelayChanged(val value: Int) extends DroomsEvent

case class CoordinantsVisibilityChanged(val value: Boolean) extends DroomsEvent
case object PlaygroundGridDisabled extends DroomsEvent
case object PlaygroundGridEnabled extends DroomsEvent

case object UpdatePlayers extends DroomsEvent
