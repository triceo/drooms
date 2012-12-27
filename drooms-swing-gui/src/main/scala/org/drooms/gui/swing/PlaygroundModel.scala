package org.drooms.gui.swing

import scala.swing.Publisher
import scala.swing.event.Event

class PlaygroundModel(val height: Int, val width: Int) extends Publisher {
  val nodes = new Array[Array[Node]](height, width)
  var worms: Set[Worm] = Set()

  for (i <- 0 until height; j <- 0 until width) {
    nodes(i)(j) = new Wall(i, j)
  }

  def updateNode(node: Node): Unit = {
    nodes(node.row)(node.col) = node
    publish(new NodeChanged(node))
  }

  def updateNodes(nodes: Seq[Node]): Unit = {
    for (node <- nodes) {
      updateNode(node)
    }
  }

  /** Moves the worm to the new position */
  def moveWorm(worm: Worm): Unit = {
    // removes old worm
    removeWorm(worm.owner.name)
    worms += worm
    println("moving worm")
    updateNodes(worm.pieces.toList)
  }

  def removeWorm(ownerName: String): Unit = {
    for (worm <- worms) {
      if (worm.owner.name == ownerName) {
        worms -= worm
        emptyNodes(worm.pieces)
      }
    }
  }

  def emptyNodes(nodesToEmpty: Iterable[Node]) = {
    nodesToEmpty.foreach(node => updateNode(new Empty(node.row, node.col)))
  }

  /** Go through all nodes and save player worms on the way*/
  def gatherWorms(): Unit = {
    for (i <- 0 until height; j <- 0 until width) {
      nodes(i)(j) match {
        case wp @ WormPiece(_, _, _, _) =>
          updateWorm(wp)
        case _ =>
      }
    }
  }

  def updateWorm(wormPiece: WormPiece): Unit = {
    if (worms.exists(_.owner.name == wormPiece.player.name)) {
      val worm = getWorm(wormPiece.player.name)
      worm.addPiece(wormPiece)
    } else {
      worms += new Worm(wormPiece.player, Set(wormPiece))
    }

  }

  def getWorm(ownerName: String): Worm = {
    def getWorm_(worms: Iterable[Worm], ownerName: String): Worm = {
      if (worms.isEmpty) throw new RuntimeException("Worm for followring player is not in playground model: " + ownerName)
      else if (worms.head.owner.name == ownerName) worms.head
      else (getWorm_(worms.tail, ownerName))
    }
    getWorm_(worms, ownerName)
  }

  case class NodeChanged(val node: Node) extends Event
}

abstract class Node {
  def row: Int
  def col: Int
}

case class Empty(val row: Int, val col: Int) extends Node
case class WormPiece(val row: Int, val col: Int, val wormType: String, val player: Player) extends Node
case class Wall(val row: Int, val col: Int) extends Node
case class Collectible(val row: Int, val col: Int, val expiresInTurn: Int, val points: Int) extends Node

case class Worm(val owner: Player, var pieces: Set[WormPiece]) {
  def addPiece(piece: WormPiece) = {
    pieces += piece
  }
}