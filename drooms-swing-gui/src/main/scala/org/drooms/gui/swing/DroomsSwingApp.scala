package org.drooms.gui.swing

import java.awt.Dimension
import java.io.File
import java.util.Timer
import java.util.TimerTask
import scala.swing.Action
import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.CheckMenuItem
import scala.swing.FileChooser
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Orientation
import scala.swing.ProgressBar
import scala.swing.Publisher
import scala.swing.Reactor
import scala.swing.SimpleSwingApplication
import scala.swing.SplitPane
import scala.swing.event.ButtonClicked
import org.drooms.gui.swing.event.DroomsEventPublisher
import org.drooms.gui.swing.event.GameFinished
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.NewGameReportChosen
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.PlaygroundGridDisabled
import org.drooms.gui.swing.event.PlaygroundGridEnabled
import org.drooms.gui.swing.event.ReplayContinued
import org.drooms.gui.swing.event.ReplayInitiated
import org.drooms.gui.swing.event.ReplayPaused
import org.drooms.gui.swing.event.TurnStepPerformed
import org.drooms.gui.swing.event.ReplayPaused
import org.drooms.gui.swing.event.ReplayContinued
import org.drooms.gui.swing.event.ReplayInitiated
import org.drooms.gui.swing.event.ReplayPaused
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.ReplayInitiated
import org.drooms.gui.swing.event.ReplayPaused
import org.drooms.gui.swing.event.ReplayContinued
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.NextTurnInitiated
import scala.swing.Slider
import java.awt.Font
import scala.swing.event.ValueChanged
import org.drooms.gui.swing.event.TurnDelayChanged
import org.drooms.gui.swing.event.TurnDelayChanged
import org.drooms.gui.swing.event.GameFinished

object DroomsSwingApp extends SimpleSwingApplication {
  val eventPublisher = DroomsEventPublisher.get()
  val leftPane = new LeftPane
  val rightPane = new RightPane
  var gameController: GameController = _
  var gameReport: (GameReport, File) = _
  var turnDelay = 100

  def top = new MainFrame {
    title = "Drooms"
    minimumSize = new Dimension(1200, 700)
    menuBar = new MainMenu()
    listenTo(eventPublisher)

    contents = new SplitPane(Orientation.Vertical, leftPane, rightPane) {
      resizeWeight = 1.0
      rightComponent.minimumSize = new Dimension(200, 500)
      leftComponent.minimumSize = new Dimension(500, 500)
    }
    var timer: Option[Timer] = None

    reactions += {
      case NewGameReportChosen(report, file) =>
        gameReport = (report, file)
        gameController = new ReplayGameController(report)
      case NextTurnInitiated() =>
        val turn = gameController.nextTurn
        for (step <- turn.steps) {
          eventPublisher.publish(new TurnStepPerformed(step))
        }
        if (!gameController.hasNextTurn()) {
          eventPublisher.publish(new GameFinished)
        }
      case GameRestarted() =>
        eventPublisher.publish(new NewGameReportChosen(gameReport._1, gameReport._2))
      case ReplayInitiated() | ReplayContinued() =>
        timer match {
          case Some(x) => 
            x.cancel()
          case None =>
        }
        timer = Some(new Timer())
        timer.get.schedule(new ScheduleNextTurn(), 0, turnDelay)
      case ReplayPaused() =>
        timer.get.cancel()
        timer = None
      case TurnDelayChanged(value) =>
        turnDelay = value
        timer match {
          // currently running replay;; update timer to new delay
          case Some(x) =>
            x.cancel()
            timer = Some(new Timer())
            timer.get.schedule(new ScheduleNextTurn(), turnDelay, turnDelay)
          case None =>
        }
      case GameFinished() =>
        timer match {
          case Some(x) => x.cancel()
          case None =>
        }
        timer = None
    }

    class ScheduleNextTurn extends TimerTask {
      def run(): Unit = {
        if (gameController.hasNextTurn()) {
          eventPublisher.publish(NextTurnInitiated())
        }
      }
    }
    centerOnScreen()
    // dummy game
    eventPublisher.publish(new NewGameReportChosen(GameReport.loadFromXml(new File("/home/psiroky/work/git-repos/drooms/advanced-report-pretty.xml")), new File(".")))
  }

  class MainMenu extends MenuBar {
    val eventPublisher = DroomsEventPublisher.get()
    listenTo(eventPublisher)
    // file menu
    contents += new Menu("File") {
      contents += new MenuItem(Action("Open game report...") {
        openGameReport()
      })
      //      contents += new MenuItem(Action("Exit") {
      //      })
    }
    // game menu
    val nextTurnItem = new MenuItem(Action("Next turn") {
      eventPublisher.publish(NextTurnInitiated())
    }) {
      enabled = false
    }
    val replayItem = new MenuItem(Action("Replay") {
      eventPublisher.publish(ReplayInitiated())
    }) {
      enabled = false
    }
    val pauseItem = new MenuItem(Action("Pause") {
      eventPublisher.publish(ReplayPaused())
    }) {
      enabled = false
    }
    val restartItem = new MenuItem(Action("Reset") {
      eventPublisher.publish(GameRestarted())
    }) {
      enabled = false
    }

    contents += new Menu("Game") {
      contents += nextTurnItem
      contents += replayItem
      contents += pauseItem
      contents += restartItem
    }
    reactions += {
      case ReplayInitiated() =>
        replayItem.enabled = false
        pauseItem.enabled = true
        restartItem.enabled = false
        nextTurnItem.enabled = false
      case ReplayPaused() =>
        replayItem.enabled = true
        pauseItem.enabled = false
        restartItem.enabled = true
        nextTurnItem.enabled = true
      case ReplayContinued() =>
        replayItem.enabled = false
        pauseItem.enabled = true
        restartItem.enabled = false
        nextTurnItem.enabled = false
      case GameRestarted() =>
        replayItem.enabled = true
        pauseItem.enabled = false
        restartItem.enabled = false
        nextTurnItem.enabled = true
      case NextTurnInitiated() =>
        replayItem.enabled = true
        pauseItem.enabled = false
        restartItem.enabled = true
        nextTurnItem.enabled = true
    }
    // players menu
    //    contents += new Menu("Players") {
    //      contents += new MenuItem("Settings...")
    //    }
    val showGridItem = new CheckMenuItem("Show grid")
    // playground menu
    contents += new Menu("Playground") {
      contents += showGridItem
    }
    // help menu
    contents += new Menu("Help") {
      contents += new MenuItem("About Drooms")
    }
    listenTo(showGridItem)
    reactions += {
      case ButtonClicked(`showGridItem`) => {
        if (showGridItem.selected)
          eventPublisher.publish(new PlaygroundGridEnabled)
        else
          eventPublisher.publish(new PlaygroundGridDisabled)
      }
      case NewGameReportChosen(_, _) => {
        replayItem.enabled = true
        pauseItem.enabled = false
        restartItem.enabled = false
        nextTurnItem.enabled = true
      }
    }

    def openGameReport(): Unit = {
      val fileChooser = new FileChooser(new File(System.getProperty("user.dir")))
      val res = fileChooser.showOpenDialog(this)
      if (res == FileChooser.Result.Approve) {
        val selectedFile = fileChooser.selectedFile
        val gameReport = GameReport.loadFromXml(selectedFile)
        eventPublisher.publish(new NewGameReportChosen(gameReport, selectedFile))
      }
    }
  }
}

class LeftPane extends BorderPanel {
  val eventPublisher = DroomsEventPublisher.get()
  val playground = new Playground
  val controlPanel = new ControlPanel

  layout(playground) = BorderPanel.Position.Center
  layout(controlPanel) = BorderPanel.Position.South

  class ControlPanel extends BorderPanel with Reactor with Publisher {
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
    reactions += {
      case ValueChanged(`intervalSlider`) =>
        eventPublisher.publish(TurnDelayChanged(intervalSlider.value))
    }

    val rightBtns = new FlowPanel(FlowPanel.Alignment.Right)() {
      contents += new Label("Turn delay ")
      contents += intervalSlider

      contents += nextTurnBtn
      contents += replayPauseBtn
      contents += restartBtn
    }

    layout(rightBtns) = BorderPanel.Position.East
    layout(new BoxPanel(Orientation.Horizontal) {
      contents += new Label("Game progress ")
      contents += progressBar
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
}

class RightPane extends BoxPanel(Orientation.Horizontal) with Reactor {
  val eventPublisher = DroomsEventPublisher.get()
  val playersListView = new PlayersListView
  contents += playersListView

  listenTo(eventPublisher)
}

trait GameStatus
case class GameNotStarted extends GameStatus
case class GameReplaying extends GameStatus
case class GameReplayingPaused extends GameStatus
