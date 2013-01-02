package org.drooms.gui.swing

class GameTurn(val number: Int, val steps: List[TurnStep]) {
  override def toString = "Turn[" + number + ", steps[" + steps.toString + "]"
}

trait TurnStep

case class WormMoved(val owner: String, val nodes: List[Node]) extends TurnStep
case class WormCrashed(val owner: String) extends TurnStep
case class WormDeactivated(val owner: String) extends TurnStep
case class WormSurvived(val owner: String, val points: Int) extends TurnStep
case class CollectibleCollected(val player: String, val collectible: Collectible) extends TurnStep
case class CollectibleAdded(val collectibe: Collectible) extends TurnStep
case class CollectibleRemoved(val collectible: Collectible) extends TurnStep
