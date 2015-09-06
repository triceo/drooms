package org.drooms.launcher.tournament;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.drooms.api.Player;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class TournamentResults {

    static class GameResults {

        private final Map<Player, DescriptiveStatistics> stats = new HashMap<>();
        private int games = 0;

        private GameResults() {
            // prevent outside instantiation
        }

        public void addResults(final Map<Player, Integer> result) {
            this.games++;
            result.forEach((p, points) -> {
                this.stats.getOrDefault(p, new DescriptiveStatistics()).addValue(points);
            });
        }

        // FIXME better name
        public int getGames() {
            return this.games;
        }

        public BigDecimal getMedian(final Player p) {
            return BigDecimal.valueOf(this.getStats(p).getPercentile(50));
        }

        private DescriptiveStatistics getStats(final Player p) {
            if (this.stats.containsKey(p)) {
                return this.stats.get(p);
            } else {
                final DescriptiveStatistics d = new DescriptiveStatistics();
                d.addValue(0.0);
                return d;
            }
        }

    }

    private final Map<String, GameResults> results = new HashMap<>();

    private final Collection<Player> players;
    private final String name;

    public TournamentResults(final String name, final Collection<Player> players) {
        this.name = name;
        this.players = Collections.unmodifiableCollection(players);
    }

    public void addResults(final String game, final Map<Player, Integer> result) {
        if (!this.results.containsKey(game)) {
            this.results.put(game, new GameResults());
        }
        this.results.get(game).addResults(result);
    }

    private Map<String, List<Collection<Player>>> assembleGameOverview() {
        return Collections.unmodifiableMap(this.results.keySet().stream().collect(Collectors.toMap(Function.identity(),
                key -> this.evaluateGame(this.players, this.results.get(key)))));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map assembleResults() {
        final Map result = new HashMap();
        result.put("name", this.name);
        result.put("players", this.players);
        result.put("games", this.assembleTournamentOverview());
        result.put("gameScore", this.assembleGameOverview());
        result.put("gameResults", this.results);
        result.put("results", this.evaluate());
        return Collections.unmodifiableMap(result);
    }

    private Map<String, Integer> assembleTournamentOverview() {
        return Collections.unmodifiableMap(this.results.keySet().stream().collect(Collectors.toMap(Function.identity(),
                key -> this.results.get(key).getGames())));
    }

    public Map<Long, Collection<Player>> evaluate() {
        final Collection<List<Collection<Player>>> gameResults = this.getGameNames().stream().map(game ->
                this.evaluateGame(this.players, this.results.get(game))).collect(Collectors.toList());
        return this.evaluateTournament(this.players, gameResults);
    }

    protected abstract List<Collection<Player>> evaluateGame(final Collection<Player> players, final GameResults game);

    protected abstract SortedMap<Long, Collection<Player>> evaluateTournament(final Collection<Player> players,
            final Collection<List<Collection<Player>>> gameResults);

    public Collection<String> getGameNames() {
        return Collections.unmodifiableSet(this.results.keySet());
    }

    public void write(final Writer w) throws IOException {
        final Configuration freemarker = new Configuration();
        freemarker.setClassForTemplateLoading(TournamentResults.class, "");
        freemarker.setObjectWrapper(new BeansWrapper());
        freemarker.setLocale(Locale.US);
        freemarker.setNumberFormat("computer");
        try {
            freemarker.getTemplate("tournament-report.html.ftl").process(
                    this.assembleResults(), w);
        } catch (final TemplateException ex) {
            throw new IllegalStateException("Invalid template.", ex);
        }
    }

}
