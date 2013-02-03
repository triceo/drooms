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
import scala.swing.event.ValueChanged

import org.drooms.gui.swing.event.DroomsEventPublisher
import org.drooms.gui.swing.event.GameFinished
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.NewGameReportChosen
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.ReplayContinued
import org.drooms.gui.swing.event.ReplayInitiated
import org.drooms.gui.swing.event.ReplayPaused
import org.drooms.gui.swing.event.TurnDelayChanged

class ControlPanel extends BorderPanel with Reactor with Publisher {
  val eventPublisher = DroomsEventPublisher.get()
  var currentLog: (GameReport, File) = _
  var currentTurn = 0
  var gameStatus: GameStatus = GameNotStarted()
  val replayPauseBtn = new Button(Action("Replay") {
    gameStatus match {
      case GameNotStarted() =>
        eventPublisher.publish(ReplayInitiated())
      case GameReplaying() =>
        eventPublisher.publish(ReplayPaused())
      case GameReplayingPaused() =>
        eventPublisher.publish(ReplayContinued())
    }
  }) {
    enabled = false
  }
  val prevTurnBtn = new Button(Action("Previous turn") {

  })
  val nextTurnBtn = new Button(Action("Next turn") {
    eventPublisher.publish(NextTurnInitiated())
  }) {
    enabled = false
  }
  val restartBtn = new Button(Action("Reset game") {
    eventPublisher.publish(new GameRestarted)
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
    //minorTickSpacing = 100
    majorTickSpacing = 250
    font = new Font("Serif", Font.PLAIN, 10)
  }
  listenTo(intervalSlider)
  val turnSlider = new Slider {
    min = 0
    max = 1000
    value = 100
    paintLabels = true
    majorTickSpacing = 250
    font = new Font("Serif", Font.PLAIN, 10)
  }
  listenTo(turnSlider)
  reactions += {
    case ValueChanged(`intervalSlider`) =>
      eventPublisher.publish(TurnDelayChanged(intervalSlider.value))
  }

  val rightBtns = new FlowPanel(FlowPanel.Alignment.Right)() {
    contents += new Label(" Current Turn ")
    contents += new BoxPanel(Orientation.Vertical) {
      contents += new TextField("0") {
        
      }
    }
    contents += turnSlider

    contents += prevTurnBtn
    contents += nextTurnBtn
    contents += replayPauseBtn
    contents += restartBtn
  }

  layout(rightBtns) = BorderPanel.Position.East
  layout(new BoxPanel(Orientation.Horizontal) {
    //contents += new Label("Game progress ")
    //contents += progressBar
    contents += new Label("Turn delay ")
    contents += intervalSlider
  }) = BorderPanel.Position.West

  listenTo(eventPublisher)

  reactions += {
    case NewGameReportChosen(log, file) => {
      nextTurnBtn.enabled = true
      replayPauseBtn.enabled = true
      replayPauseBtn.text = "Replay"
      restartBtn.enabled = false
      currentTurn = 0
      progressBar.value = 0
      progressBar.max = log.turns.size
    }
    case NextTurnInitiated() =>
      currentTurn += 1
      progressBar.value = currentTurn
    case ReplayInitiated() =>
      nextTurnBtn.enabled = false
      restartBtn.enabled = false
      replayPauseBtn.enabled = true
      gameStatus = GameReplaying()
      replayPauseBtn.text = "Pause"
    case ReplayPaused() =>
      restartBtn.enabled = true
      nextTurnBtn.enabled = true
      replayPauseBtn.enabled = true
      gameStatus = GameReplayingPaused()
      replayPauseBtn.text = "Continue"
    case ReplayContinued() =>
      restartBtn.enabled = false
      nextTurnBtn.enabled = false
      replayPauseBtn.enabled = true
      gameStatus = GameReplaying()
      replayPauseBtn.text = "Pause"
    case GameFinished() =>
      nextTurnBtn.enabled = false
      restartBtn.enabled = true
      replayPauseBtn.enabled = false
      replayPauseBtn.text = "Replay"
      gameStatus = GameNotStarted()
  }
}