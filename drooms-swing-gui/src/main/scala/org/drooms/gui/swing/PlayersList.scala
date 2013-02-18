package org.drooms.gui.swing

import java.awt.Color

import scala.collection.mutable.Buffer
import scala.swing.Alignment
import scala.swing.BorderPanel
import scala.swing.Component
import scala.swing.Label
import scala.swing.ListView
import scala.swing.ListView.Renderer

import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.NewGameReportChosen
import org.drooms.gui.swing.event.TurnStepPerformed
import org.drooms.gui.swing.event.UpdatePlayers

import javax.swing.BorderFactory


class PlayersList(val players: Buffer[Player], val colors: PlayerColors) {
  def this() = this(Buffer(), PlayerColors.getDefault())
  def this(colors: PlayerColors) = this(Buffer(), colors)
  
  def addPlayer(player: Player): Unit = players += player

  def addPlayer(name: String, color: Color): Unit = addPlayer(new Player(name, 0, color))
  
  def addPlayer(name: String): Unit = addPlayer(name, colors.getNext())

  def addPlayers(players: List[String]): Unit = {
    players.foreach(addPlayer(_))
  }
  
  def updatePoints(newScores: Map[String, Int]): Unit = {
    for (playerName <- newScores.keys) {
      players.find(_.name == playerName) match {
        case Some(p@Player(name, score, color)) => 
          p.score = newScores(name)
        case None => throw new IllegalStateException("Trying to update non-existing player '" + playerName + "'") 
      }
    }
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
  val eventBus = EventBusFactory.get()
  val playersList = PlayersList.get()
  val playersListView = new ListView(playersList.players) {
    renderer = new PlayersListRenderer
  }
  listenTo(eventBus)

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
    case UpdatePlayers() => update()
  }

  def update() {
    playersListView.listData = playersList.players.sortBy(_.score).reverse
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
