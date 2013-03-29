package org.drooms.gui.swing

import java.awt.Color
import scala.swing.Alignment
import scala.swing.BorderPanel
import scala.swing.Component
import scala.swing.Label
import scala.swing.ListView
import scala.swing.ListView.Renderer
import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.GoToTurnState
import org.drooms.gui.swing.event.TurnStepPerformed
import javax.swing.BorderFactory
import org.drooms.gui.swing.event.NewUIComponentsRequested

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
class PlayersList(val players: List[Player], val colors: PlayerColors) {
  def this() = this(List(), PlayerColors.getDefault())
  def this(colors: PlayerColors) = this(List(), colors)

  /**
   * Returns {@link Player} with the specified name.
   *
   * @param playerName name of the player
   *
   * @return {@link Player} if he exists, {@link NoSuchElementException} otherwise
   */
  def getPlayer(playerName: String): Player = {
    players.find(_.name == playerName) match {
      case Some(player) => player
      case None => throw new NoSuchElementException("Player '" + playerName + " 'not found!")
    }
  }

  def addPlayer(player: Player): PlayersList = new PlayersList(player :: players, colors)

  def addPlayer(name: String, color: Color): PlayersList = addPlayer(new Player(name, 0, color))

  def addPlayer(name: String): PlayersList = addPlayer(name, colors.getNext())

  /**
   * Creates and adds players with specified names into the list.
   *
   * @param playersNames list of players names to be added
   *
   * @return new {@link PlayersList} that contains all original {@link Player}s and the newly created ones
   */
  def addPlayers(playersNames: List[String]): PlayersList = {
    if (playersNames.isEmpty)
      new PlayersList(players, colors)
    else
      addPlayers(playersNames.tail).addPlayer(playersNames.head)
  }

  /**
   * Updates score for all specified players and returns new {@link PlayersList} with new {@link Player} scores.
   *
   * @param newScores map that contains playerName -> score mapping
   *
   * @return new {@link PlayersList} with updated players
   */
  def updateScores(newScores: Map[String, Int]): PlayersList = {
    new PlayersList(
      newScores.keys.map(name =>
        players.find(_.name == name) match {
          case Some(p @ Player(name, score, color)) =>
            new Player(name, newScores(name), color)
          case None => throw new IllegalStateException("Trying to update non-existing player '" + name + "'")
        }).toList,
      colors)
  }

  /**
   * Add points to the specified {@link Player} and returns new {@link PlayersList} with the increased score for {@link Player}.
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
   * Sets a specified score for all {@link Player}s.
   *
   * @param points how many points to assign to each player
   *
   * @return new {@link PlayersList} where all {@link Player}s has specified number of points
   */
  def setScoreForAllPlayers(points: Int): PlayersList = {
    new PlayersList(players.map(p =>
      new Player(p.name, 0, p.color)).toList, colors)
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
