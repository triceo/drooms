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

object DroomsSwingApp extends SimpleSwingApplication {

  def top = new MainFrame {
    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    title = "Drooms"
    minimumSize = new Dimension(750, 500)
    menuBar = new MainMenu()

    val btn = new Button("Test")
    val playground = new Playground()
    val playersList = new PlayersList(List("Player 1", "Player 2"))

    contents = new SplitPane(Orientation.Vertical, playground, playersList) {
      resizeWeight = 1.0
      rightComponent.minimumSize = new Dimension(150, 500)
      leftComponent.minimumSize = new Dimension(500, 500)

    }
    listenTo(btn)
    reactions += {
      case ButtonClicked(`btn`) =>
        playersList.addPlayer("Another player")
    }
    centerOnScreen()

  }
}