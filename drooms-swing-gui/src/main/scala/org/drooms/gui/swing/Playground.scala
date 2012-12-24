package org.drooms.gui.swing

import scala.swing.ScrollPane
import scala.swing.Table
import java.awt.Dimension
import scala.swing.GridBagPanel
import scala.swing.BorderPanel
import scala.swing.Component
import scala.swing.Label
import javax.swing.table.TableModel
import scala.swing.Button
import java.awt.Color
import javax.swing.table.TableCellRenderer
import javax.swing.JTable
import javax.swing.BorderFactory
import javax.swing.ImageIcon

class Playground(val height: Int, val width: Int) extends ScrollPane {
  val CELL_SIZE = 15
  val nodeModel = new PlaygroundModel(height, width)

  val wallIcon = createImageIcon("/images/brick-wall-small.png", "Wall")

  val table = new Table(height, width) {
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
      import nodeModel._
      val node = nodeModel.nodes(row)(col)
      val cell = node match {
        case Empty(_, _) => new Label("")
        case Worm(_, _, wormType, player) => new Label() {
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
        case Collectible(_, _, _) => new Label("B")
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

  def updateNode(node: Node) {
    nodeModel.updateNode(node)
  }

  def isGridVisible(): Boolean = {
    table.showGrid
  }

  def hideGrid(): Unit = {
    table.showGrid = false
    table.peer.setIntercellSpacing(new Dimension(0, 0))
  }

  def showGrid(): Unit = {
    table.showGrid = true
    table.peer.setIntercellSpacing(new Dimension(1, 1))
  }

  def createImageIcon(path: String, description: String): ImageIcon = {
    val imgUrl = getClass().getResource(path)
    if (imgUrl != null) {
      new ImageIcon(imgUrl, description)
    } else {
      throw new RuntimeException("Could not find image file " + path)
    }
  }

  viewportView = new GridBagPanel {
    layout(table) = new Constraints
  }

  //  def paintWorm(worm: Worm): Unit = {
  //    
  //  }

}