package org.drooms.gui.swing

import java.awt.{Color, Dimension}
import java.io.File
import javax.swing.{BorderFactory, Box, JOptionPane}

import org.drooms.api.Player
import org.drooms.impl.util.PlayerProperties

import scala.swing.event.{ButtonClicked, Key, KeyPressed}
import scala.swing.{Alignment, BorderPanel, BoxPanel, Button, Dialog, FileChooser, FlowPanel, Label, Orientation, ScrollPane, TextField}

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
  /* Indicated if the dialog was submitted or not */
  private var submitted = false

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
    val addPlayerBtn = new Button("Add Player")
    val loadPlayersBtn = new Button("Load players")
    val playersView = new BoxPanel(Orientation.Vertical)
    private var playersList: List[PlayerView] = List()

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
        yield (
          new PlayerInfo(playerView.getName(), playerView.getStrategyGroupId(), playerView.getStrategyArtifactId(),
            playerView.getStrategyVersionId())
          )
    }

    /**
     * Shows an open dialog, lets user select a file with players configs and shows the parsed players in UI.
     */
    def addPlayersFromFile(): Unit = {
      import scala.collection.JavaConversions._
      val fileChooser = new FileChooser(NewGameSettings.lastOpenedDir)
      val dialogRes = fileChooser.showOpenDialog(this)
      if (dialogRes == FileChooser.Result.Approve) {
        val assembly = new PlayerProperties(fileChooser.selectedFile)
        for (player <- assembly.read()) {
          addPlayerView(player)
        }
        update()
      }
    }

    def addPlayerView(player: Player) = {
      val playerView = new PlayerView(this)
      playerView.nameField.text = player.getName
      playerView.strategyArtifactIdField.text = player.getStrategyReleaseId.getArtifactId
      playerView.strategyGroupIdField.text = player.getStrategyReleaseId.getGroupId
      playerView.strategyVersionField.text = player.getStrategyReleaseId.getVersion
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
    val strategyGroupIdField = new TextField("com.github.triceo.drooms") {
      columns = 40
    }
    val strategyArtifactIdField = new TextField("drooms-strategy-random") {
      columns = 40
    }
    val strategyVersionField = new TextField("2.0-SNAPSHOT") {
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
        contents += new Label("Strategy Group ID ")
        contents += strategyGroupIdField
      }
      peer.add(Box.createVerticalStrut(5))
      contents += new BoxPanel(Orientation.Horizontal) {
        peer.add(Box.createHorizontalStrut(30))
        contents += new Label("Strategy Artifact ID  ")
        contents += strategyArtifactIdField
      }
      peer.add(Box.createVerticalStrut(5))
      contents += new BoxPanel(Orientation.Horizontal) {
        peer.add(Box.createHorizontalStrut(30))
        contents += new Label("Strategy Version  ")
        contents += strategyVersionField
      }
    }
    listenTo(deleteBtn)

    reactions += {
      case ButtonClicked(`deleteBtn`) =>
        parent.removePlayerView(this)
    }

    def getName(): String = nameField.text

    def getStrategyArtifactId(): String = strategyArtifactIdField.text

    def getStrategyGroupId(): String = strategyGroupIdField.text

    def getStrategyVersionId(): String = strategyVersionField.text

    def validateInput(): Boolean = {
      val res = !nameField.text.isEmpty && !strategyArtifactIdField.text.isEmpty() && !strategyGroupIdField.text
        .isEmpty() && !strategyVersionField.text.isEmpty()
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
