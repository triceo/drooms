package org.drooms.gui.swing

import scala.swing.Publisher
import scala.swing.event.Event
import org.drooms.gui.swing.event.DroomsEventPublisher

/**
 * Represents underlying model for Playground as array of arrays of Positions.
 */
class PlaygroundModel(val width: Int, val height: Int) {
  val eventPublisher = DroomsEventPublisher.get()
  val positions = Array.ofDim[Position](width, height)

  // initialize the playground
  for (i <- 0 until width; j <- 0 until height) {
    positions(i)(j) = Wall(Node(i, j))
  }

  def updatePosition(pos: Position): Unit = {
    positions(pos.node.x)(pos.node.y) = pos
    eventPublisher.publish(new PositionChanged(pos))
  }

  def updatePositions(positions: Seq[Position]): Unit = {
    for (pos <- positions) {
      updatePosition(pos)
    }
  }

  def emptyNodes(nodesToEmpty: Iterable[Node]) = {
    nodesToEmpty.foreach(node => updatePosition(Empty(node)))
  }
}

/** Event that indicates that value on specified position has changed */
case class PositionChanged(val pos: Position) extends Event

/**
 * Represents x and y coordinates for certain position on playground
 */
case class Node(val x: Int, val y: Int)

/**
 * Represents one position on playground
 */
trait Position {
  def node: Node
}

case class Empty(val node: Node) extends Position
case class WormPiece(val node: Node, val wormType: String, val playerName: String) extends Position
case class Wall(val node: Node) extends Position
case class Collectible(val node: Node, val expiresInTurn: Int, val points: Int) extends Position

case class Worm(val ownerName: String, var pieces: List[WormPiece]) {
  def addPiece(piece: WormPiece) = {
    pieces ::= piece
  }
}
