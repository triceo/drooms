package org.drooms.impl;

import org.drooms.api.*;
import org.drooms.api.Node.Type;
import org.drooms.impl.util.Detectors;
import org.drooms.impl.util.GameProperties;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
 * <dt>Simultaneous collections</dt>
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
        final Map<Collectible, Player> collections = new HashMap<>();
        for (final Player p : players) {
            final Node headPosition = this.getPlayerPosition(p).getHeadNode();
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
        return Collections.unmodifiableSet(gameConfig.getCollectibleTypes().stream().filter(ct -> {
            final BigDecimal probability = ct.getProbabilityOfAppearance();
            final BigDecimal chosen = BigDecimal.valueOf(GameController.RANDOM.nextDouble());
            return (probability.compareTo(chosen) > 0);
        }).map(ct -> {
            final double expirationAdjustmentRate = GameController.RANDOM.nextDouble() + 0.5;
            final double turnsToLast = expirationAdjustmentRate * ct.getExpiration();
            final int expiresIn = (int) Math.round(currentTurnNumber + turnsToLast);
            final int points = ct.getPoints();
            final Node target = this.pickRandomUnusedNode(playground, players);
            return new Collectible(target, points, expiresIn);
        }).collect(Collectors.toSet()));
    }

    @Override
    protected Set<Player> performCollisionDetection(final Playground playground,
                                                    final Collection<Player> currentPlayers) {
        return Detectors.detectCollision(currentPlayers.stream().map(this::getPlayerPosition).collect(
                Collectors.toSet())).stream().map(PlayerPosition::getPlayer).collect(Collectors.toSet());
    }

    @Override
    protected Set<Player> performInactivityDetection(final Collection<Player> currentPlayers,
                                                     final int currentTurnNumber, final int allowedInactiveTurns) {
        return Detectors.detectInactivity(allowedInactiveTurns, currentPlayers.stream().collect(
                Collectors.toMap(Function.identity(), this::getDecisionRecord)));
    }

    @Override
    protected PlayerPosition performPlayerAction(final PlayerPosition currentPos, final Action decision) {
        // move the head of the worm
        final Node currentHeadPos = currentPos.getHeadNode();
        final Playground playground = currentPos.getPlayground();
        Node newHeadPos;
        switch (decision) {
            case REVERSE: // reverse the snake and do nothing else
                return currentPos.reverse();
            case MOVE_UP:
                newHeadPos = playground.getNodeAt(currentHeadPos.getX(), currentHeadPos.getY() + 1);
                break;
            case MOVE_DOWN:
                newHeadPos = playground.getNodeAt(currentHeadPos.getX(), currentHeadPos.getY() - 1);
                break;
            case MOVE_LEFT:
                newHeadPos = playground.getNodeAt(currentHeadPos.getX() - 1, currentHeadPos.getY());
                break;
            case MOVE_RIGHT:
                newHeadPos = playground.getNodeAt(currentHeadPos.getX() + 1, currentHeadPos.getY());
                break;
            case ENTER:
                if (currentHeadPos.getType() == Type.PORTAL) {
                    // the current node is a PORTAL. move to the other end
                    newHeadPos = playground.getOtherEndOfPortal(currentHeadPos);
                    break;
                }
                // else this command makes no sense and we STAY
                // FIXME this is a problem for inactivity detection
            case NOTHING:
                // do not modify the worm in any way
                return currentPos;
            default:
                throw new IllegalStateException("Unknown action!");
        }
        // just to be sure; in theory can never happen as you first always hit a wall
        if (newHeadPos == null) {
            throw new IllegalStateException("Moving to a non-existent node!");
        }
        // move the head of the snake
        if (newHeadPos != currentHeadPos) {
            final PlayerPosition newPosition = currentPos.newHead(newHeadPos);
            return newPosition.ensureMaxLength(this.getPlayerLength(currentPos.getPlayer()));
        } else {
            return currentPos.ensureMaxLength(this.getPlayerLength(currentPos.getPlayer()));
        }
    }

    @Override
    protected Map<Player, Integer> performSurvivalRewarding(final Collection<Player> allPlayers,
            final Collection<Player> survivingPlayers, final int removedInThisRound, final int rewardAmount) {
        if (removedInThisRound < 1) {
            return Collections.emptyMap();
        }
        final int amount = rewardAmount * (allPlayers.size() - survivingPlayers.size());
        return Collections.unmodifiableMap(survivingPlayers.stream().collect(Collectors.toMap(player -> player,
                player -> amount)));
    }

    private Node pickRandomUnusedNode(final Playground p, final Collection<Player> players) {
        final List<Node> nodes = new LinkedList<>();
        // locate available nodes
        for (int x = 0; x < p.getWidth(); x++) {
            for (int y = 0; y < p.getHeight(); y++) {
                if (p.isAvailable(x, y)) {
                    nodes.add(p.getNodeAt(x, y));
                }
            }
        }
        // exclude nodes where worms are
        players.forEach(player -> nodes.removeAll(this.getPlayerPosition(player).getNodes()));
        // exclude nodes where collectibles are
        final List<Node> finalNodes = nodes.stream().filter(n -> this.getCollectible(n) == null).collect(Collectors
                .toList());
        if (finalNodes.size() == 0) {
            return null;
        } else {
            return finalNodes.get(GameController.RANDOM.nextInt(finalNodes.size()));
        }
    }

}
