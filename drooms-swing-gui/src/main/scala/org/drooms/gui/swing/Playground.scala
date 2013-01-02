package org.drooms.gui.swing

import java.awt.Color
import java.awt.Dimension
import scala.swing.Component
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.Reactor
import scala.swing.ScrollPane
import scala.swing.Table
import org.drooms.gui.swing.event.PlaygroundGridDisabled
import org.drooms.gui.swing.event.PlaygroundGridEnabled
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import org.drooms.gui.swing.event.NewGameReportChosen
import java.awt.Font
import org.drooms.gui.swing.event.TurnStepPerformed
import org.drooms.gui.swing.event.DroomsEventPublisher
import javax.swing.table.DefaultTableModel
import org.drooms.gui.swing.event.PlaygroundGridEnabled
import scala.swing.Alignment

class Playground extends ScrollPane with Reactor {
  val CELL_SIZE = 15
  val eventPublisher = DroomsEventPublisher.get()
  var cellModel: PlaygroundModel = _
  var table: Option[Table] = None
  val worms: collection.mutable.Set[Worm] = collection.mutable.Set()

  listenTo(eventPublisher)
  reactions += {
    case PlaygroundGridEnabled() => showGrid
    case PlaygroundGridDisabled() => hideGrid
    case NewGameReportChosen(gameReport, file) => {
      createNew(gameReport.playgroundWidth, gameReport.playgroundHeight)
      for (node <- gameReport.playgroundInit)
        cellModel.updatePosition(Empty(node))
      initWorms(gameReport.wormInitPositions)
    }
    case TurnStepPerformed(step) =>
      step match {
        case WormMoved(ownerName, nodes) =>
          moveWorm(ownerName, nodes)
        case WormCrashed(ownerName) =>
          removeWorm(ownerName)
        case WormDeactivated(ownerName) =>
          removeWorm(ownerName)
        case CollectibleAdded(collectible) =>
          updatePosition(collectible)
        case CollectibleRemoved(collectible) =>
          updatePosition(Empty(collectible.node))
        case CollectibleCollected(player, collectible) =>
        case _ => new RuntimeException("Unrecognized TurnStep: " + step)
      }
  }

  def createNew(width: Int, height: Int): Unit = {
      cellModel = new PlaygroundModel(width, height)
    // plus two in each direction (x and y) for border around the playground
    val actualTableWidth = width + 2
    val actualTableHeight = height + 2
    table = Some(new Table(actualTableWidth, actualTableHeight) {
      val widthPixels = CELL_SIZE * actualTableWidth - 1   // minus one so the line at the end is not rendered
      val heightPixels = CELL_SIZE * actualTableHeight - 1 // minus one so the line at the end is not rendered
      preferredSize = new Dimension(widthPixels, heightPixels)
      rowHeight = CELL_SIZE
      selection.intervalMode = Table.IntervalMode.Single
      selection.elementMode = Table.ElementMode.None
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

      override def rendererComponent(isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int): Component = {
        val wallLabel = new Label("") {
          icon = wallIcon
        }
        // border around playground
        if (col == 0 || row == 0 || col > width || row > height) {
          wallLabel
        } else {
          val node = cellModel.positions(col - 1)(row - 1)
          val cell = node match {
            case Empty(_) => new Label() {
              //icon = emptyIcon
            }
            case WormPiece(_, wormType, playerName) => new Label() {
              opaque = true
              background = PlayersList.get().getPlayer(playerName).color
              border = BorderFactory.createRaisedBevelBorder()
              if (wormType == "Head") {
                //border = BorderFactory.createLoweredBevelBorder()
                text = "\u25CF"
              }
            }
            case Wall(_) => wallLabel

            case Collectible(_, _, p) => new Label(p + "") {
              font = new Font("Serif", Font.BOLD, 10)
              icon = bonusIcon
              verticalTextPosition = Alignment.Center
              horizontalTextPosition = Alignment.Center
            }
          }
          if (isSelected) {
            cell.border = BorderFactory.createLineBorder(Color.black)
          }
          cell
        }
      }
    })
    reactions += {
      case PositionChanged(position) =>
        table.get.updateCell(position.node.y + 1, position.node.x + 1) // y == row and x == col
    }

    viewportView = new GridBagPanel {
      layout(table.get) = new Constraints
    }
  }

  def updatePositions(positions: List[Position]): Unit = {
    for (pos <- positions)
      updatePosition(pos)
  }

  def updatePosition(pos: Position) {
    cellModel.updatePosition(pos)
  }

  /** Initialize worms from specified list of pairs 'ownerName' -> 'list of Nodes' */
  def initWorms(wormsInit: Set[(String, List[Node])]): Unit = {
    worms.clear()
    for ((name, nodes) <- wormsInit) {
      worms.add(Worm(name, (for (node <- nodes) yield WormPiece(node, "Head", name)).toList))
    }
    // update model
    for (worm <- worms) updatePositions(worm.pieces)
  }

  /** Moves the worm to the new position */
  def moveWorm(ownerName: String, nodes: List[Node]): Unit = {
    this.synchronized {
      // removes current worm pieces
      removeWormPieces(ownerName)
      // worm must have at least head
      val head = nodes.head
      updateWormIfLegal(head, ownerName, "Head")

      if (nodes.size > 2) {
        for (node <- nodes.tail.init) {
          updateWormIfLegal(node, ownerName, "Body")
        }
      }

      if (nodes.size > 1) {
        val tail = nodes.last
        updateWormIfLegal(tail, ownerName, "Tail")
      }

      /**
       * Updates the wom only if the underlaying node is empty or collectible == eligible to be occupied by current worm
       */
      def updateWormIfLegal(node: Node, ownerName: String, wormType: String): Unit = {
        // we can only update Empty nodes and Collectibles, if the worm crashed into wall or other worm piece must not be updated!
        cellModel.positions(node.x)(node.y) match {
          case Empty(node) =>
            updateWorm(ownerName, new WormPiece(node, wormType, ownerName))
          case Collectible(node, _, _) =>
            updateWorm(ownerName, new WormPiece(node, wormType, ownerName))
          case _ =>
        }
      }
    }
  }

  def updateWorm(ownerName: String, piece: WormPiece) = {
    getWorm(ownerName).addPiece(piece)
    updatePosition(piece)
  }

  def getWorm(ownerName: String): Worm = {
    worms.find(_.ownerName == ownerName) match {
      case Some(worm) => worm
      case None => throw new RuntimeException("Can't update non existing worm! Owner=" + ownerName)
    }
  }

  /**
   * Removes the worm from the list of worms and also makes sure that all worm pieces are removed from playground
   */
  def removeWorm(ownerName: String): Unit = {
    val worm = getWorm(ownerName)
    removeWormPieces(ownerName)
    worms.remove(worm)
  }

  def removeWormPieces(ownerName: String): Unit = {
    worms.find(_.ownerName == ownerName) match {
      case Some(worm) =>
        for (piece <- worm.pieces) {
          cellModel.positions(piece.node.x)(piece.node.y) match {
            case WormPiece(node, t, owner) =>
              if (piece.playerName == owner) {
                updatePosition(Empty(node))
              }
            case _ =>
          }
          worm.pieces = List()
        }
      case None =>
    }
  }

  def isGridVisible(): Boolean = {
    table.get.showGrid
  }

  def hideGrid(): Unit = {
    table match {
      case Some(table) =>
        table.showGrid = false
        table.peer.setIntercellSpacing(new Dimension(0, 0))
      case None =>
    }
  }

  def showGrid(): Unit = {
    table match {
      case Some(table) =>
        table.showGrid = true
        table.peer.setIntercellSpacing(new Dimension(1, 1))
      case None =>
    }
  }

  def createImageIcon(path: String, description: String): ImageIcon = {
    val imgUrl = getClass().getResource(path)
    if (imgUrl != null) {
      new ImageIcon(imgUrl, description)
    } else {
      throw new RuntimeException("Could not find image file " + path)
    }
  }
}
