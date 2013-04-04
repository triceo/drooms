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
  val players: List[PlayerInfo]) {

  def getPlayersNames(): List[String] = {
    import scala.collection.JavaConversions._
    (for (p <- players) yield p.name).toList
  }
}

/**
 * Contains factory methods for creating new {@link GameConfig}s.
 */
object GameConfig {
  /**
   * Creates a new {@link GameConfig} using {@link org.drooms.api.Playground},
   * {@link org.drooms.impl.util.properties.GameProperties} and list of {@link org.drooms.api.Player}.
   *
   * @param playgroundFile file with the playground definition
   * @param gamePropsFile file with game properties
   * @param players info about players like name, strategy jar/dir location and strategy class
   *
   * @return new {@link GameConfing} based on the specified playground, game properties and players
   */
  def createNew(playgroundFile: File, gamePropsFile: File, playersInfo: List[PlayerInfo]): GameConfig = {
    val updatedPlayersInfo = createJarsForStrategyDirs(playersInfo)
    new GameConfig(playgroundFile, gamePropsFile, updatedPlayersInfo)
  }

  private def createJarsForStrategyDirs(playersInfo: List[PlayerInfo]): List[PlayerInfo] = {
    if (playersInfo.isEmpty) {
      List()
    } else {
      val playerInfo = playersInfo.head
      val updatedPlayerInfo = playerInfo.strategyDir match {
        case Some(dir) =>
          val file = File.createTempFile("drooms-strategy-" + playerInfo.name, ".jar")
          file.deleteOnExit()
          new PlayerInfo(playerInfo.name, Some(file), Some(dir), playerInfo.strategyClass)
        case None =>
          // just use the current player info, because the jar should be already set
          if (playerInfo.jar == None) {
            throw new IllegalStateException("Player need to have strategy jar or strategy dir set!")
          }
          playerInfo
      }
      updatedPlayerInfo :: createJarsForStrategyDirs(playersInfo.tail)
    }
  }
}

/**
 * Represents an information about player that is needed for the {@link DroomsGame}.
 *
 * Player info contains:
 * <ul>
 *   <li>player's name
 *   <li>[optional] jar file with the strategy
 *   <li>[optional] directory with stragegy classes and resources
 *   <li>FQN of the main strategy class
 */
case class PlayerInfo(val name: String, val jar: Option[File], val strategyDir: Option[File], val strategyClass: String)
