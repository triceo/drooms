package org.drooms.gui.swing

import java.awt.Color

/** 
 * Represents player in the game. Each player has a unique name, current score (points) and color.
 */
case class Player(val name: String, var score: Int, var color: Color) {
  def this(name: String) = this(name, 0, PlayerColors.DEFAULT_COLOR)

  def addPoints(points: Int): Unit = 
    score += points
  
  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[Player] && obj.asInstanceOf[Player].name == this.name
  }

  override def hashCode(): Int = this.name.hashCode()
}

/**
 * Used to handle colors assigned for players. Each player should have different {@link Color} 
 */
class PlayerColors(val colors: List[Color]) {
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

object PlayerColors {
  val DEFAULT_COLOR = Color.CYAN
  
  val defaultColors = List(
    new Color(79, 0xBE, 0xDB),
    new Color(0xE8, 68, 50),
    new Color(0xC0, 0xC0, 0xC0),
    new Color(0xFF, 99, 0),
    new Color(0xFF, 0xCC, 0),
    new Color(99, 0xFF, 0),
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
    
    def getDefault(): PlayerColors = new PlayerColors(defaultColors)
}