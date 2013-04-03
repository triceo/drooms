package org.drooms.gui.swing

import scala.swing.Dialog
import java.awt.Dimension
import scala.swing.Label
import scala.swing.Button
import scala.swing.BorderPanel
import scala.swing.FlowPanel
import scala.swing.event.ButtonClicked
import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.TextArea
import javax.swing.BorderFactory
import jxl.write.VerticalAlignment
import scala.swing.Alignment
import java.awt.Font

/**
 * About dialog with some very basic information about the application and its authors.
 */
class AboutDialog extends Dialog {
  title = "About Drooms Swing GUI"
  modal = false
  resizable = false
  minimumSize = new Dimension(500,300)
  
  val okBtn = new Button("OK")
  val mainArea = new BoxPanel(Orientation.Vertical) {
    border = BorderFactory.createEmptyBorder(30, 30, 30, 30)
    contents += new Label {
      font = new Font("sans-serif", Font.PLAIN, 14)
      text = 
        """<html>
            <center><b>Drooms Swing GUI lets you browse Drooms replays <br/> and play real-time Drooms games.</b>
            <br/>
            <br/>
            <b>Home page:</b> triceo.github.com/drooms
            <br/><br/><br/><br/>
            </center>
              <ul><b>Authors:</b>
                <li>Lukáš Petrovický - Drooms Game implementation
                <li>Petr Široký - Drooms Swing GUI application
              </ul>
        """
    }
  }
  
  contents = new BorderPanel {
    layout(mainArea) = BorderPanel.Position.Center
    layout(new FlowPanel(FlowPanel.Alignment.Center)() {
     contents += okBtn 
    }) = BorderPanel.Position.South
  }
  
  listenTo(okBtn)
  reactions += {
    case ButtonClicked(`okBtn`) =>
      visible = false
      dispose()
  }
  
  def show(): Unit = {
    centerOnScreen()
    visible = true
  }
}
