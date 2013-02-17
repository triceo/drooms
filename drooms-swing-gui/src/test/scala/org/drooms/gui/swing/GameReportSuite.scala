package org.drooms.gui.swing

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GameReportSuite extends FunSuite {

  /* Report is used just for testing purpose, it is not valid game report (semantically) */
  trait SampleReport {
    val players = List("Darth Vader", "Luke")
    val wormsInit = Set[(String, List[Node])](
        (players(0), List(Node(1,1))))
    val nodesInit = 
      (for (i <- 0 until 10; j <- 0 until 10)
        yield Node(i,j)).toSet

    val turns = List[GameTurn](
      new GameTurn(0,
        List(
          new CollectibleAdded(new Collectible(Node(5, 5), 1, 10)),
          new CollectibleAdded(new Collectible(Node(1, 3), 10, 20)),
          new WormMoved(players(0), List(Node(1, 2), Node(1, 1))))),
      new GameTurn(1,
        List(
          new CollectibleRemoved(new Collectible(Node(5,5), 1, 10)),
          new CollectibleCollected(players(0), new Collectible(Node(1,3), 10, 20)),
          new WormMoved(players(0), List(Node(1, 3), Node(1, 2), Node(1,1))))
    ))
    val results = List[(String, Int)]()
    val report = new GameReport(Map(), players, 10, 10, nodesInit, wormsInit, turns, results)
  }

  test("turn states are successfully created") {
    new SampleReport {
      val turnStates = report.createTurnsStates()
      assert(turnStates.size === turns.size + 1)
      // verify fist turn state
      assert(turnStates(1).playgroundModel.getPosition(1, 2) === WormPiece(Node(1,2), "Head", players(0)))
      assert(turnStates(1).playgroundModel.getPosition(1, 1) === WormPiece(Node(1,1), "Tail", players(0)))
      assert(turnStates(1).playgroundModel.getPosition(5, 5) === Collectible(Node(5,5), 1, 10))
      assert(turnStates(1).playgroundModel.getPosition(1, 3) === Collectible(Node(1,3), 10, 20))
      assert(countEmpty(turnStates(1).playgroundModel.positions) === 96)
      assert(turnStates(1).players(players(0)) === 0)
      
      // verify second turn state
      assert(turnStates(2).playgroundModel.getPosition(1, 3) === WormPiece(Node(1,3), "Head", players(0)))
      assert(turnStates(2).playgroundModel.getPosition(1, 2) === WormPiece(Node(1,2), "Body", players(0)))
      assert(turnStates(2).playgroundModel.getPosition(1, 1) === WormPiece(Node(1,1), "Tail", players(0)))
      assert(turnStates(2).playgroundModel.getPosition(5, 5) === Empty(Node(5,5)))
      assert(turnStates(2).players(players(0)) === 20)
      assert(countEmpty(turnStates(2).playgroundModel.positions) === 97)
      
        
      def countEmpty(positions: Array[Array[Position]]): Int = {
        var emptys = 0
        for (i <- 0 until positions(0).size; j <- 0 until positions.size) {
          if (isEmpty(positions(i)(j))) emptys += 1
        }
        emptys
      }
      
      def isEmpty(pos: Position): Boolean = pos == Empty(pos.node)
    }
  }
}