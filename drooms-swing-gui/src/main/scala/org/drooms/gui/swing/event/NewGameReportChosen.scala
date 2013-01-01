package org.drooms.gui.swing.event

import scala.swing.event.Event
import org.drooms.gui.swing.GameReport
import java.io.File

case class NewGameReportChosen(val gameReport: GameReport, val file: File) extends Event {

}