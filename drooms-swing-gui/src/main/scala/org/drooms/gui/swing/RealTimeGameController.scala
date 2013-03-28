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
import org.drooms.gui.swing.event.GameStateChanged
import javax.swing.SwingUtilities
import org.drooms.gui.swing.event.NewTurnAvailable
import org.drooms.gui.swing.event.GameStateChanged

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
class RealTimeGameController(
  /** Drooms game class used to drive the game. */
  val gameClass: Class[org.drooms.impl.DefaultGame],
  /** Directory used to store game reports. */
  val reportDir: File,
  /** Playground definition */
  val playground: org.drooms.api.Playground,
  /** List of players that will be playing the game. */
  val players: java.util.List[org.drooms.api.Player],
  /** Game configuration */
  val gameProperties: org.drooms.impl.util.properties.GameProperties)
  
  extends org.drooms.api.GameProgressListener with Logging {

  val eventBus = EventBusFactory.get()

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

  private var currentTurnNumber = 0
  /**
   * Starts new {@link DroomsGame} in background thread
   */
  def startOrContinueGame(): Unit = {
    logger.info("Starting new Drooms game.")
    val listener = this
    gameThread = new Thread() {
      override def run() {
        val game = new DroomsGame(gameClass, playground, players,
          gameProperties, reportDir)
        game.addListener(listener)
        game.play("Drooms game")
      }
    }
    // TODO log info like, game props, players info, etc
    gameThread.start()
    eventBus.publish(GameStateChanged(GameRunning))
    logger.info("Game successfully started.")
  }

  def pauseGame(): Unit = {
    
  }
  
  def restartGame(): Unit = {
    logger.info("Restarting game...")
    stopGame()
    startOrContinueGame()
  }

  def stopGame(): Unit = {
    logger.info("Stopping game...")
    gameThread.stop()
    currentTurnNumber = 0
  }

  /**
   * Indicates if the next turn is available at the moment.
   *
   * Game does not have to (currently) have available next turn even though its not yet finished.
   * Finished game means that there are no more turns to be performed. Next turn could not be available
   * at the time of calling hasNextTurn(), but can be available later, when the background {@link Game} finishes turn.
   *
   */
  def hasNextTurn(): Boolean = !finished
  
  def isGameFinished(): Boolean = finished
  /////////////////////////////////////////////////////////////////////////////
  // GameProgressListener methods
  /////////////////////////////////////////////////////////////////////////////
  def collectibleAdded(c: org.drooms.api.Collectible, where: org.drooms.api.Node): Unit = {
    currentTurnSteps ::= new CollectibleAdded(createCollectible(c, where))
  }

  def collectibleCollected(c: org.drooms.api.Collectible, p: org.drooms.api.Player, where: org.drooms.api.Node,
    points: Int): Unit = {
    currentTurnSteps ::= new CollectibleCollected(p.getName(), createCollectible(c, where))
  }

  def collectibleRemoved(c: org.drooms.api.Collectible, where: org.drooms.api.Node): Unit = {
    currentTurnSteps ::= new CollectibleRemoved(createCollectible(c, where))
  }

  private def createCollectible(c: org.drooms.api.Collectible, where: org.drooms.api.Node): Collectible = {
    new Collectible(Node(where.getX(), where.getY()), c.expiresInTurn(), c.getPoints())
  }
  
  /**
   * Called from the background running {@link org.drooms.api.Game} before the start of the next turn.
   *
   * New {@link Turn} is created from current {@link TurnStep} and the list of current turn steps is cleared.
   */
  def nextTurn(): Unit = {
    // publish current turn
    val newTurn = new GameTurn(currentTurnNumber, currentTurnSteps)
    logger.debug(s"New turn number ${currentTurnNumber} available")
    currentTurnNumber += 1
    currentTurnSteps = List()
    SwingUtilities.invokeAndWait(new Runnable() {
      def run(): Unit = {
        eventBus.publish(NewTurnAvailable(newTurn))
      }
    })
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
    logger.debug("Game finished!")
    finished = true
    eventBus.publish(GameStateChanged(GameFinished))
  }
}

trait GameState
case object GameNotStarted extends GameState
case object GameRunning extends GameState
case object GamePaused extends GameState
case object GameStopped extends GameState
case object GameFinished extends GameState
