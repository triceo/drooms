package org.drooms.gui.swing

import scala.swing.MenuBar
import scala.swing.Menu
import scala.swing.MenuItem

class MainMenu extends MenuBar {
  contents += new Menu("File") {
    contents += new MenuItem("Load game")
  }
}