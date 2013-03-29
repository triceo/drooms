package org.drooms.launcher.tournament;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.drooms.api.Player;

public class DroomsTournamentResults extends TournamentResults {

    public DroomsTournamentResults(final String name,
            final Collection<Player> players) {
        super(name, players);
    }

    @Override
    protected List<Collection<Player>> evaluateGame(
            final Collection<Player> players, final GameResults game) {
        // group players by their median results
        final SortedMap<BigDecimal, Collection<Player>> grouped = new TreeMap<>();
        for (final Player p : players) {
            final BigDecimal key = game.getMedian(p);
            if (!grouped.containsKey(key)) {
                grouped.put(key, new HashSet<Player>());
            }
            grouped.get(key).add(p);
        }
        // store the results in a reverse order; median biggest to lowest
        final List<Collection<Player>> results = new LinkedList<>(
                grouped.values());
        Collections.reverse(results);
        return Collections.unmodifiableList(results);
    }

    @Override
    protected SortedMap<Long, Collection<Player>> evaluateTournament(
            final Collection<Player> players,
            final Collection<List<Collection<Player>>> gameResults) {
        final Map<Player, Long> points = new HashMap<>();
        for (final Player p : players) {
            points.put(p, 0l);
        }
        // value each player
        for (final List<Collection<Player>> gameResult : gameResults) {
            // for each game result...
            int i = 0;
            for (final Collection<Player> playersSharingPosition : gameResult) {
                // for each player sharing the same position
                for (final Player p : playersSharingPosition) {
                    // add 2^position points
                    final long previousPoints = points.get(p);
                    points.put(p, previousPoints + Math.round(Math.pow(2, i)));
                }
                i++;
            }
        }
        // group players by their results
        final SortedMap<Long, Collection<Player>> grouped = new TreeMap<>();
        for (final Player p : players) {
            final long key = points.get(p);
            if (!grouped.containsKey(key)) {
                grouped.put(key, new HashSet<Player>());
            }
            grouped.get(key).add(p);
        }
        return Collections.unmodifiableSortedMap(grouped);
    }

}
