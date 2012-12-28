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
import org.drooms.gui.swing.event.NewGameLogChosen

object PlayersList {
  val players: Buffer[Player] = Buffer()

  def get(): List[Player] = players.toList

  def addPlayer(player: Player): Unit = players += player

  def addPlayer(name: String): Unit = players += new Player(name, PlayerColors.getNext())

  def addPlayers(players: List[String]): Unit = {
    players.foreach(addPlayer(_))
  }

  def getPlayer(playerName: String): Player = {
    players.find(_.name == playerName) match {
      case Some(player) => player
      case None => throw new RuntimeException("Player not found!" + playerName)
    }
  }

  def clear(): Unit = {
    players.clear()
    PlayerColors.reset()
  }
}

class PlayersListView extends BorderPanel {
  val eventPublisher = DroomsEventPublisher.get()
  val playersListView = new ListView(PlayersList.get()) {
    renderer = new PlayersListRenderer
  }
  listenTo(eventPublisher)

  layout(new Label("Players")) = BorderPanel.Position.North
  layout(playersListView) = BorderPanel.Position.Center

  reactions += {
    case NewGameLogChosen(gameLog, _) =>
      PlayersList.clear()
      PlayersList.addPlayers(gameLog.players)
      update()
  }

  def update() {
    playersListView.listData = PlayersList.get()
  }

  class PlayersListRenderer extends Renderer {
    override def componentFor(list: ListView[_], isSelected: Boolean, focused: Boolean, a: Any, index: Int): Component = {
      val player = a.asInstanceOf[Player]
      new Label(player.name) {
        horizontalAlignment = Alignment.Left
        opaque = true
        background = player.color
        if (isSelected) {
          border = BorderFactory.createLineBorder(Color.BLACK)
        }
      }
    }
  }
}

