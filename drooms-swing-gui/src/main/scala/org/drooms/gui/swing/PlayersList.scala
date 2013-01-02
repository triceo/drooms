package org.drooms.gui.swing

import scala.swing.UIElement
import scala.swing.ListView
import scala.swing.Component
import scala.swing.BorderPanel
import scala.swing.Label
import java.awt.Color
import scala.swing.ListView.Renderer
import scala.swing.Alignment
import javax.swing.BorderFactory
import scala.collection.mutable.Buffer
import org.drooms.gui.swing.event.DroomsEventPublisher
import org.drooms.gui.swing.event.NewGameReportChosen
import scala.swing.BoxPanel
import scala.swing.Orientation
import java.awt.BorderLayout
import org.drooms.gui.swing.event.TurnStepPerformed

class PlayersList(val players: Buffer[Player], val colors: PlayerColors) {
  def this() = this(Buffer(), PlayerColors.getDefault())
  def this(colors: PlayerColors) = this(Buffer(), colors)
  
  def addPlayer(player: Player): Unit = players += player

  def addPlayer(name: String, color: Color): Unit = addPlayer(new Player(name, 0, color))
  
  def addPlayer(name: String): Unit = addPlayer(name, colors.getNext())

  def addPlayers(players: List[String]): Unit = {
    players.foreach(addPlayer(_))
  }

  def getPlayer(playerName: String): Player = {
    players.find(_.name == playerName) match {
      case Some(player) => player
      case None => throw new RuntimeException("Player '" + playerName + "'not found!")
    }
  }

  def clear(): Unit = {
    players.clear()
    colors.reset()
  }
}

object PlayersList {
  val playersList = new PlayersList()
  
  def get() = playersList
}

class PlayersListView extends BorderPanel {
  val eventPublisher = DroomsEventPublisher.get()
  val playersList = PlayersList.get()
  val playersListView = new ListView(playersList.players) {
    renderer = new PlayersListRenderer
  }
  listenTo(eventPublisher)

  layout(new Label("Players")) = BorderPanel.Position.North
  layout(playersListView) = BorderPanel.Position.Center

  reactions += {
    case NewGameReportChosen(gameReport, _) =>
      playersList.clear()
      playersList.addPlayers(gameReport.players)
      update()
    case TurnStepPerformed(step) =>
      step match {
        case WormSurvived(ownerName, points) =>
          playersList.getPlayer(ownerName).addPoints(points)
          update()
        case CollectibleCollected(playerName, collectible) =>
          playersList.getPlayer(playerName).addPoints(collectible.points)
          update()
        case _ =>
      }
  }

  def update() {
    playersListView.listData = playersList.players
  }

  class PlayersListRenderer extends Renderer {
    override def componentFor(list: ListView[_], isSelected: Boolean, focused: Boolean, a: Any, index: Int): Component = {
      val player = a.asInstanceOf[Player]
      new BorderPanel() {
        opaque = true
        background = player.color
        border = BorderFactory.createRaisedBevelBorder()
        if (isSelected) {
          border = BorderFactory.createLoweredBevelBorder()
        }
        layout(new Label(player.name) {
          horizontalAlignment = Alignment.Left
          opaque = true
          background = player.color
        }) = BorderPanel.Position.West
        layout(new Label(player.score + " points")) = BorderPanel.Position.East
      }
    }
  }
}

