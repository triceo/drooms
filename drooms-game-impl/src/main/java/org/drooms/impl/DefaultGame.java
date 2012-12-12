package org.drooms.impl;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.drooms.api.Collectible;
import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.collectibles.CheapCollectible;
import org.drooms.impl.collectibles.ExtremeCollectible;
import org.drooms.impl.collectibles.GoodCollectible;

public class DefaultGame extends GameController {

    public DefaultGame(final File reportFolder, final String gameId) {
        super(reportFolder, gameId);
    }

    @Override
    protected Map<Collectible, Player> performCollectibleCollection(
            final Collection<Player> players) {
        final Map<Collectible, Player> collections = new HashMap<Collectible, Player>();
        for (final Player p : players) {
            final Node headPosition = this.getPlayerPosition(p).getFirst();
            final Collectible c = this.getCollectible(headPosition);
            if (c != null) { // successfully collected
                collections.put(c, p);
            }
        }
        return Collections.unmodifiableMap(collections);
    }

    @Override
    protected Map<Collectible, Node> performCollectibleDistribution(
            final Properties gameConfig, final DefaultPlayground playground,
            final Collection<Player> players, final int currentTurnNumber) {
        final Map<Collectible, Node> collectibles = new HashMap<Collectible, Node>();
        for (final CollectibleType ct : CollectibleType.values()) {
            final BigDecimal probability = ct
                    .getProbabilityOfAppearance(gameConfig);
            final BigDecimal chosen = BigDecimal.valueOf(GameController.RANDOM
                    .nextDouble());
            if (probability.compareTo(chosen) > 0) {
                final int expiration = currentTurnNumber
                        + ct.getExpiration(gameConfig);
                final int points = ct.getPoints(gameConfig);
                Collectible c = null;
                switch (ct) {
                    case CHEAP:
                        c = new CheapCollectible(points, expiration);
                        break;
                    case GOOD:
                        c = new GoodCollectible(points, expiration);
                        break;
                    case EXTREME:
                        c = new ExtremeCollectible(points, expiration);
                        break;
                    default:
                        throw new IllegalStateException(
                                "Unknown collectible type!");
                }
                collectibles.put(c,
                        this.pickRandomUnusedNode(playground, players));
            }
        }
        return Collections.unmodifiableMap(collectibles);
    }

    @Override
    protected Set<Player> performCollisionDetection(
            final DefaultPlayground playground,
            final Collection<Player> currentPlayers) {
        final Set<Player> collisions = new HashSet<Player>();
        for (final Player p1 : currentPlayers) {
            final Deque<Node> position = this.getPlayerPosition(p1);
            final Node firstPosition = position.getFirst();
            if (!playground.isAvailable(firstPosition.getX(),
                    firstPosition.getY())) {
                collisions.add(p1);
                continue;
            } else {
                // make sure the worm didn't crash into itself
                final Set<Node> nodes = new HashSet<Node>(position);
                if (nodes.size() < position.size()) {
                    // a worm occupies one node twice = a crash into itself
                    collisions.add(p1);
                }
            }
            for (final Player p2 : currentPlayers) {
                if (p1 == p2) {
                    // the same worm
                    continue;
                }
                final Node secondPosition = this.getPlayerPosition(p2)
                        .getFirst();
                if (firstPosition.equals(secondPosition)) {
                    // head-on-head collision
                    collisions.add(p1);
                    collisions.add(p2);
                } else if (position.contains(secondPosition)) {
                    // head-on-body collision
                    collisions.add(p2);
                }
            }
        }
        return Collections.unmodifiableSet(collisions);
    }

    @Override
    protected Set<Player> performInactivityDetection(
            final Collection<Player> currentPlayers,
            final int currentTurnNumber, final int allowedInactiveTurns) {
        final Set<Player> inactiveWorms = new HashSet<Player>();
        if (currentTurnNumber > allowedInactiveTurns) {
            for (final Player p : currentPlayers) {
                final Move[] moves = this.getDecisionRecord(p).toArray(
                        new Move[] {});
                boolean active = false;
                for (int i = moves.length - allowedInactiveTurns - 1; i < moves.length; i++) {
                    if (moves[i] != Move.STAY) {
                        // the worm has been active
                        active = true;
                        break;
                    }
                }
                if (!active) {
                    inactiveWorms.add(p);
                }
            }
        }
        return Collections.unmodifiableSet(inactiveWorms);
    }

    @Override
    protected Deque<Node> performPlayerMove(final Player player,
            final Move decision) {
        // move the head of the worm
        final Deque<Node> currentPos = this.getPlayerPosition(player);
        final Node currentHeadPos = currentPos.getFirst();
        Node newHeadPos;
        switch (decision) {
            case UP:
                newHeadPos = Node.getNode(currentHeadPos.getX(),
                        currentHeadPos.getY() + 1);
                break;
            case DOWN:
                newHeadPos = Node.getNode(currentHeadPos.getX(),
                        currentHeadPos.getY() - 1);
                break;
            case LEFT:
                newHeadPos = Node.getNode(currentHeadPos.getX() - 1,
                        currentHeadPos.getY());
                break;
            case RIGHT:
                newHeadPos = Node.getNode(currentHeadPos.getX() + 1,
                        currentHeadPos.getY());
                break;
            case STAY:
                newHeadPos = currentHeadPos;
                break;
            default:
                throw new IllegalStateException("Unknown move!");
        }
        // move the head of the snake
        final Deque<Node> newPosition = new LinkedList<Node>(currentPos);
        if (decision != Move.STAY) {
            newPosition.push(newHeadPos);
        }
        // make sure the snake is as long as it should be
        while (newPosition.size() > this.getPlayerLength(player)) {
            newPosition.removeLast();
        }
        // notify
        return newPosition;
    }

    private Node pickRandomUnusedNode(final DefaultPlayground p,
            final Collection<Player> players) {
        final List<Node> nodes = new LinkedList<Node>();
        // locate available nodes
        for (int x = 0; x < p.getWidth(); x++) {
            for (int y = 0; y < p.getHeight(); y++) {
                if (p.isAvailable(x, y)) {
                    nodes.add(Node.getNode(x, y));
                }
            }
        }
        // exclude nodes where worms are
        for (final Player player : players) {
            nodes.removeAll(this.getPlayerPosition(player));
        }
        // exclude nodes where collectibles are
        final List<Node> nodesCopy = new LinkedList<Node>(nodes);
        for (final Node n : nodesCopy) {
            if (this.getCollectible(n) != null) {
                nodes.remove(n);
            }
        }
        if (nodes.size() == 0) {
            return null;
        } else {
            return nodes.get(GameController.RANDOM.nextInt(nodes.size()));
        }
    }
}
