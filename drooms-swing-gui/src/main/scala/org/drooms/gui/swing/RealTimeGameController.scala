package org.drooms.gui.swing

import java.io.{File, Writer}
import javax.swing.SwingUtilities

import com.typesafe.scalalogging.slf4j.Logging
import org.drooms.api.Player
import org.drooms.gui.swing.event.{NewTurnAvailable, GameStateChanged, EventBusFactory}
import org.drooms.impl.DroomsGame
import org.drooms.impl.util.PlayerProperties

import scala.collection.JavaConversions

object RealTimeGameController extends Logging {
  /**
   * Creates new real-time game controller based on the specified configuration.
   */
  def createNew(gameConfig: GameConfig): RealTimeGameController = {
    val reportDir = new File("reports")
    val playersFile = File.createTempFile("drooms-swing-gui", "players")
    playersFile.deleteOnExit()
    new PlayerProperties(playersFile).write(JavaConversions.seqAsJavaList(gameConfig.players.collect {
      case player:PlayerInfo => new Player(player.name, player.strategyGroupId, player.strategyArtifactId, player
        .strategyVersion)
    }))
    new RealTimeGameController(classOf[org.drooms.impl.DefaultGame], reportDir, gameConfig.playgroundFile,
      playersFile, gameConfig.gameProperties)
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
  val playgroundFile: File,
  /** List of players that will be playing the game. */
  val playersFile: File,
  /** Game configuration file */
  val gamePropertiesFile: File)

  extends org.drooms.api.GameProgressListener with Logging {

  val eventBus = EventBusFactory.get()
  val players = new PlayerProperties(playersFile).read()
  val playground = new DroomsGame(gameClass, playgroundFile, players, gamePropertiesFile, reportDir).getPlayground()
  /**
   * Turn steps for the current not finished {@link Turn}. These step are gathered from background {@link Game} thread
   * based on the incoming events.
   */
  private var currentTurnSteps = List[TurnStep]()
  private var currentTurnState: TurnState = _
  /** List of completed turns. */
  @volatile
  private var finished = false
  // TODO use actors??
  private var gameThread: Thread = _
  private var currentTurnNumber = 0

  def pauseGame(): Unit = ???

  def restartGame(): Unit = {
    logger.info("Restarting game...")
    stopGame()
    startOrContinueGame()
  }

  /**
   * Starts new {@link DroomsGame} in background thread
   */
  def startOrContinueGame(): Unit = {
    currentTurnNumber = 0
    currentTurnSteps = List()
    currentTurnState = createInitialState()
    logger.info("Starting new Drooms game.")
    val listener = this
    // TODO determine if want to start or continue the game
    gameThread = new Thread() {
      override def run() {
        val game = new DroomsGame(gameClass, playgroundFile, new PlayerProperties(playersFile).read(), gamePropertiesFile, reportDir)
        game.addListener(listener)
        game.play("Drooms game")
      }
    }
    // TODO log info like, game props, players info, etc
    gameThread.start()
    finished = false
    eventBus.publish(GameStateChanged(GameRunning))
    logger.info("Game successfully started.")
  }

  def createInitialState(): TurnState = {
    val playgroundWidth = playground.getWidth()
    val playgroundHeight = playground.getHeight()
    val playgroundEmptyNodes =
      (for (
        x <- 0 until playgroundWidth;
        y <- 0 until playgroundHeight;
        if (playground.isAvailable(x, y))
      ) yield Node(x, y)).toSet
    val initialPlayground = new PlaygroundModel(playgroundWidth, playgroundHeight, EventBusFactory.getNoOp())
    initialPlayground.emptyNodes(playgroundEmptyNodes)
    //initialPlayground.initWorms(wormInitPositions)
    val initPlayers = JavaConversions.asScalaBuffer(players).map(_.getName -> 0).toMap
    new TurnState(initialPlayground, initPlayers)
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
  /**
   * Called from the background running {@link org.drooms.api.Game} before the start of the next turn.
   *
   * New {@link Turn} is created from current {@link TurnStep} and the list of current turn steps is cleared.
   */
  def nextTurn(): Unit = {
    // publish current turn
    val newTurn = new GameTurn(currentTurnNumber, currentTurnSteps.reverse)
    logger.debug(s"New turn number ${currentTurnNumber} available")
    currentTurnNumber += 1
    currentTurnSteps = List()
    val newTurnState = TurnState.updateState(currentTurnState, newTurn)
    currentTurnState = newTurnState
    SwingUtilities.invokeAndWait(new Runnable() {
      def run(): Unit = {
        eventBus.publish(NewTurnAvailable(newTurn, newTurnState))
      }
    })
  }

  def collectibleAdded(c: org.drooms.api.Collectible): Unit = {
    currentTurnSteps ::= new CollectibleAdded(createCollectible(c))
  }

  private def createCollectible(c: org.drooms.api.Collectible): Collectible = {
    new Collectible(Node(c.getAt().getX(), c.getAt().getY()), c.expiresInTurn(), c.getPoints())
  }

  def collectibleCollected(c: org.drooms.api.Collectible, p: org.drooms.api.Player, points: Int): Unit = {
    currentTurnSteps ::= new CollectibleCollected(p.getName(), createCollectible(c))
  }

  def collectibleRemoved(c: org.drooms.api.Collectible): Unit = {
    currentTurnSteps ::= new CollectibleRemoved(createCollectible(c))
  }

  def playerCrashed(p: org.drooms.api.Player): Unit = {
    currentTurnSteps ::= new WormCrashed(p.getName())
  }

  def playerDeactivated(p: org.drooms.api.Player): Unit = {
    currentTurnSteps ::= new WormDeactivated(p.getName())
  }

  def playerPerformedAction(p: org.drooms.api.Player, m: org.drooms.api.Action, nodes: org.drooms.api.Node*): Unit = {
    currentTurnSteps ::= new WormMoved(p.getName(), transformNodes(nodes))
  }

  private def transformNodes(nodes: Seq[org.drooms.api.Node]): List[Node] = {
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
