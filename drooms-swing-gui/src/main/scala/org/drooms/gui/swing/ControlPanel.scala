package org.drooms.gui.swing

import java.awt.Font
import java.io.File
import scala.swing.Action
import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.ProgressBar
import scala.swing.Publisher
import scala.swing.Reactor
import scala.swing.Slider
import scala.swing.TextField
import scala.swing.event.EditDone
import scala.swing.event.ValueChanged
import org.drooms.gui.swing.event.AfterNewReportChosen
import org.drooms.gui.swing.event.BeforeNewReportChosen
import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.GameFinished
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.GoToTurn
import org.drooms.gui.swing.event.NewGameReportChosen
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.PreviousTurn
import org.drooms.gui.swing.event.ReplayContinued
import org.drooms.gui.swing.event.ReplayInitiated
import org.drooms.gui.swing.event.ReplayPaused
import org.drooms.gui.swing.event.TurnDelayChanged
import org.drooms.gui.swing.event.GameStarted
import org.drooms.gui.swing.event.NextTurnAvailable
import org.drooms.gui.swing.event.NewGameCreated
import org.drooms.gui.swing.event.EventBus
import org.drooms.gui.swing.event.StartGame
import org.drooms.gui.swing.event.PauseGame
import org.drooms.gui.swing.event.ContinueGame
import org.drooms.gui.swing.event.GoToTurn
import org.drooms.gui.swing.event.NextTurnPerformed

/**
 * Control panel specific for game replay.
 */
class ReplayControlPanel(eventBus: EventBus) extends ControlPanel(eventBus) {
  var currentLog: (GameReport, File) = _
}

/**
 * Control panel specific for real-time game.
 */
class RealTimeGameControlPanel(eventBus: EventBus) extends ControlPanel(eventBus) {
  var gameState: GameState = GameNotStarted
  val startStopGameBtn = new Button(Action("Start") {
    var newState: GameState = GameNotStarted
    val event = gameState match {
      case GameNotStarted =>
        newState = GameRunning
        StartGame

      case GameRunning =>
        newState = GamePaused
        PauseGame

      case GamePaused =>
        newState = GameRunning
        ContinueGame
    }
    // first send the event and let other components react to it and after that
    // set new game state
    eventBus.publish(event)
    gameState = newState
  }) {

  }

  trait GameState
  case object GameNotStarted extends GameState
  case object GameRunning extends GameState
  case object GamePaused extends GameState
  case object GameStopped extends GameState
}

object ControlPanel {
  def newReplayControlPanel(): ControlPanel = {
    new ReplayControlPanel(EventBusFactory.get())
  }
}
/**
 * Control panel located at the bottom of the window
 */
class ControlPanel(val eventBus: EventBus) extends BorderPanel with Reactor with Publisher {
  //var currentTurn = 0
  var gameStatus: GameStatus = GameNotStarted

  val replayBtn = new Button(Action("Replay") {
    gameStatus match {
      case GameNotStarted =>
        eventBus.publish(ReplayInitiated)
      case GameReplaying =>
        eventBus.publish(ReplayPaused)
      case GameReplayingPaused =>
        eventBus.publish(ReplayContinued)
    }
  }) {
    enabled = false
  }
  val prevTurnBtn = new Button(Action("Previous turn") {
    eventBus.publish(PreviousTurn)
  })
  val nextTurnBtn = new Button(Action("Next turn") {
    eventBus.publish(NextTurnInitiated)
  }) {
    enabled = false
  }
  val restartBtn = new Button(Action("Reset game") {
    eventBus.publish(GameRestarted)
  }) {
    enabled = false
  }
  val progressBar = new ProgressBar {
    labelPainted = true
  }
  val intervalSlider = new Slider {
    min = 50
    max = 1000
    value = 100
    paintLabels = true
    minorTickSpacing = 100
    majorTickSpacing = 250
    font = new Font("Serif", Font.PLAIN, 10)
  }
  listenTo(intervalSlider)
  val turnSlider = new Slider {
    min = 0
    max = 1000
    value = 0
    paintLabels = true
    //majorTickSpacing = 250
    font = new Font("Serif", Font.PLAIN, 10)
  }
  val currTurnText = new TextField("0") {
    columns = 4
  }
  var gameInitialized = false
  /* 
   * Need to track current turn number because of turn slider. If the value is set directly via code 
   * (e.g. to sync up when next turn is performed) the ValueChanged is invoked and the change is published again
   * using GoToTurn() which is not necessary and sometimes even bad, as the whole playground has to be re-rendered
   * 
   * If the ValueChanged(`turnSlider`) == currentTurnNo it means that the value has been changed directly via code
   * and no new event should be published.
   */
  var currentTurnNo = 0

  listenTo(eventBus)
  listenTo(currTurnText)
  listenTo(turnSlider)
  reactions += {
    case AfterNewReportChosen => gameInitialized = true
    case BeforeNewReportChosen => gameInitialized = false

    case ValueChanged(`intervalSlider`) =>
      eventBus.publish(TurnDelayChanged(intervalSlider.value))

    case ValueChanged(`turnSlider`) =>
      if (turnSlider.value != currentTurnNo && gameInitialized)
        eventBus.publish(GoToTurn(turnSlider.value))

    case EditDone(`currTurnText`) =>
      eventBus.publish(GoToTurn(currTurnText.text.toInt))
  }

  val rightBtns = new FlowPanel(FlowPanel.Alignment.Right)() {
    contents += new Label(" Current Turn ")
    contents += new BoxPanel(Orientation.Vertical) {
      contents += currTurnText
    }
    contents += turnSlider

    contents += prevTurnBtn
    contents += nextTurnBtn
    contents += replayBtn
    contents += restartBtn
  }

  layout(rightBtns) = BorderPanel.Position.East
  layout(new BoxPanel(Orientation.Horizontal) {
    //contents += new Label("Game progress ")
    //contents += progressBar
    contents += new Label("Turn delay ")
    contents += intervalSlider
  }) = BorderPanel.Position.West

  reactions += {
    case NewGameReportChosen(log, file) => {
      nextTurnBtn.enabled = true
      replayBtn.enabled = true
      replayBtn.text = "Replay"
      restartBtn.enabled = false
      //currentTurn = 0
      progressBar.value = 0
      progressBar.max = log.turns.size
      prevTurnBtn.enabled = false
      turnSlider.max = log.turns.size - 1
      turnSlider.value = -1
    }

    case NewGameCreated(_) =>
      prevTurnBtn.enabled = false
      nextTurnBtn.enabled = false
      replayBtn.enabled = true
      replayBtn.text = "Start"

    case GameStarted =>
      prevTurnBtn.enabled = false
      restartBtn.enabled = true

    case NextTurnPerformed(turnNo) =>
      currentTurnNo = turnNo
      progressBar.value = turnNo
      currTurnText.text = turnNo + ""
      turnSlider.value = turnNo
      gameStatus match {
        case GameReplaying => prevTurnBtn.enabled = false
        case _ => prevTurnBtn.enabled = true
      }

    case NextTurnAvailable =>
      nextTurnBtn.enabled = true

    case ReplayInitiated =>
      nextTurnBtn.enabled = false
      restartBtn.enabled = false
      replayBtn.enabled = true
      gameStatus = GameReplaying
      replayBtn.text = "Pause"
      prevTurnBtn.enabled = false

    case ReplayPaused =>
      restartBtn.enabled = true
      nextTurnBtn.enabled = true
      replayBtn.enabled = true
      gameStatus = GameReplayingPaused
      replayBtn.text = "Continue"
      prevTurnBtn.enabled = true

    case ReplayContinued =>
      restartBtn.enabled = false
      nextTurnBtn.enabled = false
      replayBtn.enabled = true
      gameStatus = GameReplaying
      replayBtn.text = "Pause"
      prevTurnBtn.enabled = false

    case GameFinished =>
      nextTurnBtn.enabled = false
      restartBtn.enabled = true
      replayBtn.enabled = false
      replayBtn.text = "Replay"
      gameStatus = GameNotStarted
      prevTurnBtn.enabled = true

    case GoToTurn(number) =>
      turnSlider.value = number
      currTurnText.text = number + ""
      if (number != 0)
        prevTurnBtn.enabled = true
      nextTurnBtn.enabled = true
      replayBtn.enabled = true
      replayBtn.text = "Continue"
      restartBtn.enabled = true
  }
}
