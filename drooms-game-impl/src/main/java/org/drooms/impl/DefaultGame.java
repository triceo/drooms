package org.drooms.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drooms.api.Collectible;
import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.util.GameProperties;
import org.drooms.impl.util.GameProperties.CollectibleType;

/**
 * On top of the rules implemented by {@link GameController}, this game
 * implementation also puts forward some of its own. Those are:
 * 
 * <dl>
 * <dt>Collision detection</dt>
 * <dd>When a worm reaches a node at the same time as another worm, both are terminated. When a worm moves into a body
 * of another worm or into a wall, only this worm is terminated. Specific probabilities and values come from the game
 * config.</dd>
 * <dt>Various types of collectibles</dt>
 * <dd>This class implements three types of collectibles with varying probabilities of appearance, expirations and
 * valuations. There are cheap ones that occur all the time, good ones that occur sometimes and extremely lucrative ones
 * that occur scarcely and don't last long.</dd>
 * <dt>Simultaneos collections</dt>
 * <dd>When two worms collect the same item at the same time, it is considered a collision. Both worms are terminated
 * and neither is awarded points for the item.</dd>
 * <dt>Survival bonuses</dt>
 * <dd>In the turn when at least one worm is removed from the game, either due to crashing or inactivity, every
 * surviving worm is rewarded. The amount of the reward is "the number of worms gone so far" multiplied by the bonus
 * factor.</dd>
 * <dt>Inactivity enforcement</dt>
 * <dd>This implementation will terminate worms for inactivity, as described in the super class.</dd>
 * </dl>
 */
public class DefaultGame extends GameController {

    @Override
    protected Map<Collectible, Player> performCollectibleCollection(final Collection<Player> players) {
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
    protected Collection<Collectible> performCollectibleDistribution(final GameProperties gameConfig,
            final Playground playground, final Collection<Player> players, final int currentTurnNumber) {
        final Set<Collectible> collectibles = new HashSet<Collectible>();
        for (final CollectibleType ct : gameConfig.getCollectibleTypes()) {
            final BigDecimal probability = ct.getProbabilityOfAppearance();
            final BigDecimal chosen = BigDecimal.valueOf(GameController.RANDOM.nextDouble());
            if (probability.compareTo(chosen) > 0) {
                final double expirationAdjustmentRate = GameController.RANDOM.nextDouble() + 0.5;
                final double turnsToLast = expirationAdjustmentRate * ct.getExpiration();
                final int expiresIn = (int) Math.round(currentTurnNumber + turnsToLast);
                final int points = ct.getPoints();
                final Node target = this.pickRandomUnusedNode(playground, players);
                final Collectible c = new Collectible(target, points, expiresIn);
                collectibles.add(c);
            }
        }
        return Collections.unmodifiableSet(collectibles);
    }

    @Override
    protected Set<Player> performCollisionDetection(final Playground playground, final Collection<Player> currentPlayers) {
        final Set<Player> collisions = new HashSet<Player>();
        for (final Player p1 : currentPlayers) {
            final Deque<Node> position = this.getPlayerPosition(p1);
            final Node firstPosition = position.getFirst();
            if (!playground.isAvailable(firstPosition.getX(), firstPosition.getY())) {
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
                final Node secondPosition = this.getPlayerPosition(p2).getFirst();
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
    protected Set<Player> performInactivityDetection(final Collection<Player> currentPlayers,
            final int currentTurnNumber, final int allowedInactiveTurns) {
        final Set<Player> inactiveWorms = new HashSet<Player>();
        if (currentTurnNumber > allowedInactiveTurns) {
            for (final Player p : currentPlayers) {
                final List<Move> allMoves = this.getDecisionRecord(p);
                final int size = allMoves.size();
                final List<Move> relevantMoves = allMoves.subList(Math.max(0, size - allowedInactiveTurns - 1), size);
                if (!relevantMoves.contains(Move.STAY)) {
                    continue;
                }
                final Set<Move> uniqueMoves = new HashSet<Move>(relevantMoves);
                if (uniqueMoves.size() == 1) {
                    inactiveWorms.add(p);
                }
            }
        }
        return Collections.unmodifiableSet(inactiveWorms);
    }

    @Override
    protected Deque<Node> performPlayerMove(final Player player, final Move decision) {
        // move the head of the worm
        final Deque<Node> currentPos = this.getPlayerPosition(player);
        final Node currentHeadPos = currentPos.getFirst();
        Node newHeadPos;
        switch (decision) {
            case UP:
                newHeadPos = Node.getNode(currentHeadPos.getX(), currentHeadPos.getY() + 1);
                break;
            case DOWN:
                newHeadPos = Node.getNode(currentHeadPos.getX(), currentHeadPos.getY() - 1);
                break;
            case LEFT:
                newHeadPos = Node.getNode(currentHeadPos.getX() - 1, currentHeadPos.getY());
                break;
            case RIGHT:
                newHeadPos = Node.getNode(currentHeadPos.getX() + 1, currentHeadPos.getY());
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

    @Override
    protected Map<Player, Integer> performSurvivalRewarding(final Collection<Player> allPlayers,
            final Collection<Player> survivingPlayers, final int removedInThisRound, final int rewardAmount) {
        if (removedInThisRound < 1) {
            return Collections.emptyMap();
        }
        final int amount = rewardAmount * (allPlayers.size() - survivingPlayers.size());
        final Map<Player, Integer> result = new HashMap<>();
        for (final Player p : survivingPlayers) {
            result.put(p, amount);
        }
        return Collections.unmodifiableMap(result);
    }

    private Node pickRandomUnusedNode(final Playground p, final Collection<Player> players) {
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
