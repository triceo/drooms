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

class PlayersList(var players: List[Player]) extends BorderPanel {

  var playersListView = new ListView(players) {
    renderer = new PlayersListRenderer
  }

  import BorderPanel.Position._
  layout(new Label("Players")) = North
  layout(playersListView) = Center

  def addPlayer(player: Player) {
    playersListView.listData = playersListView.listData ++ Seq(player)
  }
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

