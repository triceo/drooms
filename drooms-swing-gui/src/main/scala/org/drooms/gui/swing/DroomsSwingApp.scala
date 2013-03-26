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

/**
 * Main class for the entire Swing application.
 *
 * Defines {@link MainFrame}.
 */
object DroomsSwingApp extends SimpleSwingApplication with Logging {
  private val eventBus = EventBusFactory.get()
  private var gameController: GameController = _
  //var gameReport: (GameReport, File) = _
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
        // TODO add common methods for creating both panes
        //gameReport = (report, file)
        logger.debug("Creating new left pane with Replay capatabilities")
        gameController = new ReplayGameController(report)
        val playersList = PlayersListFactory.createPlayersList(report.players)
        val playground = new Playground(playersList)
        playground.create(report)
        val leftPane = new LeftPane(playground, ControlPanel.newReplayControlPanel())
        val rightPane = new RightPane(playersList)
        createContents(leftPane, rightPane)
        eventBus.publish(ReplayInitialized(report))

      case NextTurnInitiated =>
        val turn = gameController.getNextTurn()
        logger.debug("Performing turn number " + turn.number)
        for (step <- turn.steps) {
          eventBus.publish(TurnStepPerformed(step))
        }
        eventBus.publish(NextTurnPerformed(turn.number))
        if (!gameController.hasNextTurn()) {
          logger.debug("Replay finished...")
          eventBus.publish(ReplayStateChangeRequested(ReplayFinished))
        }

      case ReplayResetRequested =>
        // todo reset replay
        gameController.restartGame()
        eventBus.publish(GoToTurn(0))
        eventBus.publish(ReplayStateChanged(ReplayNotStarted))

      case TurnDelayChanged(value) =>
        turnDelay = value
        timer match {
          // currently running replay;; update timer to new delay
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

      case PreviousTurnRequested =>
        logger.debug("Going to previous turn.")
        eventBus.publish(GoToTurn(gameController.prevTurnNumber))

      case GoToTurn(turnNo) =>
        logger.debug("Going to turn number " + turnNo)
        val turnState = gameController.getTurnState(turnNo)
        eventBus.publish(GoToTurnState(turnNo, turnState))

      case GoToTurnState(number, state) =>
        if (number <= 0)
          eventBus.publish(ReplayStateChangeRequested(ReplayNotStarted))
        else {
          gameController.currentTurnNumber = number
          val turn = gameController.getCurrentTurn()
          for (step <- turn.steps) {
            eventBus.publish(new TurnStepPerformed(step))
          }
        }

      case NewGameRequested =>
        new NewGameDialog().show() match {
          case Some(config) =>
            eventBus.publish(NewGameCreated(config))
          case None => // do nothing
        }

      case NewGameCreated(config) =>
        gameController = RealTimeGameController.createNew(config)

    }

    def cancelReplayTimer(): Unit = {
      logger.debug("Cancelling the replay timer!")
      timer.map(_.cancel())
      timer = None
    }
    class ExecuteNextTurn extends TimerTask {
      def run(): Unit = {
        if (gameController.hasNextTurn()) {
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

class LeftPane(val playground: Playground, val controlPanel: ControlPanel) extends BorderPanel {
  import BorderPanel.Position._
  layout(playground) = Center
  layout(controlPanel) = South
}

class RightPane(var playersList: PlayersList) extends BoxPanel(Orientation.Horizontal) with Reactor {
  val playersListView = new PlayersListView(playersList)
  contents += playersListView
}
