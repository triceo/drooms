package org.drooms.gui.swing

import com.typesafe.scalalogging.slf4j.Logging
import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.EventBus

object ReplayController {
  def createNew(gameReport: GameReport): ReplayController = {
    new ReplayController(EventBusFactory.get(), gameReport.turns, gameReport.createTurnsStates())
  }
  
  def createNew(): ReplayController = {
    new ReplayController(EventBusFactory.get(), List(), List())
  }
}
/**
 * Class used to drive (interact with) the Dooms game replay.
 */
class ReplayController(
  val eventBus: EventBus,
  var turns: List[GameTurn],
  var turnStates: List[TurnState])
    
    extends Logging {
  logger.debug("Number of turns for current replay: " + turns.size)
  logger.debug("Number of turn states for current replay: " + turnStates.size)

  def totalTurns = turns.size
  /**
   * Current replay turn.
   */
  var currentTurnNumber = 0

  def nextTurnNumber: Int = currentTurnNumber + 1

  def prevTurnNumber: Int = currentTurnNumber - 1

  def getCurrentTurn(): GameTurn = turns(currentTurnNumber)

  def getCurrentTurnState(): TurnState = getTurnState(currentTurnNumber)
  
  def addTurn(turn: GameTurn, state: TurnState): Unit = {
    turns = turns ::: List(turn)
    turnStates = turnStates ::: List(state)
  }
  /**
   * Returns next game turn. Each {@link GameTurn} is represented as set of {@link TurnStep}s that
   * should be performed within that turn. Current turn number is incrementing within this method.
   *
   * @see GameTurn
   * @see TurnStep
   */
  def getNextTurn(): GameTurn = {
    if (!hasNextTurn())
      throw new RuntimeException("Can't get next turn, becuase replay does not have more turns!!")
    else {
      val turn = turns(nextTurnNumber)
      currentTurnNumber = nextTurnNumber
      turn
    }
  }

  def getTurnState(turnNo: Int): TurnState = {
    turnStates(turnNo)
  }

  /**
   * Determines if the replay has next turn.
   *
   * @return {@code true} if the replay has next turn, {@code false} otherwise
   */
  def hasNextTurn(): Boolean = {
    // turns are numbered from zero, so the turn numbers are 0 to totalTurns - 1
    nextTurnNumber < totalTurns
  }

  /**
   * Restarting replay just means setting the current turn number to 0, as that is the first turn.
   */
  def restartReplay(): Unit =
    currentTurnNumber = 0

  /**
   * Game loaded from report is always finished.
   */
  def isGameFinished(): Boolean = true
}

/**
 * Parent trait for all replay states.
 */
trait ReplayState
case object ReplayNotStarted extends ReplayState
case object ReplayRunning extends ReplayState
case object ReplayPaused extends ReplayState
case object ReplayFinished extends ReplayState