package org.drooms.gui.swing

trait GameController {
  def nextTurn(): GameTurn
  def hasNextTurn(): Boolean
}

class ReplayGameController(val gameReport: GameReport) extends GameController {
  val totalTurns = gameReport.turns.size
  var turnNumber = 0

  /**
   * Returns next game turn. Each GameTurn contains set of {@link TurnStep}s that
   * should be performed within that turn.
   */
  override def nextTurn(): GameTurn = {
    if (!hasNextTurn())
      throw new RuntimeException("Can't get next turn, game already finished!")
    else {
      val turn = gameReport.turns(turnNumber)
      turnNumber += 1
      turn
    }
  }

  def hasNextTurn(): Boolean = turnNumber < totalTurns
  
  def nextTurnNumber(): Int = turnNumber
}
