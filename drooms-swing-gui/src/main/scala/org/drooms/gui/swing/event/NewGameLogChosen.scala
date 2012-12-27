package org.drooms.gui.swing.event

import scala.swing.event.Event
import org.drooms.gui.swing.GameLog
import java.io.File

case class NewGameLogChosen(val gameLog: GameLog, val file: File) extends Event {

}