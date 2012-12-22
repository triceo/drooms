package org.drooms.gui.swing

import scala.swing.UIElement
import scala.swing.ListView
import scala.swing.Component
import scala.swing.BorderPanel
import scala.swing.Label
import java.awt.Color

class PlayersList(var players: List[String]) extends BorderPanel {

  var playersListView = new ListView(players) {
    // TODO create custom renderer so that the player name has colored background
    //renderer = 
  }

  import BorderPanel.Position._
  layout(new Label("Players")) = North
  layout(playersListView) = Center

  def addPlayer(name: String) {
    playersListView.listData = playersListView.listData ++ Seq(name)
  }
}

