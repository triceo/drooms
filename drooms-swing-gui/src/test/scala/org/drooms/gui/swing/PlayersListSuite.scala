package org.drooms.gui.swing

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.awt.Color

@RunWith(classOf[JUnitRunner])
class PlayersListSuite extends FunSuite {
  trait SamplePlayers {
    var playersList = new PlayersList()
    playersList = playersList.addPlayer("Luke")
    playersList = playersList.addPlayer("Obi-Wan")
    playersList = playersList.addPlayer("Vader")
    val origSize = playersList.players.size
  }

  test("newly created PlayersList with no players is empty") {
    val playersList = new PlayersList()
    assert(playersList.players.size === 0)
  }

  test("correct player is returned for given name") {
    new SamplePlayers {
      val luke = playersList.getPlayer("Luke")
      assert(luke.name === "Luke")
    }
  }

  test("player have correctly assigned color") {
    new SamplePlayers {
      playersList = playersList.addPlayer("Lea", Color.PINK)
      val lea = playersList.getPlayer("Lea")
      assert(lea.name === "Lea")
      assert(lea.color === Color.PINK)
      assert(lea.score === 0)
    }
  }

  test("new player is correctly added") {
    new SamplePlayers {
      playersList = playersList.addPlayer("Lea")
      val lea = playersList.getPlayer("Lea")
      assert(playersList.players.size === origSize + 1)
      assert(lea.name === "Lea")
      assert(lea.score === 0)
    }
  }

  test("multiple new players are added") {
      new SamplePlayers {
        playersList = playersList.addPlayers(List("Lea", "Han", "Yoda"))
        assert(playersList.players.size === origSize + 3)
        assert(playersList.getPlayer("Lea") !== null)
        assert(playersList.getPlayer("Han") !== null)
        assert(playersList.getPlayer("Yoda") !== null)
      }
  }

  test("players scores are correctly updated") {
    new SamplePlayers {
      playersList = playersList.updateScores(Map("Luke" -> 10, "Obi-Wan" -> 15, "Vader" -> 20))
      assert(playersList.players.size === 3)
      assert(playersList.getPlayer("Luke").score === 10)
      assert(playersList.getPlayer("Obi-Wan").score === 15)
      assert(playersList.getPlayer("Vader").score === 20)
    }
  }
  
  test("points are correctly added to the player") {
    new SamplePlayers {
      assert(playersList.getPlayer("Luke").score === 0)
      playersList = playersList.addPoints("Luke", 15)
      assert(playersList.getPlayer("Luke").score === 15)
      assert(playersList.addPoints("Luke", 11).getPlayer("Luke").score === 26)
    }
  }
}
