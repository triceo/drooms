package org.drooms.impl.util;

import org.drooms.api.Action;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;

import java.util.*;
import java.util.stream.Collectors;

public class Detectors {

    /**
     * Detect players that have been inactive for so long, they have gone over the threshold.
     *
     * @param allowedInactiveTurns The maximum allowed number of turns for which the player is allowed to be inactive.
     *                             Any more than this and the player will be considered inactive. Negative number means
     *                             infinity.
     * @param players Players and their activities. The closer to the end of the list, the more recent the action.
     * @return Players that have been inactive for longer than allowed.
     */
    public static Set<Player> detectInactivity(final int allowedInactiveTurns, Map<Player, List<Action>> players) {
        if (players == null) {
            throw new IllegalArgumentException("Players must not be null.");
        } else if (players.isEmpty()) {
            return Collections.emptySet();
        } else if (allowedInactiveTurns < 0) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(players.keySet().parallelStream().filter(p ->
                !Detectors.isActive(players.get(p), allowedInactiveTurns)).collect(Collectors.toSet()));
    }

    protected static boolean isActive(final List<Action> actions, final int allowedInactiveTurns) {
        final int turnCount = actions.size();
        if (turnCount < allowedInactiveTurns) {
            return true;
        }
        final List<Action> relevantActions = actions.subList(Math.max(0, turnCount - allowedInactiveTurns - 1),
                turnCount);
        final Collection<Action> uniqueActions = new HashSet<>(relevantActions);
        // TODO is "NOTHING" the only inactivity?
        if (uniqueActions.size() == 1 && uniqueActions.contains(Action.NOTHING)) {
            return false;
        }
        return true;
    }

    protected static boolean didPlayerHitItself(final Deque<Node> player) {
        final Collection<Node> nodes = new HashSet<>(player);
        return nodes.size() < player.size();
    }

    protected static boolean didPlayerHitWall(final Deque<Node> player, final Playground playground) {
        final Node playerHeadPosition = player.getFirst();
        return !playground.isAvailable(playerHeadPosition.getX(), playerHeadPosition.getY());
    }

    protected static boolean didPlayerCollideWithOther(final Deque<Node> player, final Deque<Node> otherPlayer) {
        if (player == otherPlayer) { // identical players
            return false;
        }
        return otherPlayer.contains(player.getFirst());
    }

    /**
     * Detect players who performed a move that will get them killed. This either means running into a wall, colliding
     * with themselves or colliding with another player.
     *
     * @param playground Playground in which the players compete.
     * @param players Positions of the players to be evaluated.
     * @return Unmodifiable set of players that have in some way collided.
     */
    public static Set<Player> detectCollision(final Playground playground, final Map<Player, Deque<Node>> players) {
        // find all players that collided by crashing into themselves or into a wall
        final Collection<Player> playersNotFailed = players.keySet().stream().filter(player ->
                !Detectors.didPlayerHitItself(players.get(player))).filter(player ->
                !Detectors.didPlayerHitWall(players.get(player), playground)).collect(Collectors.toSet());
        // for every other player, check for collisions with all known players
        final Collection<Player> playersSurviving = playersNotFailed.stream().filter(player -> {
            final Deque<Node> position = players.get(player);
            for (Deque<Node> otherPosition: players.values()) {
                if (Detectors.didPlayerCollideWithOther(position, otherPosition)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toSet());
        // and assemble the final collection of worms that have somehow crashed
        return Collections.unmodifiableSet(players.keySet().stream().filter(player ->
                !playersSurviving.contains(player)).collect(Collectors.toSet()));
    }

}
