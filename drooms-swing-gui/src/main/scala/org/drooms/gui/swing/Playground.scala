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
import org.drooms.gui.swing.event.NewGameLogChosen

class Playground extends ScrollPane with Reactor {
  val CELL_SIZE = 15
  var nodeModel: PlaygroundModel = _
  var table: Table = _

  lazy val wallIcon = createImageIcon("/images/brick-wall-small.png", "Wall")

  reactions += {
    case PlaygroundGridEnabled() => showGrid
    case PlaygroundGridDisabled() => hideGrid
    case NewGameLogChosen(gameLog, file) => {
      createNew(gameLog.playgroundHeight, gameLog.playgroundWidth)
      for (node <- gameLog.playgroundInit)
        nodeModel.updateNode(node)
      nodeModel.gatherWorms()
    }
  }

  def createNew(height: Int, width: Int): Unit = {
    nodeModel = new PlaygroundModel(height, width)
    table = new Table(height, width) {
      val widthPixels = CELL_SIZE * width - 1
      val heightPixels = CELL_SIZE * height - 1
      preferredSize = new Dimension(widthPixels, heightPixels)
      rowHeight = CELL_SIZE
      selection.intervalMode = Table.IntervalMode.Single
      selection.elementMode = Table.ElementMode.Cell
      peer.setTableHeader(null)
      showGrid = false
      peer.setIntercellSpacing(new Dimension(0, 0))

      override def rendererComponent(isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int): Component = {
        val node = nodeModel.nodes(row)(col)
        val cell = node match {
          case Empty(_, _) => new Label("")
          case WormPiece(_, _, wormType, player) => new Label() {
            opaque = true
            background = player.color
            if (wormType == "Head") {
              border = BorderFactory.createLineBorder(Color.BLACK)
            }
          }
          case Wall(_, _) => new Label("") {
            icon = wallIcon
            //border = BorderFactory.createLineBorder(Color.BLACK)
          }
          case Collectible(_, _, _, _) => new Label("B")
        }
        if (isSelected) {
          cell.border = BorderFactory.createLineBorder(Color.black)
        }
        cell
      }
    }
    listenTo(nodeModel)
    reactions += {
      case nodeModel.NodeChanged(node) =>
        table.updateCell(node.row, node.col)
    }

    viewportView = new GridBagPanel {
      layout(table) = new Constraints
    }
  }

  def removeWorm(worm: Worm): Unit = {

  }

  def moveWorm(worm: Worm): Unit = {
    nodeModel.moveWorm(worm)
  }

  def updateNodes(nodes: List[Node]): Unit = {
    for (node <- nodes)
      updateNode(node)
  }

  def updateNode(node: Node) {
    nodeModel.updateNode(node)
  }

  def isGridVisible(): Boolean = {
    table.showGrid
  }

  def hideGrid(): Unit = {
    if (table != null) {
      table.showGrid = false
      table.peer.setIntercellSpacing(new Dimension(0, 0))
    }
  }

  def showGrid(): Unit = {
    if (table != null) {
      table.showGrid = true
      table.peer.setIntercellSpacing(new Dimension(1, 1))
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
