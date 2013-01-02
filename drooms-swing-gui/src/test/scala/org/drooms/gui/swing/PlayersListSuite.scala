package org.drooms.gui.swing

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.awt.Color

@RunWith(classOf[JUnitRunner])
class PlayersListSuite extends FunSuite {
  trait SamplePlayers {
    val playersList = new PlayersList()
    playersList.addPlayer("Luke")
    playersList.addPlayer("Obi-Wan")
    playersList.addPlayer("Vader")
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
      playersList.addPlayer("Lea", Color.PINK)
      val lea = playersList.getPlayer("Lea")
      assert(lea.name === "Lea")
      assert(lea.color === Color.PINK)
      assert(lea.score === 0)
    }
  }

  test("new player is correctly added") {
    new SamplePlayers {
      playersList.addPlayer("Lea")
      val lea = playersList.getPlayer("Lea")
      assert(playersList.players.size === origSize + 1)
      assert(lea.name === "Lea")
      assert(lea.score === 0)
    }
  }

  test("multiple new players are added") {
      new SamplePlayers {
        playersList.addPlayers(List("Lea", "Han", "Yoda"))
        assert(playersList.players.size === origSize + 3) 
      }
  }

  test("non-empty players list is properly cleared when requested") {
    new SamplePlayers {
      assert(playersList.players.size != 0)
      playersList.clear()
      assert(playersList.players.size === 0)
    }
  }
}