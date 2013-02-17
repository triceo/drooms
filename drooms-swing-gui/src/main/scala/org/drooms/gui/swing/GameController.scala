package org.drooms.gui.swing

import org.drooms.gui.swing.event.NoOpEventPublisher

trait GameController {
  def nextTurn(): GameTurn
  def hasNextTurn(): Boolean
  def getTurnState(turn: Int): TurnState
  def nextTurnNumber(): Int
  def setNextTurnNumber(number: Int): Unit
}

class ReplayGameController(val gameReport: GameReport) extends GameController {
  val totalTurns = gameReport.turns.size
  var turnNumber = 0
  val turnsStates = gameReport.createTurnsStates()

  def setNextTurnNumber(number: Int) = {
    turnNumber = number
  }
  
  /**
   * Returns next game turn. Each GameTurn contains set of {@link TurnStep}s that
   * should be performed within that turn.
   */
  override def nextTurn(): GameTurn = {
    println(turnNumber)
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

  def getTurnState(turnNo: Int): TurnState = turnsStates(turnNo)

}

case class TurnState(val playgroundModel: PlaygroundModel, val players: Map[String, Int])
