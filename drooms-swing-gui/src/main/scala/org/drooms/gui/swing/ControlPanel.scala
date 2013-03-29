package org.drooms.gui.swing

import java.awt.Font
import scala.swing.Action
import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.Publisher
import scala.swing.Reactor
import scala.swing.Slider
import scala.swing.TextField
import scala.swing.event.EditDone
import scala.swing.event.ValueChanged
import org.drooms.gui.swing.event.AfterNewReportChosen
import org.drooms.gui.swing.event.BeforeNewReportChosen
import org.drooms.gui.swing.event.EventBus
import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.GameStateChangeRequested
import org.drooms.gui.swing.event.GameStateChangeRequested
import org.drooms.gui.swing.event.GameStateChanged
import org.drooms.gui.swing.event.GoToTurn
import org.drooms.gui.swing.event.NewGameCreated
import org.drooms.gui.swing.event.NewTurnAvailable
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.NextTurnPerformed
import org.drooms.gui.swing.event.PreviousTurnRequested
import org.drooms.gui.swing.event.ReplayInitialized
import org.drooms.gui.swing.event.ReplayResetRequested
import org.drooms.gui.swing.event.ReplayStateChangeRequested
import org.drooms.gui.swing.event.ReplayStateChanged
import org.drooms.gui.swing.event.TurnDelayChanged
import org.drooms.gui.swing.event.NewUIComponentsRequested
import org.drooms.gui.swing.event.GameRestartRequested
import org.drooms.gui.swing.event.ReplayStateChanged

/**
 * Control panel with controls used for replay.
 */
class ReplayControlPanel(eventBus: EventBus) extends ControlPanel(eventBus) {
  private var replayState: ReplayState = ReplayNotStarted

  private var replayInitialized = false
  /* 
   * Need to track current turn number because of turn slider. If the value is set directly via code 
   * (e.g. to sync up when next turn is performed) the ValueChanged is invoked and the change is published again
   * using GoToTurn() which is not necessary and sometimes even bad, as the whole playground has to be re-rendered
   * 
   * If the ValueChanged(`turnSlider`) == currentTurnNo it means that the value has been changed directly via code
   * and no new event should be published.
   */
  private var currentTurnNo = 0

  val replayBtn = new Button(Action("Replay") {
    val event = replayState match {
      case ReplayNotStarted =>
        ReplayStateChangeRequested(ReplayRunning)
      case ReplayRunning =>
        ReplayStateChangeRequested(ReplayPaused)
      case ReplayPaused =>
        ReplayStateChangeRequested(ReplayRunning)
      case ReplayFinished =>
        ReplayStateChanged(ReplayRunning)
    }
    eventBus.publish(event)
  }) {
    enabled = false
  }
  val prevTurnBtn = new Button(Action("Previous turn") {
    eventBus.publish(PreviousTurnRequested)
  }) {
    enabled = false
  }
  val nextTurnBtn = new Button(Action("Next turn") {
    eventBus.publish(NextTurnInitiated)
  }) {
    enabled = false
  }
  val restartBtn = new Button(Action("Reset replay") {
    eventBus.publish(ReplayResetRequested)
  }) {
    enabled = false
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

  listenTo(intervalSlider)
  listenTo(currTurnText)
  listenTo(turnSlider)

  reactions += {
    case AfterNewReportChosen => replayInitialized = true
    case BeforeNewReportChosen => replayInitialized = false

    case ValueChanged(`intervalSlider`) =>
      eventBus.publish(TurnDelayChanged(intervalSlider.value))

    case ValueChanged(`turnSlider`) =>
      if (turnSlider.value != currentTurnNo && replayInitialized)
        eventBus.publish(GoToTurn(turnSlider.value))

    case EditDone(`currTurnText`) =>
      eventBus.publish(GoToTurn(currTurnText.text.toInt))

    // GUI was created and is ready to accept users actions
    case ReplayInitialized(report) =>
      nextTurnBtn.enabled = true
      prevTurnBtn.enabled = false
      replayBtn.enabled = true
      replayBtn.text = "Replay"
      restartBtn.enabled = false
      turnSlider.value = -1
      turnSlider.max = report.turns.size - 1

    case NextTurnPerformed(turnNo) =>
      replayState match {
        case ReplayRunning =>
          restartBtn.enabled = false
          prevTurnBtn.enabled = false
          nextTurnBtn.enabled = false

        case _ =>
          restartBtn.enabled = true
          prevTurnBtn.enabled = true
        //nextTurnBtn.enabled = false
      }
      currentTurnNo = turnNo
      currTurnText.text = turnNo + ""
      turnSlider.value = turnNo

    case NewTurnAvailable(_, _) =>
      replayInitialized = true
      replayState match {
        case ReplayRunning =>
        case _ =>
          nextTurnBtn.enabled = true
      }
      replayBtn.enabled = true

    case ReplayStateChanged(newState) => {
      newState match {
        case ReplayNotStarted =>
          nextTurnBtn.enabled = true
          prevTurnBtn.enabled = false
          restartBtn.enabled = false
          replayBtn.enabled = true
          replayBtn.text = "Start replay"

        case ReplayRunning =>
          nextTurnBtn.enabled = false
          prevTurnBtn.enabled = false
          restartBtn.enabled = false
          replayBtn.enabled = true
          replayBtn.text = "Pause replay"

        case ReplayPaused =>
          nextTurnBtn.enabled = true
          prevTurnBtn.enabled = true
          restartBtn.enabled = true
          replayBtn.enabled = true
          replayBtn.text = "Continue replay"

        case ReplayFinished =>
          nextTurnBtn.enabled = false
          prevTurnBtn.enabled = true
          restartBtn.enabled = true
          replayBtn.enabled = false
          replayBtn.text = "Continue replay"
      }
      replayState = newState
    }

    case GoToTurn(number) =>
      currentTurnNo = number
      turnSlider.value = number
      currTurnText.text = number + ""
      if (number != 0) {
        prevTurnBtn.enabled = true
      } else {
        prevTurnBtn.enabled = false
      }
      nextTurnBtn.enabled = true
      replayBtn.enabled = true
      replayBtn.text = "Continue"
      restartBtn.enabled = true
  }

  layout(rightBtns) = BorderPanel.Position.East
  layout(new BoxPanel(Orientation.Horizontal) {
    contents += new Label("Turn delay ")
    contents += intervalSlider
  }) = BorderPanel.Position.West
}

/**
 * Control panel specific for real-time game.
 */
class RealTimeGameControlPanel(eventBus: EventBus) extends ControlPanel(eventBus) {
  var gameState: GameState = GameNotStarted

  val startPauseBtn = new Button(Action("Start game") {
    val toState = gameState match {
      case GameNotStarted | GamePaused =>
        GameRunning
      case GameRunning =>
        GameStopped
    }
    eventBus.publish(GameStateChangeRequested(toState))
  }) {
    enabled = false
  }

  val restartBtn = new Button(Action("Restart game") {
    eventBus.publish(GameRestartRequested)
  }) {
    enabled = false
  }
  val btnsPanel = new FlowPanel {
    contents += startPauseBtn
    contents += restartBtn
  }

  reactions += {
    case NewGameCreated(config) =>
      startPauseBtn.enabled = true
      startPauseBtn.text = "Start game"
      restartBtn.enabled = false

    case GameStateChanged(newState) =>
      newState match {
        case GameNotStarted =>
          startPauseBtn.enabled = true
          startPauseBtn.text = "Start game"
          restartBtn.enabled = false

        case GameRunning =>
          startPauseBtn.enabled = true
          startPauseBtn.text = "Stop game"
          restartBtn.enabled = true

        case GamePaused =>
          startPauseBtn.enabled = true
          startPauseBtn.text = "Continue game"
          restartBtn.enabled = true

        case GameStopped =>
          startPauseBtn.enabled = false
          restartBtn.enabled = true

        case GameFinished =>
          startPauseBtn.enabled = false
      }
      gameState = newState
  }
  layout(btnsPanel) = BorderPanel.Position.East
}

object ControlPanel {
  val StartReplayText = "Start replay"
  val PauseReplayText = "Pause replay"

  def newReplayControlPanel(): ControlPanel = {
    new ReplayControlPanel(EventBusFactory.get())
  }

  def newRealTimeGameControlPanel(): ControlPanel = {
    new RealTimeGameControlPanel(EventBusFactory.get())
  }
}

/**
 * Control panel located at the bottom of the window
 */
class ControlPanel(val eventBus: EventBus) extends BorderPanel with Reactor with Publisher {
  listenTo(eventBus)
  reactions += {
    case NewUIComponentsRequested =>
      eventBus.deafTo(this)
      deafTo(eventBus)
  }
}
