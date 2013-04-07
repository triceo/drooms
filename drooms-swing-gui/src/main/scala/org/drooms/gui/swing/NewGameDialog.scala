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
import scala.io.Source
import java.io.FileInputStream
import javax.swing.JOptionPane

/**
 * Dialog used to specify configuration needed for new Drooms game.
 *
 * It allows users to specify:
 * <ul>
 *   <li>game configuration file (*.cfg)
 *   <li>playground definition file (*.playground)
 *   <li>list of players (name, strategy location and strategy class) that will be part of the game
 */
class NewGameDialog extends Dialog {
  modal = true
  minimumSize = new Dimension(700, 300)
  preferredSize = new Dimension(700, 600)
  title = "New Drooms Game"

  /* Indicated if the dialog was submitted or not */
  private var submitted = false

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
    contents += configUpload
    contents += playgrUpload
  }

  val configs = new FlowPanel() {
    border = BorderFactory.createTitledBorder("Configuration")
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
      if (validateInput()) {
        submitted = true
        visible = false
        dispose()
      } else {
          JOptionPane.showMessageDialog(this.peer, "One or more fields are empty! Every text field has to be set.", 
              "Input validation error", JOptionPane.ERROR_MESSAGE)
      }
    case KeyPressed(_, Key.Escape, _, _) =>
      visible = false
      dispose()
  }

  def show(): Option[GameConfig] = {
    centerOnScreen()
    visible = true
    if (submitted) {
      Some(GameConfig.createNew(playgrUpload.file, configUpload.file, players.getPlayersInfo()))
    } else {
      None
    }
  }

  def validateInput(): Boolean = {
    !playgrUpload.path.text.isEmpty() && !configUpload.path.text.isEmpty() && players.validateInput()
  }

  /**
   * Represents edit line (file path) and button that can be used to load file/dir name.
   */
  class SelectFileLine extends BoxPanel(Orientation.Horizontal) {
    val path = new TextField() {
      columns = 30
    }
    val btn = new Button("Select File")

    contents += path
    contents += btn

    listenTo(btn)
    reactions += {
      case ButtonClicked(`btn`) =>
        val fileChooser = new FileChooser(NewGameSettings.lastOpenedDir)
        fileChooser.fileSelectionMode = FileChooser.SelectionMode.FilesAndDirectories
        val res = fileChooser.showOpenDialog(this)
        if (res == FileChooser.Result.Approve) {
          path.text = fileChooser.selectedFile.getAbsolutePath()
          NewGameSettings.lastOpenedDir = fileChooser.selectedFile.getParentFile()
        }
    }

    def file: File = {
      new File(path.text)
    }
  }

  class PlayersConfigView extends ScrollPane {
    private var playersList: List[PlayerView] = List()
    val addPlayerBtn = new Button("Add Player")
    val loadPlayersBtn = new Button("Load players")
    val playersView = new BoxPanel(Orientation.Vertical)

    listenTo(addPlayerBtn)
    listenTo(loadPlayersBtn)
    contents = new BorderPanel {
      layout(playersView) = BorderPanel.Position.Center
      border = BorderFactory.createTitledBorder("Players")
    }

    reactions += {
      case ButtonClicked(`addPlayerBtn`) =>
        addEmptyPlayerView()

      case ButtonClicked(`loadPlayersBtn`) =>
        addPlayersFromFile()
    }
    update()

    def getPlayersInfo(): List[PlayerInfo] = {
      for (playerView <- playersList)
        yield (if (playerView.getJarOrDir().isDirectory()) {
        new PlayerInfo(playerView.getName(), None, Some(playerView.getJarOrDir()), playerView.getStrategyClass())
      } else {
        new PlayerInfo(playerView.getName(), Some(playerView.getJarOrDir()), None, playerView.getStrategyClass())
      })
    }

    /**
     * Shows an open dialog, lets user select a file with players configs and shows the parsed players in UI.
     */
    def addPlayersFromFile(): Unit = {
      import scala.collection.JavaConversions._
      val fileChooser = new FileChooser(NewGameSettings.lastOpenedDir)
      val dialogRes = fileChooser.showOpenDialog(this)
      if (dialogRes == FileChooser.Result.Approve) {
        NewGameSettings.lastOpenedDir = fileChooser.selectedFile.getParentFile()
        val props = new java.util.Properties()
        props.load(new FileInputStream(fileChooser.selectedFile))
        for (playerName <- props.keySet()) {
          val value = props.getProperty(playerName.asInstanceOf[String])
          val strs = value.split("@")
          if (strs.size != 2) {
            throw new RuntimeException("Can't parse following player definition: " + value)
          }
          // cut off the 'file://' prefix if present
          val filePath =
            if (strs(1).startsWith("file://"))
              strs(1).substring(7)
            else
              strs(1)
          addPlayerView(playerName.asInstanceOf[String], strs(0), filePath)
        }
        update()
      }
    }

    def addPlayerView(name: String, clazz: String, path: String) = {
      val playerView = new PlayerView(this)
      playerView.nameField.text = name
      playerView.strategyClassField.text = clazz
      playerView.jarDirFileLine.path.text = path
      playersList ::= playerView
    }

    def addEmptyPlayerView(): Unit = {
      playersList ::= new PlayerView(this)
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

    def removePlayerView(playerView: PlayerView): Unit = {
      playersList = playersList.filter(_ != playerView)
      update()
    }
    
    def validateInput(): Boolean = {
      !playersList.exists(_.validateInput() == false)
    }
  }

  /**
   * Represents an UI component that holds  information about a player.
   *
   * Player's name, strategy jar/dir location and strategy class are stored.
   */
  class PlayerView(val parent: PlayersConfigView) extends FlowPanel(FlowPanel.Alignment.Left)() {
    val deleteBtn = new Button("Delete")
    val nameField = new TextField("") {
      columns = 10
    }
    val jarDirFileLine = new SelectFileLine()
    val strategyClassField = new TextField("") {
      columns = 40
    }

    border = BorderFactory.createLineBorder(Color.black)
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
        contents += new Label("Strategy jar/dir ")
        contents += jarDirFileLine
      }
      peer.add(Box.createVerticalStrut(5))
      contents += new BoxPanel(Orientation.Horizontal) {
        peer.add(Box.createHorizontalStrut(30))
        contents += new Label("Strategy class  ")
        contents += strategyClassField
      }
    }
    listenTo(deleteBtn)

    reactions += {
      case ButtonClicked(`deleteBtn`) =>
        parent.removePlayerView(this)
    }

    def getName(): String = nameField.text
    def getJarOrDir(): File = jarDirFileLine.file
    def getStrategyClass(): String = strategyClassField.text
    
    def validateInput(): Boolean = {
      val res = !nameField.text.isEmpty && !jarDirFileLine.path.text.isEmpty() && !strategyClassField.text.isEmpty()
      println(res)
      res
    }
  }
}

/**
 * Global settings shared between various UI components in the {@link NewGameDialog}.
 */
object NewGameSettings {
  var lastOpenedDir: File = new File(System.getProperty("user.dir"))
}
