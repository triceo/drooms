package org.drooms.gui.swing

import java.awt.Dimension
import java.io.File
import java.util.Timer
import java.util.TimerTask

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.Reactor
import scala.swing.SimpleSwingApplication
import scala.swing.SplitPane

import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.GameFinished
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.GameRestarted
import org.drooms.gui.swing.event.GoToTurn
import org.drooms.gui.swing.event.GoToTurnState
import org.drooms.gui.swing.event.NewGameReportChosen
import org.drooms.gui.swing.event.NextTurnInitiated
import org.drooms.gui.swing.event.PreviousTurn
import org.drooms.gui.swing.event.ReplayContinued
import org.drooms.gui.swing.event.ReplayInitiated
import org.drooms.gui.swing.event.ReplayPaused
import org.drooms.gui.swing.event.TurnDelayChanged
import org.drooms.gui.swing.event.TurnStepPerformed
import org.drooms.gui.swing.event.UpdatePlayers

import javax.swing.SwingUtilities

object DroomsSwingApp extends SimpleSwingApplication {
  val eventBus = EventBusFactory.get()
  val leftPane = new LeftPane
  val rightPane = new RightPane
  var gameController: GameController = _
  var gameReport: (GameReport, File) = _
  var turnDelay = 100

  def top = new MainFrame {
    title = "Drooms"
    minimumSize = new Dimension(1300, 700)
    menuBar = new MainMenu()
    listenTo(eventBus)

    contents = new SplitPane(Orientation.Vertical, leftPane, rightPane) {
      resizeWeight = 1.0
      rightComponent.minimumSize = new Dimension(200, 500)
      leftComponent.minimumSize = new Dimension(500, 500)
    }
    var timer: Option[Timer] = None

    reactions += {
      case NewGameReportChosen(report, file) =>
        gameReport = (report, file)
        gameController = new ReplayGameController(report)

      case NextTurnInitiated() =>
        val turn = gameController.nextTurn
        for (step <- turn.steps) {
          eventBus.publish(new TurnStepPerformed(step))
        }
        if (!gameController.hasNextTurn()) {
          eventBus.publish(new GameFinished)
        }

      case GameRestarted() =>
        eventBus.publish(new NewGameReportChosen(gameReport._1, gameReport._2))

      case ReplayInitiated() | ReplayContinued() =>
        timer match {
          case Some(x) =>
            x.cancel()
          case None =>
        }
        timer = Some(new Timer())
        timer.get.schedule(new ScheduleNextTurn(), 0, turnDelay)

      case ReplayPaused() =>
        timer.get.cancel()
        timer = None

      case TurnDelayChanged(value) =>
        turnDelay = value
        timer match {
          // currently running replay;; update timer to new delay
          case Some(x) =>
            x.cancel()
            timer = Some(new Timer())
            timer.get.schedule(new ScheduleNextTurn(), turnDelay, turnDelay)
          case None =>
        }

      case GameFinished() =>
        timer match {
          case Some(x) => x.cancel()
          case None =>
        }
        timer = None

      case PreviousTurn() =>
        eventBus.publish(GoToTurn(gameController.prevTurnNumber))

      case GoToTurn(turnNo) =>
        val turnState = gameController.getTurnState(turnNo)
        eventBus.publish(GoToTurnState(turnNo, turnState))
        
      case GoToTurnState(number, state) =>
        PlayersList.get().updatePoints(state.players)
        if (number <= 0) 
          eventBus.publish(GameRestarted())
        else {
          gameController.setNextTurnNumber(number)
          val turn = gameController.nextTurn
          for (step <- turn.steps) {
            eventBus.publish(new TurnStepPerformed(step))
          }
          eventBus.publish(UpdatePlayers())
        }
    }

    class ScheduleNextTurn extends TimerTask {
      def run(): Unit = {
        if (gameController.hasNextTurn()) {
          SwingUtilities.invokeAndWait(new Runnable() {
            override def run(): Unit = {
              eventBus.publish(NextTurnInitiated())
            }
          })
        }
      }
    }
    centerOnScreen()
  }
}

class LeftPane extends BorderPanel {
  val playground = new Playground
  val controlPanel = new ControlPanel

  layout(playground) = BorderPanel.Position.Center
  layout(controlPanel) = BorderPanel.Position.South
}

class RightPane extends BoxPanel(Orientation.Horizontal) with Reactor {
  val playersListView = new PlayersListView
  contents += playersListView
}

trait GameStatus
case class GameNotStarted extends GameStatus
case class GameReplaying extends GameStatus
case class GameReplayingPaused extends GameStatus
