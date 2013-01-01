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

object PlayersList {
  val players: Buffer[Player] = Buffer()

  def get(): List[Player] = players.toList

  def addPlayer(player: Player): Unit = players += player

  def addPlayer(name: String): Unit = players += new Player(name, 0, PlayerColors.getNext())

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
    case NewGameReportChosen(gameReport, _) =>
      PlayersList.clear()
      PlayersList.addPlayers(gameReport.players)
      update()
    case TurnStepPerformed(step) =>
      step match {
        case WormSurvived(ownerName, points) =>
          PlayersList.getPlayer(ownerName).addPoints(points)
          update()
        case CollectibleCollected(playerName, collectible) =>
          PlayersList.getPlayer(playerName).addPoints(collectible.points)
          update()
        case _ =>
      }
  }

  def update() {
    playersListView.listData = PlayersList.get()
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
        layout(new Label(player.currentScore + " points")) = BorderPanel.Position.East
      }
    }
  }
}

