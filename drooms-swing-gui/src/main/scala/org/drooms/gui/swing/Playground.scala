package org.drooms.gui.swing

import scala.swing.ScrollPane
import scala.swing.Table

class Playground extends ScrollPane {
  val table = new Table(10, 10) {
    rowHeight = 10
    selection.intervalMode = Table.IntervalMode.Single
    selection.elementMode = Table.ElementMode.Cell
    //co
  }

  viewportView = table

}