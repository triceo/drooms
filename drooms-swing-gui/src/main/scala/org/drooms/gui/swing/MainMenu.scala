package org.drooms.gui.swing

import java.io.File

import scala.swing.Action
import scala.swing.CheckMenuItem
import scala.swing.FileChooser
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.event.ButtonClicked

import org.drooms.gui.swing.event.CoordinantsVisibilityChanged
import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.NewGameReportChosen
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.PlaygroundGridDisabled
import org.drooms.gui.swing.event.PlaygroundGridEnabled
import org.drooms.gui.swing.event.ReplayContinued
import org.drooms.gui.swing.event.ReplayInitiated
import org.drooms.gui.swing.event.ReplayPaused

import javax.swing.filechooser.FileFilter

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
    contents += new MenuItem(Action("Open game report...") {
      openGameReport()
    })
    //      contents += new MenuItem(Action("Exit") {
    //      })
  }
  // game menu
  val nextTurnItem = new MenuItem(Action("Next turn") {
    eventBus.publish(NextTurnInitiated())
  }) {
    enabled = false
  }
  val replayItem = new MenuItem(Action("Replay") {
    eventBus.publish(ReplayInitiated())
  }) {
    enabled = false
  }
  val pauseItem = new MenuItem(Action("Pause") {
    eventBus.publish(ReplayPaused())
  }) {
    enabled = false
  }
  val restartItem = new MenuItem(Action("Reset") {
    eventBus.publish(GameRestarted())
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
  val showCoordsItem = new CheckMenuItem("Show axis numbers")
  // playground menu
  contents += new Menu("Playground") {
    contents += showGridItem
    contents += showCoordsItem
  }
  // help menu
  //    contents += new Menu("Help") {
  //      contents += new MenuItem("About Drooms")
  //    }
  listenTo(showGridItem)
  listenTo(showCoordsItem)
  reactions += {
    case ButtonClicked(`showGridItem`) => {
      if (showGridItem.selected)
        eventBus.publish(new PlaygroundGridEnabled)
      else
        eventBus.publish(new PlaygroundGridDisabled)
    }
    case ButtonClicked(`showCoordsItem`) => {
      eventBus.publish(new CoordinantsVisibilityChanged(showCoordsItem.selected))
    }

    case NewGameReportChosen(_, _) => {
      replayItem.enabled = true
      pauseItem.enabled = false
      restartItem.enabled = false
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
      val selectedFile = fileChooser.selectedFile
      lastUsedDir = selectedFile.getParentFile()
      val gameReport = GameReport.loadFromXml(selectedFile)
      eventBus.publish(new NewGameReportChosen(gameReport, selectedFile))
    }
  }
}
