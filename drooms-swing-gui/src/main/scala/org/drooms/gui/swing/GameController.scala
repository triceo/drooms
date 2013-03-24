package org.drooms.gui.swing

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.Writer
import java.util.ArrayList
import java.util.Properties
import org.drooms.api.GameProgressListener
import org.drooms.impl.DefaultGame
import org.drooms.impl.DefaultPlayground
import org.drooms.impl.DroomsGame
import org.drooms.impl.util.PlayerAssembly
import org.drooms.impl.util.properties.GameProperties
import com.typesafe.scalalogging.slf4j.Logging
import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.NextTurnAvailable

/**
 * Represents trait that can interact with the Drooms game.
 *
 * Contains all game related information like what events happened in each turn or
 * complete history of all turns.
 *
 * Basically there are possible two implementations:
 *  - one that interprets previously generated {@link GameReport}. See {@link ReplayGameController}
 *  - one that runs real-time game on background and gets the results directly. See {@link RealTimeGameController}
 *
 */
trait GameController {
  /**
   * Number associated with the turn that is considered as current from client point of view.
   * This number is only updated by calling certain methods. When running real-time game the number
   * is not automatically updated when the turns are completed.
   */
  var currentTurnNumber: Int
  def nextTurnNumber: Int
  def prevTurnNumber: Int
  /**
   * Returns next game turn. Each {@link GameTurn} is represented as set of {@link TurnStep}s that
   * should be performed within that turn.
   *
   * @see GameTurn
   * @see TurnStep
   */
  def getNextTurn(): GameTurn
  def getCurrentTurn(): GameTurn

  /**
   * Determines if the current game has next turn.
   *
   * @return {@code true} if the game has next turn, {@code false} otherwise
   */
  def hasNextTurn(): Boolean

  def getTurnState(turn: Int): TurnState

  /**
   * Starts the game.
   */
  def startGame(): Unit

  /**
   * Restarts the game.
   */
  def restartGame(): Unit

  /**
   * Determines if the current has already finished or not.
   */
  def isGameFinished(): Boolean
}

/**
 * Implements methods common for concrete game controller implementations.
 *
 * Turns are numbered starting from 0, like in the XML report. Turn states are numbered also from 0, but
 * 0th turn state is the initial one (player starting positions), so 0th turn in XML report == 1st turn state.
 *
 * @see GameController
 */
abstract class AbstractGameController extends GameController with Logging {
  val eventBus = EventBusFactory.get()
  var currentTurnNumber = 0

  def turnStates: List[TurnState]

  def turns: List[GameTurn]

  def getCurrentTurn(): GameTurn = turns(currentTurnNumber)
  
  def nextTurnNumber: Int = currentTurnNumber + 1

  def prevTurnNumber: Int = currentTurnNumber - 1


  /**
   * Returns the current turn and sets current turn number to next turn number (meaning incrementing by 1)
   */
  def getNextTurn(): GameTurn = {
    if (!hasNextTurn())
      throw new RuntimeException("Can't get next turn!")
    else {
      val turn = turns(nextTurnNumber)
      currentTurnNumber = nextTurnNumber
      turn
    }
  }

  def getTurnState(turnNo: Int): TurnState = {
    turnStates(turnNo)
  }

  def getCurrentTurnState(): TurnState = getTurnState(currentTurnNumber)

}

/**
 * {@link GameController} implementation that uses the {@link GameReport}
 *
 * @see GameController
 */
class ReplayGameController(val gameReport: GameReport) extends AbstractGameController {
  val turns = gameReport.turns
  val totalTurns = gameReport.turns.size
  val turnStates = gameReport.createTurnsStates()
  logger.debug("Number of turns for current replay: " + turns.size)
  logger.debug("Number of turn states for current replay: " + turnStates.size)

  def hasNextTurn(): Boolean = nextTurnNumber < totalTurns

  /**
   * Game is represented as {@link GameReport} so there is nothing involved in starting the game.
   */
  def startGame(): Unit = {
    // do nothing
  }

  /**
   * Restarting game just means setting the current turn number to 0, as that is the first turn.
   */
  def restartGame(): Unit =
    currentTurnNumber = 0

  /**
   * Game loaded from report is always finished.
   */
  def isGameFinished(): Boolean = true
}

object RealTimeGameController extends Logging {
  /**
   * Creates new real-time game controller based on the specified configuration.
   */
  def createNew(gameConfig: NewGameConfig): RealTimeGameController = {
    val reportDir = new File("reports")
    
    new RealTimeGameController(classOf[org.drooms.impl.DefaultGame], reportDir, gameConfig.playground, gameConfig.players, gameConfig.gameProperties)
  }
}

/**
 * Class used for controlling (starting, pausing, stopping, etc) real-time Drooms game.
 *
 * Game is started in new thread and updates the turns as they are finished over time.
 *
 * @see GameController
 */
class RealTimeGameController (
  /** Drooms game class used to drive the game. */
  val gameClass: Class[org.drooms.impl.DefaultGame],
  /** Directory used to store game reports. */
  val reportDir: File,
  /** Playground definition */
  val playground: org.drooms.api.Playground,
  /** List of players that will be playing the game. */
  val players: java.util.List[org.drooms.api.Player],
  /** Game configuration */
  val gameProperties: org.drooms.impl.util.properties.GameProperties) extends AbstractGameController
  with org.drooms.api.GameProgressListener with Logging {

  var turns = List[GameTurn]()
  var turnStates = List[TurnState]()

  /**
   * Turn steps for the current not finished {@link Turn}. These step are gathered from background {@link Game} thread
   * based on the incoming events.
   */
  @volatile
  private var currentTurnSteps = List[TurnStep]()
  /** List of completed turns. */
  @volatile
  private var finished = false
  // TODO use actors??
  private var gameThread: Thread = _

  /**
   * Starts new {@link DroomsGame} in background thread
   */
  def startGame(): Unit = {
    logger.info("Starting new Drooms game.")
    val listeners = new ArrayList[GameProgressListener]()
    listeners.add(this)
    gameThread = new Thread() {
      override def run() {
        new DroomsGame(gameClass, playground, players,
          gameProperties, listeners, reportDir).play("Drooms game")
      }
    }
    // TODO log info like, game props, players info, etc
    gameThread.start()
    logger.info("Game successfully started.")
  }

  def restartGame(): Unit = {
    logger.info("Restarting game...")
    stopGame()
    currentTurnNumber = 0
    currentTurnSteps = List()
    turns = List()
    startGame()
  }

  def stopGame(): Unit = {
    logger.info("Stopping game...")
    gameThread.stop()
  }

  /**
   * Used to determine if there are more turns to be performed.
   */
  def isGameFinished(): Boolean = finished

  /**
   * Indicates if the next turn is available at the moment.
   *
   * Game does not have to (currently) have available next turn even though its not yet finished.
   * Finished game means that there are no more turns to be performed. Next turn could not be available
   * at the time of calling hasNextTurn(), but can be available later, when the background {@link Game} finishes turn.
   *
   */
  def hasNextTurn(): Boolean = !finished
  /////////////////////////////////////////////////////////////////////////////
  // GameProgressListener methods
  /////////////////////////////////////////////////////////////////////////////
  def collectibleAdded(c: org.drooms.api.Collectible, where: org.drooms.api.Node): Unit = {
    currentTurnSteps ::= new CollectibleAdded(createCollectible(c, where))
  }

  private def createCollectible(c: org.drooms.api.Collectible, where: org.drooms.api.Node): Collectible = {
    new Collectible(Node(where.getX(), where.getY()), c.expiresInTurn(), c.getPoints())
  }

  def collectibleCollected(c: org.drooms.api.Collectible, p: org.drooms.api.Player, where: org.drooms.api.Node,
    points: Int): Unit = {
    currentTurnSteps ::= new CollectibleCollected(p.getName(), createCollectible(c, where))
  }

  def collectibleRemoved(c: org.drooms.api.Collectible, where: org.drooms.api.Node): Unit = {
    currentTurnSteps ::= new CollectibleRemoved(createCollectible(c, where))
  }

  /**
   * Called from the background running {@link org.drooms.api.Game} before the start of the next turn.
   *
   * New {@link Turn} is created from current {@link TurnStep} and the list of current turn steps is cleared.
   */
  def nextTurn(): Unit = {
    // add the current turn into list
    val turnNumber = turns.size
    turns = turns ::: List(new GameTurn(turnNumber, currentTurnSteps))
    currentTurnSteps = List()
    eventBus.publish(NextTurnAvailable)
  }

  def playerCrashed(p: org.drooms.api.Player): Unit = {
    currentTurnSteps ::= new WormCrashed(p.getName())
  }

  def playerDeactivated(p: org.drooms.api.Player): Unit = {
    currentTurnSteps ::= new WormDeactivated(p.getName())
  }

  def playerMoved(p: org.drooms.api.Player, m: org.drooms.api.Move, nodes: org.drooms.api.Node*): Unit = {
    currentTurnSteps ::= new WormMoved(p.getName(), transformNodes(nodes))
  }

  private def transformNodes(nodes: Seq[org.drooms.api.Node]) = {
    (for (node <- nodes) yield Node(node.getX(), node.getY())).toList
  }

  def playerSurvived(p: org.drooms.api.Player, points: Int): Unit = {
    currentTurnSteps ::= new WormSurvived(p.getName(), points)
  }

  /**
   * Write a report of the current state.
   *
   * @param w
   *            Where to write.
   * @throws IOException
   *             When it cannot be written.
   */
  def write(w: Writer): Unit = {
    // called at the end of the game
    finished = true
  }
}
