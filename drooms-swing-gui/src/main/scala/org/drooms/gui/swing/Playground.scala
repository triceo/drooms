package org.drooms.gui.swing

import scala.swing.ScrollPane
import scala.swing.Table
import java.awt.Dimension
import scala.swing.GridBagPanel
import scala.swing.BorderPanel

class Playground extends ScrollPane {
  preferredSize = new Dimension(300, 300)
  //maximumSize = new Dimension(300, 300)

  val table = new Table(80, 60) {
    preferredSize = new Dimension(700, 600)
    //maximumSize = new Dimension(40, 40)
    rowHeight = 10
    selection.intervalMode = Table.IntervalMode.Single
    selection.elementMode = Table.ElementMode.Cell
    peer.setTableHeader(null)
    //co
  }

  viewportView = new GridBagPanel {
    layout(table) = new Constraints
  }

}