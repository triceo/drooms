package org.drooms.gui.swing

import java.awt.Color
import java.awt.Dimension
import java.io.File
import scala.swing.Alignment
import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.FileChooser
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.TextField
import scala.swing.event.ButtonClicked
import javax.swing.BorderFactory
import javax.swing.Box
import scala.swing.event.Key
import scala.swing.event.KeyPressed

class NewGameDialog extends Dialog {
  modal = true
  minimumSize = new Dimension(700, 300)
  title = "New Game"

  var submitted = false
  
  val leftColumn = new BoxPanel(Orientation.Vertical) {
    xLayoutAlignment = 0.0
    contents += new Label("Game configuration") {
      horizontalAlignment = Alignment.Trailing

    }
    peer.add(Box.createVerticalStrut(10))
    contents += new Label("Playground definition") {
      xLayoutAlignment = 0.0
      horizontalAlignment = Alignment.Trailing
    }
  }

  val playgrUpload = new SelectFileLine()
  val configUpload = new SelectFileLine()
  val rightColumn = new BoxPanel(Orientation.Vertical) {
    contents += playgrUpload
    contents += configUpload
  }

  val configs = new FlowPanel() {
    border = BorderFactory.createTitledBorder("Congiguration")
    contents += leftColumn
    contents += rightColumn
  }

  val players = new PlayersConfigView

  val mainArea = new BoxPanel(Orientation.Vertical) {
    contents += configs
    contents += players
  }

  val okBtn = new Button("Create")
  val cancelBtn = new Button("Cancel")
  val buttons = new FlowPanel(FlowPanel.Alignment.Right)() {
    contents += okBtn
    contents += cancelBtn
  }

  contents = new BorderPanel {
    layout(mainArea) = BorderPanel.Position.Center
    layout(buttons) = BorderPanel.Position.South
  }
  
  listenTo(cancelBtn)
  listenTo(okBtn)
  reactions += {
    case ButtonClicked(`cancelBtn`) =>
      visible = false
      dispose()
    case ButtonClicked(`okBtn`) =>
      // TODO validate input
      submitted = true
      visible = false
      dispose()
    case KeyPressed(_, Key.Escape, _, _) =>
      visible = false
      dispose()
  }

  def show(): Option[NewGameConfig] = {
    centerOnScreen()
    visible = true
    if (submitted) {
      Some(NewGameConfig.createNew(playgrUpload.file, configUpload.file, players.getPlayersInfo()))
    } else {
      None
    }
  }

  class SelectFileLine extends BoxPanel(Orientation.Horizontal) {
    var file: File = _
    
    val path = new TextField() {
      columns = 30
    }
    val btn = new Button("Select File")

    contents += path
    contents += btn

    listenTo(btn)
    reactions += {
      case ButtonClicked(`btn`) =>
        val fileChooser = new FileChooser()
        //fileChooser.fileFilter = xmlFileFilter
        val res = fileChooser.showOpenDialog(this)
        if (res == FileChooser.Result.Approve) {
          file = fileChooser.selectedFile
          path.text = file.getAbsolutePath()
        }
    }
  }

  class PlayersConfigView extends ScrollPane {
    var playersList: List[NewPlayerView] = List()
    val addPlayerBtn = new Button("Add Player")
    val loadPlayersBtn = new Button("Load players")

    val playersView = new BoxPanel(Orientation.Vertical)

    def getPlayersInfo(): List[NewPlayerInfo] = {
      for (playerView <- playersList)
        yield new NewPlayerInfo(playerView.getName(), playerView.getJarFile(), playerView.getStrategyClass())
        
    }
    listenTo(addPlayerBtn)
    listenTo(loadPlayersBtn)
    contents = new BorderPanel {
      layout(playersView) = BorderPanel.Position.Center
      border = BorderFactory.createTitledBorder("Players")
    }
    update()
    reactions += {
      case ButtonClicked(`addPlayerBtn`) =>
        addEmptyPlayer()
    }

    def addEmptyPlayer(): Unit = {
      playersList ::= new NewPlayerView()
      update()
    }

    def update(): Unit = {
      playersView.contents.clear()
      playersView.contents += new FlowPanel(FlowPanel.Alignment.Right)() {
        contents += addPlayerBtn
        contents += loadPlayersBtn
      }

      for (player <- playersList) yield {
        playersView.contents += player
        playersView.peer.add(Box.createVerticalStrut(5))
      }
      pack()
    }
  }

  class NewPlayerView extends FlowPanel(FlowPanel.Alignment.Left)() {
    val deleteBtn = new Button("Delete")
    val nameField = new TextField("") {
            columns = 10
          }
    val jarFileLine = new SelectFileLine()
    val strategyClassField = new TextField("") {
          columns = 40
        }
    
    border = BorderFactory.createLineBorder(Color.black);
    contents += new BoxPanel(Orientation.Vertical) {
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += new FlowPanel(FlowPanel.Alignment.Left)() {
          contents += new Label("Name ")
          contents += nameField
          contents += deleteBtn
        }
      }
      peer.add(Box.createVerticalStrut(5))
      contents += new BoxPanel(Orientation.Horizontal) {
        peer.add(Box.createHorizontalStrut(30))
        contents += new Label("Strategy jar ")
        contents += jarFileLine
      }
      peer.add(Box.createVerticalStrut(5))
      contents += new BoxPanel(Orientation.Horizontal) {
        peer.add(Box.createHorizontalStrut(30))
        contents += new Label("Strategy classs ")
        contents += strategyClassField
      }
    }
    listenTo(deleteBtn)
    
    def getName(): String = nameField.text
    def getJarFile(): File = jarFileLine.file
    def getStrategyClass(): String = strategyClassField.text
  }
}
