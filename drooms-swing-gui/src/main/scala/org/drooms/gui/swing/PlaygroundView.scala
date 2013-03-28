package org.drooms.gui.swing

import java.awt.Dimension
import java.awt.Font
import scala.swing.Alignment
import scala.swing.Component
import scala.swing.FlowPanel
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.Reactor
import scala.swing.ScrollPane
import scala.swing.Table
import org.drooms.gui.swing.event.CoordinantsVisibilityChanged
import org.drooms.gui.swing.event.EventBusFactory
import org.drooms.gui.swing.event.GoToTurnState
import org.drooms.gui.swing.event.NewGameReportChosen
import org.drooms.gui.swing.event.PlaygroundGridDisabled
import org.drooms.gui.swing.event.PlaygroundGridEnabled
import org.drooms.gui.swing.event.TurnStepPerformed
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.UIManager
import javax.swing.table.DefaultTableModel
import org.drooms.gui.swing.event.NewGameCreated
import org.drooms.gui.swing.event.ReplayInitialized
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Represents the Playground in GUI as {@link ScrollPane}.
 *
 * Playground contains the grid (table) of all nodes and also some additional GUI elements
 * like border from walls or labels for the rows/columns numbers.
 */
class PlaygroundView(var playersList: PlayersList) extends ScrollPane with Reactor with Logging {
  val CELL_SIZE = 15
  val eventBus = EventBusFactory.get()
  var cellModel: PlaygroundModel = _ // TODO use PlaygroundController
  var table: Option[Table] = None
  val worms: collection.mutable.Set[Worm] = collection.mutable.Set()
  var showCoords = false

  def create(report: GameReport): Unit = {
    createNew(report.playgroundWidth, report.playgroundHeight)
    for (node <- report.playgroundInit)
      cellModel.updatePosition(Empty(node))
    initWorms(report.wormInitPositions)
  }
  
  def create(config: NewGameConfig): Unit = {
    // TODO create model with only walls and empty spaces
  }

  listenTo(eventBus)
  reactions += {
    case PlaygroundGridEnabled => showGrid
    case PlaygroundGridDisabled => hideGrid

    case NewGameCreated(config) =>
      createNew(config.getPlaygroundWidth(), config.getPlaygroundHeight())
      for (node <- config.getPlaygroundInit())
        cellModel.updatePosition(Empty(node))

    case GoToTurnState(number, state) =>
      logger.debug(s"Creating new playground table for turn ${number}")
      worms.clear()
      worms ++= state.playgroundModel.worms
      val newModel = state.playgroundModel
      newModel.eventBus = eventBus
      createNew(plwidth, plheight, newModel)
      updateWholeTable()

    case TurnStepPerformed(step) =>
      cellModel.update(step)
  }
  var actualTableWidth: Int = _
  var actualTableHeight: Int = _
  var plwidth: Int = _
  var plheight: Int = _

  def updateWholeTable(): Unit = {
    for (i <- 0 until cellModel.width; j <- 0 until cellModel.height) {
      eventBus.publish(PositionChanged(cellModel.positions(i)(j)))
    }
    table.map(_.repaint())
  }

  def createNew(width: Int, height: Int): Unit = {
    createNew(width, height, new PlaygroundModel(width, height, eventBus))
  }
  
  def createNew(width: Int, height: Int, model: PlaygroundModel): Unit = {
    cellModel = model
    plwidth = width
    plheight = height
    // plus two in each direction (x and y) for border around the playground
    actualTableWidth = width + 2 + 1 // +2 for wall border and +1 for coordinate numbers
    actualTableHeight = height + 2 + 1 // +2 for wall border and +1 for coordinate numbers
    table = Some(new Table(actualTableHeight, actualTableWidth) {
      val widthPixels = CELL_SIZE * actualTableWidth - 1 // minus one so the line at the end is not rendered
      val heightPixels = CELL_SIZE * actualTableHeight - 1 // minus one so the line at the end is not rendered
      preferredSize = new Dimension(widthPixels, heightPixels)
      rowHeight = CELL_SIZE
      selection.intervalMode = Table.IntervalMode.Single
      peer.setTableHeader(null)
      model = new DefaultTableModel(actualTableHeight, actualTableWidth) { // rows == height, cols == width
        override def setValueAt(value: Any, row: Int, col: Int) {
          super.setValueAt(value, row, col)
        }
        override def isCellEditable(row: Int, column: Int) = false
      }
      showGrid = false
      peer.setIntercellSpacing(new Dimension(0, 0))
      lazy val wallIcon = createImageIcon("/images/brick-wall-small.png", "Wall")
      lazy val bonusIcon = createImageIcon("/images/strawberry-icon.png", "Bonus")
      val emptyComponent = new FlowPanel {
        // just empty space
      }
      trait CellType
      case object Blank extends CellType
      case object Border extends CellType
      case object AxisXNumber extends CellType
      case object AxisYNumber extends CellType
      case object PlaygroundCell extends CellType

      // table has (0,0) in left upper corner, model has (0,0) in left down corner -> we need to translate the 
      // coordinates accordingly
      override def rendererComponent(isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int): Component = {
        def determineCellType(row: Int, col: Int): CellType = {
          // numbers on Y axis
          if (col == 0) {
            if (showCoords && row <= actualTableHeight - 3 && row > 0)
              AxisYNumber
            else
              Blank
          } else if (row == actualTableHeight - 1) { // number on X axis
            if (showCoords && col >= 2 && col < actualTableWidth - 1)
              AxisXNumber
            else
              Blank
          } else if (col == 1 || row == 0 || col == actualTableWidth - 1 || row == actualTableHeight - 2) { // border around the playground
            // second column | first row | last column | last row - 1
            Border
          } else {
            PlaygroundCell
          }
        }

        val wallLabel = new Label("") {
          icon = wallIcon
        }
        val cellType = determineCellType(row, col)
        cellType match {
          case Blank => emptyComponent
          case Border => wallLabel
          case AxisYNumber => {
            // number on Y axis
            new Label(actualTableHeight - row - 3 + "") {
              opaque = true
              background = UIManager.getColor("Panel.background")
              font = new Font("Serif", Font.BOLD, 10)

            }
          }
          case AxisXNumber => {
            // number on X axis
            // start numbering from the second col, so the 0,0 points to correct cell
            new Label(col - 2 + "") {
              opaque = true
              background = UIManager.getColor("Panel.background")
              font = new Font("Serif", Font.BOLD, 8)
            }
          }
          case PlaygroundCell => {
            val pos = cellModel.getPosition(col - 2, actualTableHeight - row - 3) // modelRow(Int) or tableRow(Int)
            val cell = pos match {
              case Empty(_) => emptyComponent
              case WormPiece(_, wormType, playerName) => new Label() {
                opaque = true
                background = playersList.getPlayer(playerName).color
                border = BorderFactory.createRaisedBevelBorder()
                if (wormType == "Head") {
                  //border = BorderFactory.createLoweredBevelBorder()
                  text = "\u25CF" // full circle
                }
              }
              case Wall(_) => wallLabel

              case Collectible(_, _, p) => new Label(p + "") {
                opaque = true
                font = new Font("Serif", Font.BOLD, 10)
                icon = bonusIcon
                background = UIManager.getColor("Panel.background")
                verticalTextPosition = Alignment.Center
                horizontalTextPosition = Alignment.Center
              }
            }
            cell.tooltip = (pos.node.x + "," + pos.node.y)
            cell
          }
        }
      }
    })
    reactions += {
      case PositionChanged(position) =>
        // Y-axis numbering in playground model and table is reversed
        // starting from 0 to actualTableHeight -1 and need to subtract the current position and -2 for number and wall down
        table.get.updateCell(actualTableHeight - 1 - position.node.y - 2, position.node.x + 2) // y == row and x == col
      case CoordinantsVisibilityChanged(value) => {
        showCoords = value
        // update the table, so the headers are painted
        table match {
          case Some(table) => {
            for (i <- 0 until actualTableWidth)
              table.updateCell(actualTableHeight - 1, i)
            for (j <- 0 until actualTableHeight)
              table.updateCell(j, 0)
          }
          case None =>
        }
      }
    }

    viewportView = new GridBagPanel {
      layout(table.get) = new Constraints
    }
  }

  def updatePositions(positions: List[Position]): Unit = {
    cellModel.updatePositions(positions)
  }

  def updatePosition(pos: Position) {
    cellModel.updatePosition(pos)
  }

  /** Initialize worms from specified list of pairs 'ownerName' -> 'list of Nodes' */
  def initWorms(wormsInit: Set[(String, List[Node])]): Unit = {
    cellModel.initWorms(wormsInit)
  }

  def isGridVisible(): Boolean = {
    table.get.showGrid
  }

  def hideGrid(): Unit = {
    setGridVisibility(false)
  }

  def showGrid(): Unit = {
    setGridVisibility(true)
  }

  private def setGridVisibility(visible: Boolean): Unit = {
    table match {
      case Some(table) =>
        table.showGrid = visible
        if (visible)
          table.peer.setIntercellSpacing(new Dimension(1, 1))
        else
          table.peer.setIntercellSpacing(new Dimension(0, 0))
      case None =>
    }
  }

  private def createImageIcon(path: String, description: String): ImageIcon = {
    val imgUrl = getClass().getResource(path)
    if (imgUrl != null) {
      new ImageIcon(imgUrl, description)
    } else {
      throw new RuntimeException("Could not find image file " + path)
    }
  }
}
