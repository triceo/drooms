package org.drooms.gui.swing

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import scala.collection.JavaConversions.asScalaBuffer
import org.drooms.impl.util.PlayerAssembly
import org.drooms.impl.DefaultPlayground

/**
 * Holds the configuration needed for new Drooms game.
 */
case class GameConfig(
    val playgroundFile: File,
    val gameProperties: File,
    val players: java.util.List[org.drooms.api.Player]) {

  def getPlayersNames(): List[String] = {
    import scala.collection.JavaConversions._
    (for (p <- players) yield p.getName()).toList
  }
}

/**
 * Contains factory methods for creating new {@link GameConfig}s.
 */
object GameConfig {
  /**
   * Creates a new {@link GameConfig} using playground, game properties and players specified as files.
   * 
   * @param playgroundFile file with the playground definition
   * @param gamePropsFile file with game properties
   * @param playersFile file with players definition
   * 
   * @return new {@link GameConfing} based on the specified playground, game properties and players
   */
  def createNew(playgroundFile: File, gamePropsFile: File, playersFile: File): GameConfig = {
    val players = new PlayerAssembly(playersFile).assemblePlayers()
    createNew(playgroundFile, gamePropsFile, players)
  }
  
  /**
   * Creates a new {@link GameConfig} using playground file, game properties file and players specified as list of 
   * {@link PlayerInfo}.
   * 
   * @param playgroundFile file with the playground definition
   * @param gamePropsFile file with game properties
   * @param playersInfo list of {@link PlayerInfo} contains needed information about players
   * 
   * @return new {@link GameConfing} based on the specified playground, game properties and players
   */
  def createNew(playgroundFile: File, gamePropsFile: File, playersInfo: List[PlayerInfo]): GameConfig = {
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
    createNew(playgroundFile, gamePropsFile, playersFile)
  }

  /**
   * Creates a new {@link GameConfig} using {@link org.drooms.api.Playground}, 
   * {@link org.drooms.impl.util.properties.GameProperties} and list of {@link org.drooms.api.Player}.
   * 
   * @param playgroundFile file with the playground definition
   * @param gamePropsFile file with game properties
   * @param playersFile file with players definition
   * 
   * @return new {@link GameConfing} based on the specified playground, game properties and players
   */
  def createNew(playgroundFile: File, gamePropsFile: File, 
    players: java.util.List[org.drooms.api.Player]): GameConfig = {
    new GameConfig(playgroundFile, gamePropsFile, players)
  }
}

/**
 * Represents an information about player that is needed for the {@link DroomsGame}.
 * 
 * Player info contains:
 * <ul>
 *   <li>player's name
 *   <li>jar file with the strategy
 *   <li>FQN of the main strategy class
 */
case class PlayerInfo(val name: String, val jar: File, val strategyClass: String)
