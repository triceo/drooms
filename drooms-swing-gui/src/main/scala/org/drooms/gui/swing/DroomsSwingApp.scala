package org.drooms.gui.swing

import scala.swing.SimpleSwingApplication
import scala.swing.Button
import scala.swing.MainFrame
import java.awt.Dimension
import scala.swing.SplitPane
import scala.swing.Orientation
import scala.swing.event.ButtonClicked
import scala.swing.Table
import javax.swing.UIManager
import scala.swing.MenuBar
import scala.swing.Menu
import scala.swing.MenuItem
import scala.swing.BorderPanel
import scala.swing.BoxPanel

object DroomsSwingApp extends SimpleSwingApplication {

  def top = new MainFrame {
    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    title = "Drooms"
    minimumSize = new Dimension(750, 500)
    menuBar = new MainMenu()

    val btn = new Button("Test")
    // left pane - playground + game controls
    val leftPane = new BoxPanel(Orientation.Horizontal) {
      val playground = new Playground()
      import BorderPanel.Position._
      //      layout(playground) = Center
      //      layout(new Button("Control Panel")) = South
      contents += playground
    }
    val rightPane = new PlayersList(List("Player 1", "Player 2"))

    contents = new SplitPane(Orientation.Vertical, leftPane, rightPane) {
      resizeWeight = 1.0
      rightComponent.minimumSize = new Dimension(150, 500)
      leftComponent.minimumSize = new Dimension(500, 500)

    }
    listenTo(btn)
    reactions += {
      case ButtonClicked(`btn`) =>
        rightPane.addPlayer("Another player")
    }
    centerOnScreen()

  }
}

class MainMenu extends MenuBar {
  contents += new Menu("File") {
    contents += new MenuItem("Open game log...")
  }
  contents += new Menu("Game") {
    contents += new MenuItem("Start")
    contents += new MenuItem("Restart")
  }
  contents += new Menu("Players") {
    contents += new MenuItem("Settings...")
  }
  contents += new Menu("Help") {
    contents += new MenuItem("About Drooms")
  }
}