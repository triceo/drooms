package org.drooms.gui.swing

import org.drooms.gui.swing.event.NoOpEventPublisher

trait GameController {
  def nextTurn(): GameTurn
  def hasNextTurn(): Boolean
  def getTurnState(turn: Int): TurnState
  def nextTurnNumber(): Int
  def prevTurnNumber(): Int
  def setNextTurnNumber(number: Int): Unit
}

/**
 * Turns are numbered starting from 0, like in the XML report. Turn states are numbered from 0, but
 * 0th turn state is the initial one (player starting positions), so 0th turn in XML report == 1st turn state.
 */
class ReplayGameController(val gameReport: GameReport) extends GameController {
  val totalTurns = gameReport.turns.size
  var nextTurnNumber = 0
  val turnsStates = gameReport.createTurnsStates()

  def setNextTurnNumber(number: Int) = {
    nextTurnNumber = number
  }
  
  /**
   * Returns next game turn. Each GameTurn contains set of {@link TurnStep}s that
   * should be performed within that turn.
   */
  override def nextTurn(): GameTurn = {
    if (!hasNextTurn())
      throw new RuntimeException("Can't get next turn, game already finished!")
    else {
      val turn = gameReport.turns(nextTurnNumber)
      nextTurnNumber += 1
      turn
    }
  }

  def hasNextTurn(): Boolean = nextTurnNumber < totalTurns

  def prevTurnNumber(): Int = nextTurnNumber - 2

  def getTurnState(turnNo: Int): TurnState = turnsStates(turnNo + 1)

}

case class TurnState(val playgroundModel: PlaygroundModel, val players: Map[String, Int])
