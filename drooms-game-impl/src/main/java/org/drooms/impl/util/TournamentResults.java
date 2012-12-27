package org.drooms.impl.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.drooms.api.Player;

public abstract class TournamentResults {

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

        public BigDecimal getAverage(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getMean());
        }

        public BigDecimal getFirstQuartile(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getPercentile(0.25));
        }

        public BigDecimal getMax(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getMax());
        }

        public BigDecimal getMedian(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getPercentile(0.5));
        }

        public BigDecimal getMin(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getMin());
        }

        public BigDecimal getStandardDeviation(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getStandardDeviation());
        }

        private DescriptiveStatistics getStats(final Player p) {
            if (this.stats.containsKey(p)) {
                return this.stats.get(p);
            } else {
                return new DescriptiveStatistics();
            }
        }

        public BigDecimal getThirdQuartile(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getPercentile(0.75));
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

    protected abstract List<Collection<Player>> evaluateGame(
            final Collection<Player> players, final GameResults game);

    protected abstract SortedMap<Long, Collection<Player>> evaluateTournament(
            final Collection<Player> players,
            final Collection<List<Collection<Player>>> gameResults);

    public Collection<String> getGameNames() {
        return Collections.unmodifiableSet(this.results.keySet());
    }

}
