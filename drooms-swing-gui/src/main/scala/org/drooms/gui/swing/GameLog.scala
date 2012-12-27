package org.drooms.gui.swing

import java.io.File
import java.awt.Color
import scala.io.Source
import scala.xml.XML
import scala.xml.NodeSeq

// TODO add game results
class GameLog(
  val gameId: String,
  val props: Map[String, String],
  val players: List[Player],
  val playgroundHeight: Int,
  val playgroundWidth: Int,
  val playgroundInit: List[Node],
  val turns: List[GameTurn])

object GameLog {
  def loadFromXml(file: File): GameLog = {
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
        yield new Player((player \ "@name").text, Color.CYAN)).toList
    // parse playground
    val playgroundXml = xml \ "playground"
    val nodes: List[Node] =
      (for (node <- playgroundXml \ "node")
        yield new Empty((node \ "@y").text.toInt, (node \ "@x").text.toInt)).toList

    val playgroundHeight = nodes.maxBy(_.row).row + 1 // numbering starts from 0 -> need to increment the max row
    val playgroundWidth = nodes.maxBy(_.col).col + 1 // numbering starts from 0 -> need to increment the max column

    // parse turns
    val turnsXml = xml \ "turns"

    def parseTurn(turnXml: NodeSeq): GameTurn = {
      val number = (turnXml \ "@number").text.toInt
      val wormsMoved =
        (for (playerPos <- turnXml \ "playerPosition")
          yield parsePlayerPosition(playerPos)).toList
      val newCollectibles =
        (for (newCollectible <- turnXml \ "newCollectible")
          yield parseNewCollectible(newCollectible)).toList
      new GameTurn(number, wormsMoved ::: newCollectibles)
    }

    def parsePlayerPosition(playerPosXml: NodeSeq): WormMoved = {
      val player = getPlayer((playerPosXml \ "player" \ "@name").text)
      val pieces =
        (for (node <- playerPosXml \ "node")
          yield new WormPiece((node \ "@y").text.toInt, (node \ "@x").text.toInt, "Worm", player)).toSet
      new WormMoved(new Worm(player, pieces))
    }

    def parseNewCollectible(newCollectibleXml: NodeSeq): CollectibeAdded = {
      val expires = (newCollectibleXml \ "collectible" \ "@expiresInTurn").text.toInt
      val points = (newCollectibleXml \ "collectible" \ "@points").text.toInt
      val row = (newCollectibleXml \ "node" \ "@y").text.toInt
      val col = (newCollectibleXml \ "node" \ "@x").text.toInt
      new CollectibeAdded(new Collectible(row, col, expires, points))
    }

    def getPlayer(playerName: String): Player = {
      players.find(_.name == playerName) match {
        case Some(player) => player
        case None => throw new RuntimeException("Player not found " + playerName)
      }
    }
    val turns =
      (for (turn <- turnsXml \ "turn") yield parseTurn(turn)).toList
    // parse final results

    new GameLog(
      "",
      props,
      players.sortBy(_.name),
      playgroundHeight,
      playgroundWidth,
      nodes,
      turns)
  }
}