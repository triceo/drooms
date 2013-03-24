package org.drooms.gui.swing

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File

@RunWith(classOf[JUnitRunner])
class ReplayGameControllerSuite extends FunSuite {
   trait SampleReplayer {
     val report = GameReportXmlParser.parseReport(new File(getClass().getClassLoader().getResource("report-for-game-replayer.xml").getPath()))
     val gameReplayer = new ReplayGameController(report)
   }
   
   test("Simplest replay controller can be instantiated") {
       new ReplayGameController(new GameReport(Map(), List(), 10, 10, Set(), Set(), List(), List()))
   }
   
   test("next turns can be performed") {
     new SampleReplayer {
       var nextTurnNumber = 1
       assert(gameReplayer.nextTurnNumber === nextTurnNumber)
       while(gameReplayer.hasNextTurn()) {
         val turn = gameReplayer.getNextTurn()
        nextTurnNumber += 1
        assert(gameReplayer.nextTurnNumber === nextTurnNumber)
       }
       // verify that all turns has been performed
       assert(nextTurnNumber - 1 === 8, "Not all turns has been performed!")
     }
   }
}