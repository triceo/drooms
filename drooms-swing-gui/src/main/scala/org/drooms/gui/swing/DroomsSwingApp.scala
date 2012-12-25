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
import scala.swing.Publisher
import org.drooms.gui.swing.event.PlaygroundGridEnabled
import org.drooms.gui.swing.event.PlaygroundGridDisabled

object DroomsSwingApp extends SimpleSwingApplication {
  // TODO leftPane here and own class for leftPane?
  //  var players = List[Player]()
  val leftPane = new LeftPane
  val rightPane = new RightPane

  def top = new MainFrame {
    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    title = "Drooms"
    minimumSize = new Dimension(1200, 700)
    menuBar = new MainMenu()
    leftPane.playground.listenTo(menuBar)
    contents = new SplitPane(Orientation.Vertical, leftPane, rightPane) {
      resizeWeight = 1.0
      rightComponent.minimumSize = new Dimension(200, 500)
      leftComponent.minimumSize = new Dimension(500, 500)
    }

    centerOnScreen()
  }
 
  class MainMenu extends MenuBar with Publisher {
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
        if (showGridBtn.selected)
          publish(new PlaygroundGridEnabled)
        else
          publish(new PlaygroundGridDisabled)
      }
    }
  }
}

class LeftPane extends BorderPanel {
  val playground = new Playground
  val controlPanel = new ControlPanel

  layout(playground) = BorderPanel.Position.Center
  layout(controlPanel) = BorderPanel.Position.South

  class ControlPanel extends FlowPanel(FlowPanel.Alignment.Right)() {
    val startBtn = new Button("Start game")
    val nextTurnBtn = new Button("Next turn")
    contents += nextTurnBtn
    contents += startBtn

    listenTo(startBtn)
    reactions += {
      case ButtonClicked(`startBtn`) =>
        playground.createNew(40, 60)
        createDummyPlayground
    }

    private def createDummyPlayground: Unit = {
      val p = new Player("", Color.CYAN)
      val p2 = new Player("", Color.YELLOW)
      playground.updateNode(new Worm(20, 20, "Body", p))
      playground.updateNode(new Worm(21, 20, "Body", p))
      playground.updateNode(new Worm(22, 20, "Body", p))
      playground.updateNode(new Worm(23, 20, "Body", p))
      playground.updateNode(new Worm(24, 20, "Body", p))
      playground.updateNode(new Worm(25, 20, "Body", p))
      playground.updateNode(new Worm(20, 19, "Body", p))
      playground.updateNode(new Worm(20, 18, "Body", p))
      playground.updateNode(new Worm(20, 17, "Body", p))
      playground.updateNode(new Worm(19, 17, "Head", p))

      playground.updateNode(new Worm(20, 50, "Body", p2))
      playground.updateNode(new Worm(21, 50, "Body", p2))
      playground.updateNode(new Worm(22, 50, "Head", p2))

      playground.updateNode(new Wall(22, 30))
      playground.updateNode(new Wall(22, 31))
      playground.updateNode(new Wall(23, 30))
      playground.updateNode(new Wall(23, 31))
      playground.updateNode(new Wall(24, 30))
      playground.updateNode(new Wall(24, 31))
      playground.updateNode(new Wall(25, 30))
      playground.updateNode(new Wall(25, 31))

      playground.updateNode(new Collectible(10, 10, 20))
    }
  }
}

class RightPane extends BoxPanel(Orientation.Horizontal) {
  val playersList = new PlayersList(
    List(
      new Player("Player 1", Color.CYAN),
      new Player("Player 2", Color.GREEN),
      new Player("Player 3", Color.GRAY),
      new Player("Player 4", Color.PINK),
      new Player("Player 5", Color.YELLOW)))
  contents += playersList
}
