package org.drooms.gui.swing

import java.io.File

/**
 * Holds the configuration needed for new Drooms game.
 */
case class GameConfig(
  val playgroundFile: File,
  val gameProperties: File,
  val players: List[PlayerInfo]) {

  def getPlayersNames(): List[String] = {
    (for (p <- players) yield p.name).toList
  }
}

/**
 * Contains factory methods for creating new {@link GameConfig}s.
 */
object GameConfig {
  /**
   * Creates a new {@link GameConfig} using {@link org.drooms.api.Playground},
   * {@link org.drooms.impl.util.properties.GameProperties} and list of {@link PlayerInfo}s.
   *
   * @param playgroundFile file with the playground definition
   * @param gamePropsFile file with game properties
   * @param players info about players like name, strategy jar/dir location and strategy class
   *
   * @return new { @link GameConfig} based on the specified playground, game properties and players
   */
  def createNew(playgroundFile: File, gamePropsFile: File, playersInfo: List[PlayerInfo]): GameConfig = {
    new GameConfig(playgroundFile, gamePropsFile, playersInfo)
  }

}

/**
 * Represents an information about player that is needed for the {@link DroomsGame}.
 *
 * Player info contains:
 * <ul>
 *   <li>player's name
 * <li>strategy's Maven Group ID
 * <li>strategy's Maven Artifact ID
 * <li>strategy's version
 */
case class PlayerInfo(val name: String, val strategyGroupId: String, val strategyArtifactId: String, val
strategyVersion: String)
