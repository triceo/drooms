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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.drooms.api.Player;

public class TournamentResults {

    protected static class GameResults {

        private final Map<Player, DescriptiveStatistics> stats = new HashMap<>();

        private GameResults() {
            // prevent outside instantiation
        }

        public void addResults(final Map<Player, Integer> result) {
            for (final Map.Entry<Player, Integer> entry : result.entrySet()) {
                final Player p = entry.getKey();
                final int points = entry.getValue();
                if (!this.stats.containsKey(p)) {
                    this.stats.put(p, new DescriptiveStatistics());
                }
                this.stats.get(p).addValue(points);
            }
        }
        
        private DescriptiveStatistics getStats(Player p) {
            if (stats.containsKey(p)) {
                return stats.get(p);
            } else {
                return new DescriptiveStatistics();
            }
        }

        public BigDecimal getAverage(final Player p) {
            return BigDecimal.valueOf(getStats(p).getMean());
        }

        public BigDecimal getFirstQuartile(final Player p) {
            return BigDecimal.valueOf(getStats(p).getPercentile(0.25));
        }

        public BigDecimal getMax(final Player p) {
            return BigDecimal.valueOf(getStats(p).getMax());
        }

        public BigDecimal getMedian(final Player p) {
            return BigDecimal.valueOf(getStats(p).getPercentile(0.5));
        }

        public BigDecimal getMin(final Player p) {
            return BigDecimal.valueOf(getStats(p).getMin());
        }

        public BigDecimal getStandardDeviation(final Player p) {
            return BigDecimal.valueOf(getStats(p).getStandardDeviation());
        }

        public BigDecimal getThirdQuartile(final Player p) {
            return BigDecimal.valueOf(getStats(p).getPercentile(0.75));
        }
    }

    private final Map<String, GameResults> results = new HashMap<>();

    private final Collection<Player> players;

    public TournamentResults(final Collection<Player> players) {
        this.players = players;
    }

    public void addResults(final String game, final Map<Player, Integer> result) {
        if (!this.results.containsKey(game)) {
            this.results.put(game, new GameResults());
        }
        this.results.get(game).addResults(result);
    }

    public Map<Long, Collection<Player>> evaluate() {
        final Collection<List<Collection<Player>>> gameResults = new LinkedList<>();
        for (final String game : this.getGameNames()) {
            gameResults.add(this.evaluateGame(this.players,
                    this.results.get(game)));
        }
        return this.evaluateTournament(this.players, gameResults);
    }

    // FIXME make abstract
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

    // FIXME make abstract
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

    public Collection<String> getGameNames() {
        return Collections.unmodifiableSet(this.results.keySet());
    }

}
