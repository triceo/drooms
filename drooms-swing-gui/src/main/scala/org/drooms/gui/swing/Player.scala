package org.drooms.gui.swing

import java.awt.Color

class Player(val name: String, var currentScore: Int, var color: Color) {
  def this(name: String) = this(name, 0, PlayerColors.DEFAULT_COLOR)

  def addPoints(points: Int): Unit = currentScore += points

  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[Player] && obj.asInstanceOf[Player].name == this.name
  }

  override def hashCode(): Int = this.name.hashCode()
}

object PlayerColors {
  val DEFAULT_COLOR = Color.CYAN

  val colors = List(
    new Color(79, 0xBE, 0xDB),
    new Color(0xE8, 68, 50),
    new Color(0xC0, 0xC0, 0xC0),
    new Color(0xFF, 99, 00),
    new Color(0xFF, 0xCC, 00),
    new Color(99, 0xFF, 00),
    new Color(0xFF, 0xBA, 0xD2),
    new Color(0xC9, 0xA7, 98),
    new Color(20, 139, 66),
    new Color(0x7C, 88, 0x9A),
    // in case more than 10 players in the game
    Color.ORANGE,
    Color.YELLOW,
    Color.GREEN,
    Color.GRAY,
    Color.BLUE,
    Color.MAGENTA,
    Color.DARK_GRAY,
    Color.PINK)
  var nextColorIndex: Int = 0

  def getNext(): Color = {
    if (nextColorIndex >= colors.size)
      nextColorIndex = 0
    val color = colors(nextColorIndex)
    nextColorIndex += 1
    color
  }

  def reset(): Unit = {
    nextColorIndex = 0
  }
}