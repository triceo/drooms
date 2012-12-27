package org.drooms.gui.swing

class GameTurn(val number: Int, val steps: List[TurnStep]) {
  override def toString = "Turn[" + number + ", steps[" + steps.toString + "]"
}

trait TurnStep

case class WormMoved(val worm: Worm) extends TurnStep
case class WormDied(val worm: Worm) extends TurnStep
case class CollectibeAdded(val collectibe: Collectible) extends TurnStep
case class CollectibleRemoved(val collectible: Collectible) extends TurnStep
case class WormRewarded(val worm: Worm, val collectible: Collectible) extends TurnStep
