package org.drooms.impl.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.drooms.api.Player;

public class DroomsTournamentResults extends TournamentResults {

    public DroomsTournamentResults(final Collection<Player> players) {
        super(players);
    }

    @Override
    protected List<Collection<Player>> evaluateGame(
            final Collection<Player> players, final GameResults game) {
        // group players by their median results
        final MultiMap<BigDecimal, Player> grouped = new MultiHashMap<>();
        for (final Player p : players) {
            grouped.put(game.getMedian(p), p);
        }
        // order the grouped players
        final SortedMap<BigDecimal, Collection<Player>> ordered = new TreeMap<>();
        for (final Entry<BigDecimal, Collection<Player>> entry : grouped
                .entrySet()) {
            ordered.put(entry.getKey(), entry.getValue());
        }
        // store the results in a reverse order; median biggest to lowest
        final List<Collection<Player>> results = new LinkedList<>(
                ordered.values());
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
        final MultiMap<Long, Player> grouped = new MultiHashMap<>();
        for (final Player p : players) {
            grouped.put(points.get(p), p);
        }
        // order the grouped players
        final SortedMap<Long, Collection<Player>> ordered = new TreeMap<>();
        for (final Entry<Long, Collection<Player>> entry : grouped.entrySet()) {
            ordered.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableSortedMap(ordered);
    }

}
