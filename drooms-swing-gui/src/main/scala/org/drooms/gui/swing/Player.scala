package org.drooms.gui.swing

import java.awt.Color

class Player(val name: String, var color: Color) {
  def this(name: String) = this(name, PlayerColors.DEFAULT_COLOR)

  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[Player] && obj.asInstanceOf[Player].name == this.name
  }

  override def hashCode(): Int = this.name.hashCode()

}

object PlayerColors {
  val DEFAULT_COLOR = Color.CYAN

  val colors = List(
    Color.CYAN,
    Color.ORANGE,
    Color.YELLOW,
    Color.GREEN,
    Color.GRAY,
    Color.BLUE,
    Color.MAGENTA,
    Color.DARK_GRAY,
    new Color(100, 100, 100),
    Color.PINK)
  var nextColor: Int = 0

  def getNext(): Color = {
    val color = colors(nextColor)
    nextColor += 1
    color
  }

  def reset(): Unit = {
    nextColor = 0
  }
}