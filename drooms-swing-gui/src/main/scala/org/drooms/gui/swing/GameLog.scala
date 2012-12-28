package org.drooms.gui.swing

import java.io.File
import java.awt.Color
import scala.io.Source
import scala.xml.XML
import scala.xml.NodeSeq

class GameLog(
  val props: Map[String, String],
  val players: List[String],
  val playgroundHeight: Int,
  val playgroundWidth: Int,
  val playgroundInit: Set[Node],
  val wormInitPositions: Set[(String, List[Node])],
  val turns: List[GameTurn],
  val results: List[(String, Int)])

object GameLog {
  def loadFromXml(file: File): GameLog = {
    // parse game ID
    // parse game properties
    val xml = XML.loadFile(file)
    val configXml = xml \ "config"
    val props =
      (for (prop <- (configXml \ "property"))
        yield ((prop \ "@name").text) -> ((prop \ "@value").text)).toMap
    // parse players list
    val playersXml = xml \ "players"
    val players =
      (for (player <- (playersXml \ "player"))
        yield (player \ "@name").text).toList
    // parse playground
    val playgroundXml = xml \ "playground"
    val nodes =
      (for (node <- playgroundXml \ "node")
        yield Node((node \ "@x").text.toInt, (node \ "@y").text.toInt)).toSet
    val playgroundWidth = nodes.maxBy(_.x).x + 1 // numbering starts from 0 -> need to increment the max x
    val playgroundHeight = nodes.maxBy(_.y).y + 1 // numbering starts from 0 -> need to increment the max y
    // parse turns
    val turnsXml = xml \ "turns"
    val turns =
      (for (turn <- turnsXml \ "turn") yield parseTurn(turn)).toList
    // parse worm initial positions from first(0-th) turn
    val initPositions: collection.mutable.Set[(String, List[Node])] = collection.mutable.Set()
    for (turn <- turns(0).steps) {
      turn match {
        case WormMoved(owner, nodes) =>
          initPositions.add(owner, nodes)
        case _ =>
      }
    }

    // parse final results
    val results =
      (for (result <- xml \ "results" \ "score")
        yield (parsePlayerName(result), (result \ "@points").text.toInt)).toList

    new GameLog(
      props,
      players.sorted,
      playgroundHeight,
      playgroundWidth,
      nodes,
      initPositions.toSet,
      turns,
      results)
  }

  ////////////////////// Helper methods for parsing XML report ////////////////
  private def parseTurn(turnXml: NodeSeq): GameTurn = {
    val number = (turnXml \ "@number").text.toInt
    // worms moved (player positions)
    val wormsMoved =
      (for (playerPos <- turnXml \ "playerPosition")
        yield new WormMoved(parsePlayerName(playerPos), parsePlayerPosition(playerPos))).toList
    // new collectibles
    val newCollectibles =
      (for (newCollectible <- turnXml \ "newCollectible")
        yield new CollectibleAdded(parseCollectible(newCollectible))).toList
    // removed colletibles
    val removedCollectibles =
      (for (removedCollectible <- turnXml \ "removedCollectible")
        yield new CollectibleRemoved(parseCollectible(removedCollectible))).toList
    // crashed worms
    val crashedWorms =
      (for (crashedWorm <- turnXml \ "crashedPlayer")
        yield new WormCrashed(parsePlayerName(crashedWorm))).toList
    // deactivated worms
    val deactivatedWorms =
      (for (deactivatedWorm <- turnXml \ "deactivatedPlayer")
        yield new WormDeactivated(parsePlayerName(deactivatedWorm))).toList
    // survived worms
    val survivedWorms =
      (for (survivedWorm <- turnXml \ "survivedPlayer")
        yield new WormSurvived(parsePlayerName(survivedWorm), (survivedWorm \ "@points").text.toInt)).toList
    new GameTurn(number, wormsMoved ::: newCollectibles ::: removedCollectibles ::: crashedWorms ::: deactivatedWorms)
  }

  private def parsePlayerName(xml: NodeSeq): String = (xml \ "player" \ "@name").text

  def parsePlayerPosition(playerPosXml: NodeSeq): List[Node] = {
    (for (node <- playerPosXml \ "node")
      yield Node((node \ "@x").text.toInt, (node \ "@y").text.toInt)).toList
  }

  private def parseCollectible(collectibleXml: NodeSeq): Collectible = {
    val expires = (collectibleXml \ "collectible" \ "@expiresInTurn").text.toInt
    val points = (collectibleXml \ "collectible" \ "@points").text.toInt
    new Collectible(parseNode(collectibleXml), expires, points)
  }

  private def parseNode(xml: NodeSeq): Node = {
    val x = (xml \ "node" \ "@x").text.toInt
    val y = (xml \ "node" \ "@y").text.toInt
    new Node(x, y)
  }
}