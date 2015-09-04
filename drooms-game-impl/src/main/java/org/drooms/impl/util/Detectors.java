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

    public static Set<Player> detectCollision(final Playground playground, final Map<Player, Deque<Node>> players) {
        final Set<Player> collisions = new HashSet<>();
        for (final Map.Entry<Player, Deque<Node>> entry: players.entrySet()) {
            final Deque<Node> positions = entry.getValue();
            final Player player = entry.getKey();
            final Node playerHeadPosition = positions.getFirst();
            if (!playground.isAvailable(playerHeadPosition.getX(), playerHeadPosition.getY())) {
                collisions.add(player);
                continue;
            } else {
                // make sure the worm didn't crash into itself
                final Collection<Node> nodes = new HashSet<>(positions);
                if (nodes.size() < positions.size()) {
                    // a worm occupies one node twice = a crash into itself
                    collisions.add(player);
                }
            }
            for (final Player otherPlayer : players.keySet()) {
                if (player == otherPlayer) {
                    // the same worm
                    continue;
                }
                final Node otherPlayerHeadPosition = players.get(otherPlayer).getFirst();
                if (playerHeadPosition.equals(otherPlayerHeadPosition)) {
                    // head-on-head collision
                    collisions.add(player);
                    collisions.add(otherPlayer);
                } else if (positions.contains(otherPlayerHeadPosition)) {
                    // head-on-body collision
                    collisions.add(otherPlayer);
                }
            }
        }
        return Collections.unmodifiableSet(collisions);
    }

}
