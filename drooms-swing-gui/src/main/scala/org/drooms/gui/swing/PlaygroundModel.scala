package org.drooms.gui.swing

import scala.swing.Publisher
import scala.swing.event.Event

class PlaygroundModel(val height: Int, val width: Int) extends Publisher {
  val nodes = new Array[Array[Node]](height, width)
  for (i <- 0 until height; j <- 0 until width) {
    nodes(i)(j) = new Empty(i, j)
  }

  def updateNode(node: Node): Unit = {
    nodes(node.row)(node.col) = node
    publish(new NodeChanged(node))
  }

  case class NodeChanged(val node: Node) extends Event
}

abstract class Node {
  def row: Int
  def col: Int
}
case class Empty(val row: Int, val col: Int) extends Node
case class Worm(val row: Int, val col: Int, val wormType: String, val player: Player) extends Node
case class Wall(val row: Int, val col: Int) extends Node
case class Collectible(val row: Int, val col: Int, val expiresInTurn: Int) extends Node