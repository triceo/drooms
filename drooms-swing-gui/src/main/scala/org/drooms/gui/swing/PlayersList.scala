package org.drooms.gui.swing

import java.awt.Color
import javax.swing.BorderFactory

import scala.swing.ListView.Renderer
import scala.swing.{Alignment, BorderPanel, Component, Label, ListView}

/**
 * Utility class used for creating {@link PlayersList}s.
 */
object PlayersListFactory {
  def createPlayersList(players: List[String]): PlayersList = {
    (new PlayersList).addPlayers(players)
  }
}

/**
 * List of {@link Player}s for current replay or real-time game
 *
 * List is immutable. All operations that are altering the list are returning new instance with those
 * changes applied.
 */
class PlayersList(val players: List[GamePlayer], val colors: PlayerColors) {
  def this() = this(List(), PlayerColors.getDefault())
  def this(colors: PlayerColors) = this(List(), colors)

  def addPlayer(player: GamePlayer): PlayersList = new PlayersList(player :: players, colors)

  def addPlayer(name: String, color: Color): PlayersList = addPlayer(new GamePlayer(name, 0, color))

  def addPlayer(name: String): PlayersList = addPlayer(name, colors.getNext())

  /**
   * Creates and adds players with specified names into the list.
   *
   * @param playersNames list of players names to be added
   *
   * @return new { @link PlayersList} that contains all original { @link GamePlayer}s and the newly created ones
   */
  def addPlayers(playersNames: List[String]): PlayersList = {
    if (playersNames.isEmpty)
      new PlayersList(players, colors)
    else
      addPlayers(playersNames.tail).addPlayer(playersNames.head)
  }

  /**
   * Updates score for all specified players and returns new {@link PlayersList} with new {@link GamePlayer} scores.
   *
   * @param newScores map that contains playerName -> score mapping
   *
   * @return new {@link PlayersList} with updated players
   */
  def updateScores(newScores: Map[String, Int]): PlayersList = {
    new PlayersList(
      newScores.keys.map(name =>
        players.find(_.name == name) match {
          case Some(p@GamePlayer(name, score, color)) =>
            new GamePlayer(name, newScores(name), color)
          case None => throw new IllegalStateException("Trying to update non-existing player '" + name + "'")
        }).toList,
      colors)
  }

  /**
   * Add points to the specified {@link GamePlayer} and returns new {@link PlayersList} with the increased score for
   * {@link GamePlayer}.
   *
   * @param playerName name of the player to which add the points
   * @param points how many point to add
   *
   * @return new {@link PlayersList} with the specified player's score increased
   */
  def addPoints(playerName: String, points: Int): PlayersList = {
    new PlayersList(getPlayer(playerName).addPoints(points) :: players.filter(_.name != playerName), colors)
  }

  /**
   * Returns {@link GamePlayer} with the specified name.
   *
   * @param playerName name of the player
   *
   * @return { @link Player} if he exists, { @link NoSuchElementException} otherwise
   */
  def getPlayer(playerName: String): GamePlayer = {
    players.find(_.name == playerName) match {
      case Some(player) => player
      case None => throw new NoSuchElementException("Player '" + playerName + " 'not found!")
    }
  }

  /**
   * Sets a specified score for all {@link Player}s.
   *
   * @param points how many points to assign to each player
   *
   * @return new {@link PlayersList} where all {@link Player}s has specified number of points
   */
  def setScoreForAllPlayers(points: Int): PlayersList = {
    new PlayersList(players.map(p =>
      new GamePlayer(p.name, 0, p.color)).toList, colors)
  }

  override def toString(): String = players.toString()
}

class PlayersListView(var playersList: PlayersList) extends BorderPanel {
  val eventBus = EventBusFactory.get()
  val playersListView = new ListView(playersList.players) {
    renderer = new PlayersListRenderer
  }

  listenTo(eventBus)

  reactions += {
    case TurnStepPerformed(step) =>
      step match {
        case WormSurvived(ownerName, points) =>
          playersList = playersList.addPoints(ownerName, points)
          update()

        case CollectibleCollected(playerName, collectible) =>
          playersList = playersList.addPoints(playerName, collectible.points)
          update()

        case _ =>
      }

    case GoToTurnState(_, state) =>
      playersList = playersList.updateScores(state.playersScore)
      update()
      
    case NewUIComponentsRequested =>
      eventBus.deafTo(this)
      deafTo(eventBus)
  }

  createUIContents()

  def createUIContents(): Unit = {
    layout(new Label("Players")) = BorderPanel.Position.North
    layout(playersListView) = BorderPanel.Position.Center
  }

  def update() {
    playersListView.listData = playersList.players.sortBy(_.score).reverse
  }

  class PlayersListRenderer extends Renderer {
    override def componentFor(list: ListView[_], isSelected: Boolean, focused: Boolean, a: Any, index: Int): Component = {
      val player = a.asInstanceOf[GamePlayer]
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
