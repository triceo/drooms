package org.drooms.gui.swing

import java.awt.Dimension
import java.io.File
import scala.swing.Action
import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.CheckMenuItem
import scala.swing.FileChooser
import scala.swing.FlowPanel
import scala.swing.MainFrame
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Orientation
import scala.swing.Publisher
import scala.swing.Reactor
import scala.swing.SimpleSwingApplication
import scala.swing.SplitPane
import scala.swing.event.ButtonClicked
import org.drooms.gui.swing.event.DroomsEventPublisher
import org.drooms.gui.swing.event.GameFinished
import org.drooms.gui.swing.event.NewGameLogChosen
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.PlaygroundGridDisabled
import org.drooms.gui.swing.event.PlaygroundGridEnabled
import org.drooms.gui.swing.event.TurnStepPerformed
import org.drooms.gui.swing.event.NewGameLogChosen
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.NextTurnInitiated

object DroomsSwingApp extends SimpleSwingApplication {
  val eventPublisher = DroomsEventPublisher.get()
  val leftPane = new LeftPane
  val rightPane = new RightPane
  var gameController: GameController = _
  var gameLog: (GameLog, File) = _

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
    reactions += {
      case NewGameLogChosen(log, file) =>
        gameLog = (log, file)
        gameController = new ReplayGameController(log)
      case NextTurnInitiated() =>
        val turn = gameController.nextTurn
        for (step <- turn.steps) {
          eventPublisher.publish(new TurnStepPerformed(step))
        }
        if (!gameController.hasNextTurn()) {
          eventPublisher.publish(new GameFinished)
        }
      case GameRestarted() => eventPublisher.publish(new NewGameLogChosen(gameLog._1, gameLog._2))
    }
    centerOnScreen()
    // dummy game
    eventPublisher.publish(new NewGameLogChosen(GameLog.loadFromXml(new File("/home/psiroky/work/git-repos/drooms/advanced-report-pretty.xml")), new File(".")))
  }

  class MainMenu extends MenuBar {
    val eventPublisher = DroomsEventPublisher.get()

    // file menu
    contents += new Menu("File") {
      contents += new MenuItem(Action("Open game log...") {
        openGameLog()
      })
    }
    // game menu
    val startGameItem = new MenuItem("Start") {
      enabled = false
    }
    val restartGameItem = new MenuItem("Restart") {
      enabled = false
    }
    contents += new Menu("Game") {
      contents += startGameItem
      contents += restartGameItem
    }
    // players menu
    contents += new Menu("Players") {
      contents += new MenuItem("Settings...")
    }
    val showGridBtn = new CheckMenuItem("Show grid")
    // playground menu
    contents += new Menu("Playground") {
      contents += showGridBtn
    }
    // help menu
    contents += new Menu("Help") {
      contents += new MenuItem("About Drooms")
    }
    listenTo(eventPublisher)
    listenTo(showGridBtn)
    listenTo(this)
    reactions += {
      case ButtonClicked(`showGridBtn`) => {
        if (showGridBtn.selected)
          eventPublisher.publish(new PlaygroundGridEnabled)
        else
          eventPublisher.publish(new PlaygroundGridDisabled)
      }
      case NewGameLogChosen(_, _) => {
        startGameItem.enabled = true
        restartGameItem.enabled = true
      }
    }

    def openGameLog(): Unit = {
      val fileChooser = new FileChooser(new File(System.getProperty("user.dir")))
      val res = fileChooser.showOpenDialog(this)
      if (res == FileChooser.Result.Approve) {
        val selectedFile = fileChooser.selectedFile
        val gameLog = GameLog.loadFromXml(selectedFile)
        eventPublisher.publish(new NewGameLogChosen(gameLog, selectedFile))
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

  class ControlPanel extends FlowPanel(FlowPanel.Alignment.Right)() with Reactor with Publisher {
    var currentLog: (GameLog, File) = _
    val startBtn = new Button("Start game") {
      enabled = false
    }
    val nextTurnBtn = new Button("Next turn") {
      enabled = false
    }
    val restartBtn = new Button("Restart game") {
      enabled = false
    }
    contents += nextTurnBtn
    contents += startBtn
    contents += restartBtn
    listenTo(eventPublisher)
    listenTo(nextTurnBtn, startBtn, restartBtn)

    reactions += {
      case NewGameLogChosen(log, file) => {
        //        currentLog = (log, file)
        nextTurnBtn.enabled = true
        startBtn.enabled = true
      }
      case ButtonClicked(`restartBtn`) => {
        eventPublisher.publish(new GameRestarted)
        //        eventPublisher.publish(new NewGameLogChosen(currentLog._1, currentLog._2))
      }
      case ButtonClicked(`nextTurnBtn`) =>
        restartBtn.enabled = true
        eventPublisher.publish(new NextTurnInitiated)
      case GameFinished() =>
        nextTurnBtn.enabled = false
    }
  }
}

class RightPane extends BoxPanel(Orientation.Horizontal) with Reactor {
  val eventPublisher = DroomsEventPublisher.get()
  val playersListView = new PlayersListView
  contents += playersListView

  listenTo(eventPublisher)
}
