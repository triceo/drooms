package org.drooms.gui.swing

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.collection.mutable.Buffer

@RunWith(classOf[JUnitRunner])
class GameReportXmlParserSuite extends FunSuite with BeforeAndAfter {
  var report: GameReport = _

  before {
    report = GameReportXmlParser.parseReport(new File(getClass().getClassLoader().getResource("simple-game-report.xml").getPath()))
  }

  test("game properties are successfully parsed") {
    val props = report.props
    assert(props.size === 19)
    assert(props("worm.max.inactive.turns") === "3")
    assert(props("worm.max.turns") === "1000")
    assert(props("game.class") === "org.drooms.impl.DefaultGame")
    assert(props("collectible.probability.good") === "0.1")

  }

  test("players list is successfully parsed") {
    val players = report.players
    assert(players.size === 8)
    assert(players.contains("nonsuicidal"), "player nonsuicidal is present")
    assert(players.contains("suicidal2"), "player suicidal2 is present")
    assert(players.contains("suicidal6"), "player suicidal6 is present")
  }

  test("playground definition is successfully parsed") {
    val playground = report.playgroundInit
    assert(report.playgroundWidth === 77, "width is correct")
    assert(report.playgroundHeight === 24, "width is correct")
    assert(playground.size === 1716, "number of playground nodes")
    assert(playground.find(node => node.x == 0 && node.y == 0) === Some(Node(0, 0)))
    assert(playground.find(node => node.x == 11 && node.y == 22) === Some(Node(11, 22)))
    assert(playground.find(node => node.x == 76 && node.y == 23) === Some(Node(76, 23)))
    
    assert(playground.find(node => node.x == 23 && node.y == 12) === None)
  }

  test("turns are successfully parsed") {
    val turns = report.turns
    assert(turns.size === 9)
    // verify correct number of events (steps) in turns
    assert(turns(0).steps.size === 8)
    assert(turns(4).steps.size === 15)
    // verify correct events (steps) in turns
    // first step has only WormMoved (playerPosition) steps
    val onlyWormMoved = turns(0).steps.forall(step => 
      step match {
        case WormMoved(_, _) => true
        case _ => false
      })
    assert(onlyWormMoved === true, "First turns should contain only WormMoved events!")
    
    val turn4 = turns(4)
    val turn4CrashedPlayers =  Buffer[String]()
    val turn4NewCollectibles = Buffer[Collectible]()
    turn4.steps.foreach(step => 
      step match {
        case WormCrashed(owner) => 
          turn4CrashedPlayers += owner
        case CollectibleAdded(collectible) =>
          turn4NewCollectibles += collectible
        case _ => 
      })
    // turn 4 contains 3 crashed players
    assert(turn4CrashedPlayers.size === 3)
    assert(turn4CrashedPlayers.toSet === Set("suicidal2", "suicidal3", "suicidal5"))
    // turn 4 contains 2 new collectibles
    assert(turn4NewCollectibles.size === 2)
    assert(turn4NewCollectibles.exists(c => 
      c.node == Node(29, 16) &&
      c.expiresInTurn == 88 &&
      c.points == 1) === true, "NewCollectible not found in turns steps!")
   // TODO test also SurvivedPlayer, RemovedCollectible and CollectedCollectible
  }

  test("final results are successfully parsed") {
      val results = report.results
      assert(results.size === 8)
      // check that players have correct score
      assert(results.find(_._1 == "nonsuicidal") === Some(("nonsuicidal", 8)))
      assert(results.find(_._1 == "suicidal2") === Some(("suicidal2", 3)))
      assert(results.find(_._1 == "suicidal6") === Some(("suicidal6", 3)))
  }
}