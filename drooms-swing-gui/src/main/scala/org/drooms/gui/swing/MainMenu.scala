package org.drooms.gui.swing

import java.io.File
import scala.swing.Action
import scala.swing.CheckMenuItem
import scala.swing.FileChooser
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.event.ButtonClicked
import org.drooms.gui.swing.event.AfterNewReportChosen
import org.drooms.gui.swing.event.BeforeNewReportChosen
import org.drooms.gui.swing.event.CoordinantsVisibilityChanged
import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.NewGameReportChosen
import org.drooms.gui.swing.event.NewGameRequested
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.PlaygroundGridDisabled
import org.drooms.gui.swing.event.PlaygroundGridEnabled
import org.drooms.gui.swing.event.ReplayInitialized
import org.drooms.gui.swing.event.ReplayResetRequested
import org.drooms.gui.swing.event.ReplayStateChangeRequested
import org.drooms.gui.swing.event.ReplayStateChanged
import javax.swing.filechooser.FileFilter
import org.drooms.gui.swing.event.GameStateChangeRequested

/**
 * Represents application's main menu as standard {@link scala.swing.MenuBar}.
 */
class MainMenu extends MenuBar {
  // TODO make this configurable and saved as preference in user's home dir
  val REPORT_LOCATIONS = List(
    "../../drooms-game-impl/reports", // in case the app is started from drooms-swing-gui/target
    "../drooms-game-impl/reports" // in case the app is started from drooms-swing-gui
    )
  val eventBus = EventBusFactory.get()
  listenTo(eventBus)

  // file menu
  contents += new Menu("File") {
    contents += new MenuItem(Action("Quit") {
      // TODO send event and let main app handle the exit
      System.exit(0)
    })
  }

  // replay menu
  val nextTurnItem = new MenuItem(Action("Next turn") {
    eventBus.publish(NextTurnInitiated)
  }) {
    enabled = false
  }
  val replayStartItem = new MenuItem(Action("Replay") {
    eventBus.publish(ReplayStateChangeRequested(ReplayRunning))
  }) {
    enabled = false
  }
  val replayPauseItem = new MenuItem(Action("Pause") {
    eventBus.publish(ReplayStateChangeRequested(ReplayPaused))
  }) {
    enabled = false
  }
  val replayRestartItem = new MenuItem(Action("Reset") {
    eventBus.publish(ReplayResetRequested)
  }) {
    enabled = false
  }

  contents += new Menu("Replay") {
    contents += new MenuItem(Action("Open game report...") {
      openGameReport()
    })
    contents += nextTurnItem
    contents += replayStartItem
    contents += replayPauseItem
    contents += replayRestartItem
  }

  val showGridItem = new CheckMenuItem("Show grid")
  val showCoordsItem = new CheckMenuItem("Show axis numbers")

  // game menu
  val newGameItem = new MenuItem(Action("New game...") {
    eventBus.publish(NewGameRequested)
  }) {
    enabled = true
  }
  //  val startGame = new MenuItem(Action("Start") {
  //    eventBus.publish(GameStateChangeRequested(GameRunning))
  //  }) {
  //    enabled = false
  //  }
  contents += new Menu("Game") {
    contents += newGameItem
  }
  // playground menu
  contents += new Menu("Playground") {
    contents += showGridItem
    contents += showCoordsItem
  }
  // help menu
  contents += new Menu("Help") {
    contents += new MenuItem("About Drooms")
  }
  listenTo(showGridItem)
  listenTo(showCoordsItem)

  reactions += {
    case ButtonClicked(`showGridItem`) => {
      if (showGridItem.selected)
        eventBus.publish(PlaygroundGridEnabled)
      else
        eventBus.publish(PlaygroundGridDisabled)
    }
    case ButtonClicked(`showCoordsItem`) => {
      eventBus.publish(new CoordinantsVisibilityChanged(showCoordsItem.selected))
    }

    case NewGameReportChosen(_, _) => {
      replayStartItem.enabled = true
      replayPauseItem.enabled = false
      replayRestartItem.enabled = false
      nextTurnItem.enabled = true
    }
    case ReplayInitialized(report) =>
      replayStartItem.enabled = false
      replayPauseItem.enabled = true
      replayRestartItem.enabled = false
      nextTurnItem.enabled = false
    case ReplayStateChanged(toState) =>
      toState match {
        case ReplayNotStarted =>
          replayStartItem.enabled = true
          replayPauseItem.enabled = false
          replayRestartItem.enabled = false
          nextTurnItem.enabled = true

        case ReplayRunning =>
          replayStartItem.enabled = true
          replayPauseItem.enabled = false
          replayRestartItem.enabled = true
          nextTurnItem.enabled = true

        case ReplayPaused =>
          replayStartItem.enabled = false
          replayPauseItem.enabled = false
          replayRestartItem.enabled = true
          nextTurnItem.enabled = true

        case ReplayFinished =>
          replayStartItem.enabled = true
          replayPauseItem.enabled = false
          replayRestartItem.enabled = false
          nextTurnItem.enabled = true
      }
  }

  var lastUsedDir = {
    var openIn = new File(System.getProperty("user.dir"))
    for (dirName <- REPORT_LOCATIONS) {
      val dir = new File(dirName)
      if (dir.exists()) {
        openIn = dir
      }
    }
    openIn
  }

  val xmlFileFilter = new FileFilter() {
    // filter files, because the reports are saved in XML
    override def accept(f: File): Boolean = {
      f.getPath().endsWith(".xml") || f.isDirectory()
    }
    override def getDescription() = "XML report file"
  }

  def openGameReport(): Unit = {
    val fileChooser = new FileChooser(lastUsedDir)
    fileChooser.fileFilter = xmlFileFilter
    val res = fileChooser.showOpenDialog(this)
    if (res == FileChooser.Result.Approve) {
      eventBus.publish(BeforeNewReportChosen)
      val selectedFile = fileChooser.selectedFile
      lastUsedDir = selectedFile.getParentFile()
      val gameReport = GameReport.loadFromXml(selectedFile)
      eventBus.publish(new NewGameReportChosen(gameReport, selectedFile))
      eventBus.publish(AfterNewReportChosen)
    }
  }
}
