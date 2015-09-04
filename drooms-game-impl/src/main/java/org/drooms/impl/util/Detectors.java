package org.drooms.impl.util;

import org.drooms.api.Action;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;

import java.util.*;
import java.util.stream.Collectors;

public class Detectors {

    public static Set<Player> detectInactivity(final int currentTurn, final int allowedInactiveTurns,
                                                      Map<Player, List<Action>> players) {
        if (currentTurn <= allowedInactiveTurns) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(players.keySet().parallelStream().filter(p -> {
            final List<Action> allMoves = players.get(p);
            final int size = allMoves.size();
            final Collection<Action> relevantMoves = new HashSet<>(allMoves.subList(Math.max(0,
                    size - allowedInactiveTurns - 1), size));
            if (relevantMoves.size() == 1 && relevantMoves.contains(Action.NOTHING)) {
                return true;
            }
            return false;
        }).collect(Collectors.toSet()));
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
