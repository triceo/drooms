package org.drooms.gui.swing

import scala.swing.Publisher
import scala.swing.event.Event
import org.drooms.gui.swing.event.DroomsEventPublisher

/**
 * Represents underlying model for Playground as array of arrays of Positions.
 */
case class PlaygroundModel(val width: Int, val height: Int, var positions: Array[Array[Position]], var eventPublisher: Publisher) {
  var worms: collection.mutable.Set[Worm] = collection.mutable.Set()
  def this(width: Int, height: Int, publisher: Publisher) = {
    this(width, height, Array.ofDim[Position](width, height), publisher)
    // initialize the playground
    for (i <- 0 until width; j <- 0 until height) {
      positions(i)(j) = Wall(Node(i, j))
    }
  }

  def this(positions: Array[Array[Position]]) = {
    this(positions.size, positions(0).size, positions, DroomsEventPublisher.get())
  }

  def this(positions: Array[Array[Position]], publisher: Publisher) = {
    this(positions.size, positions(0).size, positions, publisher)
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

  override def clone(): PlaygroundModel = {
    val newPoss = Array.ofDim[Position](width, height)
    for (i <- 0 until width; j <- 0 until height) {
      val pos = positions(i)(j) match {
        case Empty(node) => Empty(node)
        case Wall(node) =>  Wall(node)
        case WormPiece(node, t, p) => WormPiece(node, t, p)
        case Collectible(node, exp, points) => Collectible(node, exp, points)
      }
      newPoss(i)(j) = pos
    }
    val newModel = new PlaygroundModel(newPoss)
    
    newModel.worms = worms.clone()
    newModel
  }

  /** Initialize worms from specified list of pairs 'ownerName' -> 'list of Nodes' */
  def initWorms(wormsInit: Set[(String, List[Node])]): Unit = {
    worms.clear()
    // TODO create method constructWorm(nodes)
    for ((name, nodes) <- wormsInit) {
      worms.add(Worm(name, (for (node <- nodes) yield WormPiece(node, "Head", name)).toList))
    }
    // update model
    for (worm <- worms) updatePositions(worm.pieces)
  }

  /** Moves the worm to the new position */
  def moveWorm(ownerName: String, nodes: List[Node]): Unit = {
    this.synchronized {
      // removes current worm pieces
      removeWormPieces(ownerName)
      // worm must have at least head
      val head = nodes.head
      updateWormIfLegal(head, ownerName, "Head")

      if (nodes.size > 2) {
        for (node <- nodes.tail.init) {
          updateWormIfLegal(node, ownerName, "Body")
        }
      }

      if (nodes.size > 1) {
        val tail = nodes.last
        updateWormIfLegal(tail, ownerName, "Tail")
      }

      /**
       * Updates the worm only if the underlaying node is empty or is collectible -> eligible to be occupied by current worm
       */
      def updateWormIfLegal(node: Node, ownerName: String, wormType: String): Unit = {
        // we can only update Empty nodes and Collectibles, if the worm crashed into wall or other worm, piece must not be updated!
        if (node.x >= width || node.y >= height) return
        positions(node.x)(node.y) match {
          case Empty(node) =>
            updateWorm(ownerName, new WormPiece(node, wormType, ownerName))
          case Collectible(node, _, _) =>
            updateWorm(ownerName, new WormPiece(node, wormType, ownerName))
          case _ =>
        }
      }
    }
  }

  def updateWorm(ownerName: String, piece: WormPiece) = {
    getWorm(ownerName).addPiece(piece)
    updatePosition(piece)
  }

  def getWorm(ownerName: String): Worm = {
    worms.find(_.ownerName == ownerName) match {
      case Some(worm) => worm
      case None => throw new RuntimeException("Can't get non existing worm! Owner=" + ownerName)
    }
  }

  /**
   * Removes the worm from the list of worms and also makes sure that all worm pieces are removed from playground
   */
  def removeWorm(ownerName: String): Unit = {
    val worm = getWorm(ownerName)
    removeWormPieces(ownerName)
    worms.remove(worm)
  }

  def removeWormPieces(ownerName: String): Unit = {
    worms.find(_.ownerName == ownerName) match {
      case Some(worm) =>
        for (piece <- worm.pieces) {
          positions(piece.node.x)(piece.node.y) match {
            case WormPiece(node, t, owner) =>
              if (piece.playerName == owner) {
                updatePosition(Empty(node))
              }
            case _ =>
          }
          worm.pieces = List()
        }
      case None =>
    }
  }

  def update(step: TurnStep): Unit = {
    step match {
      case WormMoved(ownerName, nodes) =>
        moveWorm(ownerName, nodes)
      case WormCrashed(ownerName) =>
        removeWorm(ownerName)
      case WormDeactivated(ownerName) =>
        removeWorm(ownerName)
      case CollectibleAdded(collectible) =>
        updatePosition(collectible)
      case CollectibleRemoved(collectible) =>
        updatePosition(Empty(collectible.node))
      case CollectibleCollected(player, collectible) =>
      case _ => new RuntimeException("Unrecognized TurnStep: " + step)
    }
  }

  def useModel(model: PlaygroundModel): Unit = {
    positions = model.positions
  }

  def getPosition(x: Int, y: Int): Position = {
    positions(x)(y)
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
