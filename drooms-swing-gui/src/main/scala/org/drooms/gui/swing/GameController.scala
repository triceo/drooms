package org.drooms.gui.swing

trait GameController {
  def nextTurn(): GameTurn
  def hasNextTurn(): Boolean
}

class ReplayGameController(val gameReport: GameReport) extends GameController {
  val totalTurns = gameReport.turns.size
  var turnNumber = 0

  // should return set of steps within that turn
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

//class RealTimeGameController extends GameController {
//  override def nextTurn: GameTurn = {
//    new GameTurn(0, List())
//  }
//  override def hasNextTurn(): Boolean = false
//}