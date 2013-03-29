package org.drooms.gui.swing

import java.awt.Dimension
import java.util.Timer
import java.util.TimerTask
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.BorderPanel.Position.South
import scala.swing.BoxPanel
import scala.swing.Component
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.Reactor
import scala.swing.SimpleSwingApplication
import scala.swing.SplitPane
import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.GoToTurn
import org.drooms.gui.swing.event.GoToTurnState
import org.drooms.gui.swing.event.NewGameCreated
import org.drooms.gui.swing.event.NewGameReportChosen
import org.drooms.gui.swing.event.NewGameRequested
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.NextTurnPerformed
import org.drooms.gui.swing.event.PreviousTurnRequested
import org.drooms.gui.swing.event.ReplayInitialized
import org.drooms.gui.swing.event.ReplayResetRequested
import org.drooms.gui.swing.event.ReplayStateChangeRequested
import org.drooms.gui.swing.event.ReplayStateChanged
import org.drooms.gui.swing.event.TurnDelayChanged
import org.drooms.gui.swing.event.TurnStepPerformed
import com.typesafe.scalalogging.slf4j.Logging
import javax.swing.SwingUtilities
import java.io.File
import org.drooms.gui.swing.event.ReplayStateChanged
import org.drooms.gui.swing.event.NewGameAccepted
import scala.swing.Separator
import org.drooms.gui.swing.event.NewUIComponentsRequested
import org.drooms.gui.swing.event.GameStateChangeRequested
import org.drooms.gui.swing.event.GameStateChanged
import org.drooms.gui.swing.event.NewTurnAvailable
import org.drooms.gui.swing.event.GameRestartRequested

/**
 * Main class for the entire Swing application.
 *
 * Defines {@link MainFrame}.
 */
object DroomsSwingApp extends SimpleSwingApplication with Logging {
  private val eventBus = EventBusFactory.get()
  private var replayController: Option[ReplayController] = None
  private var gameController: Option[RealTimeGameController] = None
  var turnDelay = 100

  def top = new MainFrame {
    title = "Drooms"
    minimumSize = new Dimension(1300, 700)
    menuBar = new MainMenu()
    listenTo(eventBus)

    def createContents(leftPane: Component, rightPane: Component): Unit = {
      contents = new SplitPane(Orientation.Vertical, leftPane, rightPane) {
        resizeWeight = 1.0
        rightComponent.minimumSize = new Dimension(200, 500)
        leftComponent.minimumSize = new Dimension(500, 500)
      }
      repaint()
    }
    var timer: Option[Timer] = None

    reactions += {
      case NewGameReportChosen(report, file) =>
        eventBus.publish(NewUIComponentsRequested)
        cancelReplayTimer()
        logger.debug("Creating new left pane with Replay capatabilities")
        replayController = Some(ReplayController.createNew(report))
        val playersList = PlayersListFactory.createPlayersList(report.players)
        val playground = new PlaygroundView(playersList)
        playground.create(report)
        val leftPane = new LeftPane(playground, ControlPanel.newReplayControlPanel())
        val rightPane = new RightPane(playersList)
        createContents(leftPane, rightPane)
        eventBus.publish(ReplayInitialized(report))

      case NextTurnInitiated =>
        val rc = replayController.getOrElse(
          throw new IllegalStateException("Can't perform next turn when there is no replay controller!"))
        val turn = rc.getNextTurn()
        logger.debug("Performing turn number " + turn.number)
        for (step <- turn.steps) {
          eventBus.publish(TurnStepPerformed(step))
        }
        eventBus.publish(NextTurnPerformed(turn.number))
        if (!rc.hasNextTurn()) {
          logger.debug("Replay finished...")
          eventBus.publish(ReplayStateChangeRequested(ReplayFinished))
        }

      case ReplayResetRequested =>
        val rc = replayController.getOrElse(
          throw new IllegalStateException("Can't reset replay when there is no replay controller!"))
        println("Replay reset requested")
        rc.restartReplay()
        eventBus.publish(GoToTurn(1))
        eventBus.publish(ReplayStateChanged(ReplayNotStarted))

      case TurnDelayChanged(value) =>
        turnDelay = value
        timer match {
          // currently running replay, update timer to new delay
          case Some(x) =>
            x.cancel()
            timer = Some(new Timer())
            timer.map(_.schedule(new ExecuteNextTurn(), turnDelay, turnDelay))
          case None =>
        }

      // TODO move into game controller??
      case ReplayStateChangeRequested(toState) => {
        toState match {
          case ReplayNotStarted | ReplayPaused =>
            cancelReplayTimer()

          case ReplayRunning =>
            cancelReplayTimer()
            logger.debug("Creating new replay timer with delay " + turnDelay)
            timer = Some(new Timer())
            timer.map(_.schedule(new ExecuteNextTurn(), 0, turnDelay))

          case ReplayFinished =>
            logger.debug("Replay finished -> cancelling the timer thread")
            cancelReplayTimer()

        }
        eventBus.publish(ReplayStateChanged(toState))
      }

      case NewTurnAvailable(turn, state) =>
        replayController.map(_.addTurn(turn, state))

      case GameStateChangeRequested(toState) => {
        val gc = gameController.getOrElse(
          throw new IllegalStateException("Can't change the game state when there is no game controller!"))
        toState match {
          case GameNotStarted =>
            gc.stopGame()

          case GameRunning =>
            gc.startOrContinueGame()

          case GamePaused =>
            gc.pauseGame()

          case GameStopped =>
            gc.stopGame()
        }
        eventBus.publish(GameStateChanged(toState))
      }

      case GameRestartRequested =>
        eventBus.publish(GameStateChangeRequested(GameNotStarted))
        replayController = Some(ReplayController.createNew()) 
        eventBus.publish(GameStateChangeRequested(GameRunning))

      case PreviousTurnRequested =>
        val rc = replayController.getOrElse(
          throw new IllegalStateException("Can't go to previous turn when there is not replay controller!"))
        logger.debug("Going to previous turn.")
        eventBus.publish(GoToTurn(rc.prevTurnNumber))

      case GoToTurn(turnNo) =>
        val rc = replayController.getOrElse(
          throw new IllegalStateException("Can't got to specified turn when there is not replay controller!"))
        logger.debug("Going to turn number " + turnNo)
        val turnState = rc.getTurnState(turnNo)
        rc.currentTurnNumber = turnNo
        eventBus.publish(GoToTurnState(turnNo, turnState))

      case NewGameRequested =>
        new NewGameDialog().show() match {
          case Some(config) =>
            eventBus.publish(NewGameAccepted(config))
          case None =>
        }

      case NewGameAccepted(config) =>
        //eventBus.publish() clean-up event
        gameController = Some(RealTimeGameController.createNew(config))
        replayController = Some(ReplayController.createNew())
        val playersList = PlayersListFactory.createPlayersList(config.getPlayersNames())
        val playground = new PlaygroundView(playersList)
        playground.create(config)
        val leftPane = new LeftPane(playground, ControlPanel.newReplayControlPanel(), ControlPanel.newRealTimeGameControlPanel())
        val rightPane = new RightPane(playersList)
        createContents(leftPane, rightPane)
        eventBus.publish(NewGameCreated(config))

      case NewGameCreated(config) =>

    }

    def cancelReplayTimer(): Unit = {
      logger.debug("Cancelling the replay timer!")
      timer.map(_.cancel())
      timer = None
    }
    class ExecuteNextTurn extends TimerTask {
      def run(): Unit = {
        if (replayController.forall(_.hasNextTurn())) {
          SwingUtilities.invokeAndWait(new Runnable() {
            override def run(): Unit = {
              eventBus.publish(NextTurnInitiated)
            }
          })
        }
      }
    }
    centerOnScreen()
  }
}

class LeftPane(val playground: PlaygroundView, val controlPanels: ControlPanel*) extends BorderPanel {
  import BorderPanel.Position._
  layout(playground) = Center
  layout(new BoxPanel(Orientation.Vertical) {
    for (c <- controlPanels) {
      contents += c
      contents += new Separator
    }
  }) = South
}

class RightPane(val playersList: PlayersList) extends BoxPanel(Orientation.Horizontal) with Reactor {
  val playersListView = new PlayersListView(playersList)
  contents += playersListView
}
