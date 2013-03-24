package org.drooms.gui.swing

/**
 * Represents one turn as defined by the game.
 *
 * Turn consists of one or more {@link TurnStep}s.
 *
 * @see TurnStep
 */
class GameTurn(val number: Int, val steps: List[TurnStep]) {
  override def toString = "Turn[" + number + ", steps[" + steps.toString + "]"
}

/**
 * Represents part of the {@link Turn}.
 *
 * There are several turn steps that can occur as part of the whole {@link Turn}.
 * Each of the following steps can occur multiple times inside one {@link Turn}:
 * <ul>
 *   <li>{@link WormMoved}
 *   <li>{@link WormCrashed}
 *   <li>{@link WormDeactivated}
 *   <li>{@link WormSurvived}
 *   <li>{@link CollectibleCollected}
 *   <li>{@link CollectibleAdded}
 *   <li>{@link CollectibleRemoved}
 *
 * See classes in org.drooms.api.impl.logic.events package for more info the actions that
 * can be part of the turn.
 */
trait TurnStep

case class WormMoved(val owner: String, val nodes: List[Node]) extends TurnStep
case class WormCrashed(val owner: String) extends TurnStep
case class WormDeactivated(val owner: String) extends TurnStep
case class WormSurvived(val owner: String, val points: Int) extends TurnStep
case class CollectibleCollected(val player: String, val collectible: Collectible) extends TurnStep
case class CollectibleAdded(val collectibe: Collectible) extends TurnStep
case class CollectibleRemoved(val collectible: Collectible) extends TurnStep

/**
 * Contains all the data needed to go to certain state.
 *
 * State is defined by the playground model and and players data (mainly the points for each player).
 * Turn state for turn 0 is the playground and players state _after_ the turn 0 was performed (e.g. before turn 1 is initiated)
 */
case class TurnState(val playgroundModel: PlaygroundModel, val players: Map[String, Int])
