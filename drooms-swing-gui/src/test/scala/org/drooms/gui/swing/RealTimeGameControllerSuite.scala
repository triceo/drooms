package org.drooms.gui.swing

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.drooms.impl.DefaultPlayground
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.drooms.impl.util.GameProperties
import java.io.File

@RunWith(classOf[JUnitRunner])
class RealTimeGameControllerSuite extends FunSuite with MockitoSugar {
  trait MockControllerConfiguration {
    val playground = mock[org.drooms.api.Playground]
    val gameProps = mock[GameProperties]
    val players = mock[java.util.List[org.drooms.api.Player]]
  }
  
//  test("dummy controller is created") {
//    val gameConfig = mock[GameConfig]
//    //when(gameConfig.gameProperties).thenReturn(GameProperties.read)
//
//    val controller = RealTimeGameController.createNew(gameConfig)
//    expectResult(classOf[org.drooms.impl.DefaultGame]) { controller.gameClass }
//    expectResult(true) { controller.hasNextTurn }
//    expectResult(false) { controller.isGameFinished }
//    expectResult(false) { controller.reportDir == null }
//  }
//
//  test("sample controller with real config is created") {
//    new MockControllerConfiguration {
//      val gameConfig = GameConfig.createNew(playground, gameProps, players)
//    }
//  }
//
//  test("game is started, restarted and stopped ") {
//
//  }
}