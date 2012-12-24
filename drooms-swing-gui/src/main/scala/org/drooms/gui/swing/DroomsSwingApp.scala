package org.drooms.gui.swing

import java.awt.Dimension
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.BorderPanel.Position.South
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.MainFrame
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Orientation
import scala.swing.SimpleSwingApplication
import scala.swing.SplitPane
import scala.swing.event.ButtonClicked
import scala.swing.CheckMenuItem
import scala.swing.Action
import scala.swing.ScrollPane
import scala.swing.event.ButtonClicked
import java.awt.Color
import scala.swing.FlowPanel
import scala.swing.Alignment

object DroomsSwingApp extends SimpleSwingApplication {
  // TODO leftPane here and own class for leftPane?
  var players = List[Player]()
  var playground = new BoxPanel(Orientation.Horizontal)
  var playgr = new Playground(10, 10)

  def top = new MainFrame {
    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    title = "Drooms"
    minimumSize = new Dimension(1200, 700)
    menuBar = new MainMenu()

    val btn = new Button("Control Panel")
    val controlPanel = new FlowPanel(FlowPanel.Alignment.Right)(btn)
    // left pane - playground + game controls
    val leftPane =
      new BorderPanel() {
        import BorderPanel.Position._
        //                layout(playground) = Center
        //                layout(new Button("Control Panel")) = South
        layout(playground) = Center
        layout(controlPanel) = South
      }
    val rightPane = new PlayersList(
      List(new Player("Player 1", Color.CYAN),
        new Player("Player 2", Color.GREEN),
        new Player("Player 3", Color.GRAY),
        new Player("Player 4", Color.PINK),
        new Player("Player 5", Color.YELLOW)))

    contents = new SplitPane(Orientation.Vertical, leftPane, rightPane) {
      resizeWeight = 1.0
      rightComponent.minimumSize = new Dimension(200, 500)
      leftComponent.minimumSize = new Dimension(500, 500)
    }
    setPlayground(new Playground(40, 60))
    listenTo(btn)
    reactions += {
      case ButtonClicked(`btn`) =>
        val p = new Player("", Color.CYAN)
        val p2 = new Player("", Color.YELLOW)
        playgr.updateNode(new Worm(20, 20, "Body", p))
        playgr.updateNode(new Worm(21, 20, "Body", p))
        playgr.updateNode(new Worm(22, 20, "Body", p))
        playgr.updateNode(new Worm(23, 20, "Body", p))
        playgr.updateNode(new Worm(24, 20, "Body", p))
        playgr.updateNode(new Worm(25, 20, "Body", p))
        playgr.updateNode(new Worm(20, 19, "Body", p))
        playgr.updateNode(new Worm(20, 18, "Body", p))
        playgr.updateNode(new Worm(20, 17, "Body", p))
        playgr.updateNode(new Worm(19, 17, "Head", p))

        playgr.updateNode(new Worm(20, 50, "Body", p2))
        playgr.updateNode(new Worm(21, 50, "Body", p2))
        playgr.updateNode(new Worm(22, 50, "Head", p2))

        playgr.updateNode(new Wall(22, 30))
        playgr.updateNode(new Wall(22, 31))
        playgr.updateNode(new Wall(22, 32))
        playgr.updateNode(new Wall(23, 30))
        playgr.updateNode(new Wall(23, 31))
        playgr.updateNode(new Wall(23, 32))
        playgr.updateNode(new Wall(24, 30))
        playgr.updateNode(new Wall(25, 30))

        playgr.updateNode(new Collectible(10, 10, 20))
    }

    centerOnScreen()

  }

  def setPlayground(newPlayground: Playground): Unit = {
    playground.contents.clear
    playground.contents += newPlayground
    playground.revalidate
    playground.repaint
    playgr = newPlayground
  }

  def hidePlaygroundGrid(): Unit = playgr.hideGrid

  def showPlaygroundGrid(): Unit = playgr.showGrid

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
    val showGridBtn = new CheckMenuItem("Show grid")
    contents += new Menu("Playground") {
      contents += showGridBtn
    }
    contents += new Menu("Help") {
      contents += new MenuItem("About Drooms")
    }
    listenTo(showGridBtn)
    reactions += {
      case ButtonClicked(`showGridBtn`) => {
        if (showGridBtn.selected) showPlaygroundGrid()
        else hidePlaygroundGrid()
      }

    }
  }
}

class ControlPanel extends FlowPanel {

}

class LeftPane extends BorderPanel {
  var playground = new BoxPanel(Orientation.Horizontal)
  val controlPanel = new Button("Control Panel")
}

class RigthPane extends BoxPanel(Orientation.Horizontal) {
  val playersList = new PlayersList(List())
}

