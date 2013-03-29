package org.drooms.gui.swing

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

import org.drooms.impl.DefaultPlayground
import org.drooms.impl.util.PlayerAssembly
import org.drooms.impl.util.properties.GameProperties

/**
 * Holds the configuration needed for new Drooms game.
 */
case class GameConfig(
    val playground: org.drooms.api.Playground,
    val gameProperties: org.drooms.impl.util.properties.GameProperties,
    val players: java.util.List[org.drooms.api.Player]) {

  def getPlaygroundInit(): Set[Node] = {
    // create the initial playground def
    (for (
      i <- 0 until playground.getWidth();
      j <- 0 until playground.getHeight();
      if (playground.isAvailable(i, j))
    ) yield Node(i, j)).toSet

  }

  def getPlaygroundWidth(): Int = playground.getWidth()

  def getPlaygroundHeight(): Int = playground.getHeight()
  
  def getPlayersNames(): List[String] = {
    import scala.collection.JavaConversions._
    (for (p <- players) yield p.getName()).toList
  }
}

object GameConfig {
  def createNew(playgroundFile: File, gameConfigFile: File, playersInfo: List[PlayerInfo]): GameConfig = {
    val playground = DefaultPlayground.read(playgroundFile.getName(), new FileInputStream(playgroundFile))
    val playersFile = File.createTempFile("droom-swing-gui", "players")
    playersFile.deleteOnExit()
    val output = new FileOutputStream(playersFile)
    val props = new Properties()
    for (player <- playersInfo) {
      props.setProperty(player.name, player.strategyClass + "@file://" + player.jar.getAbsolutePath())
    }
    props.store(output, "")
    output.flush()
    output.close()
    val players = new PlayerAssembly(playersFile).assemblePlayers()
    
    val gameConfig = GameProperties.read(gameConfigFile)
    createNew(playground, gameConfig, players)
  }

  def createNew(playground: org.drooms.api.Playground, gameConfig: org.drooms.impl.util.properties.GameProperties, 
    players: java.util.List[org.drooms.api.Player]): GameConfig = {
    new GameConfig(playground, gameConfig, players)
  }
}

case class PlayerInfo(val name: String, val jar: File, val strategyClass: String)
