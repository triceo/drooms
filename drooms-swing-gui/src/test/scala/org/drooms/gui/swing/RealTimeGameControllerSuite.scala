package org.drooms.gui.swing

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.drooms.impl.DefaultPlayground
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.drooms.impl.util.properties.GameProperties
import java.io.File
import org.drooms.impl.DroomsGame

@RunWith(classOf[JUnitRunner])
class RealTimeGameControllerSuite extends FunSuite with MockitoSugar {
  trait MockControllerConfiguration {
    val playground = mock[org.drooms.api.Playground]
    val gameProps = mock[GameProperties]
    val players = mock[java.util.List[org.drooms.api.Player]]
  }
  
  
  trait MockDroomsGame {
    val droomsGame = mock[DroomsGame]
    //when(droomsGame.play("Mock game")).then()
  }

  test("dummy controller is created") {
    val gameConfig = mock[NewGameConfig]
    //when(gameConfig.gameProperties).thenReturn(GameProperties.read)

    val controller = RealTimeGameController.createNew(gameConfig)
    expect(classOf[org.drooms.impl.DefaultGame]) { controller.gameClass }
    expect(true) { controller.hasNextTurn }
    expect(false) { controller.isGameFinished }
    expect(false) { controller.reportDir == null }
  }

  test("sample controller with real config is created") {
    new MockControllerConfiguration {
      val gameConfig = NewGameConfig.createNew(playground, gameProps, players)
    }
  }

  test("game is started, restarted and stopped ") {

  }
}