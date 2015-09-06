package org.drooms.launcher.tournament;

import org.drooms.api.Player;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DroomsTournamentResults extends TournamentResults {

    public DroomsTournamentResults(final String name, final Collection<Player> players) {
        super(name, players);
    }

    @Override
    protected List<Collection<Player>> evaluateGame(final Collection<Player> players, final GameResults game) {
        // group players by their median results
        final SortedMap<BigDecimal, Collection<Player>> grouped = new TreeMap<>();
        players.forEach(p -> {
            grouped.getOrDefault(game.getMedian(p), new HashSet<>()).add(p);
        });
        // store the results in a reverse order; median biggest to lowest
        final List<Collection<Player>> results = new LinkedList<>(grouped.values());
        Collections.reverse(results);
        return Collections.unmodifiableList(results);
    }

    @Override
    protected SortedMap<Long, Collection<Player>> evaluateTournament(final Collection<Player> players,
            final Collection<List<Collection<Player>>> gameResults) {
        final Map<Player, Long> points = new HashMap<>();
        // value each player
        gameResults.forEach(gameResult -> {
            // for each game result...
            final AtomicInteger i = new AtomicInteger(0);
            gameResult.forEach(playersSharingPosition -> {
                // for each player sharing the same position
                playersSharingPosition.forEach(p -> {
                    // add 2^position points
                    points.put(p, points.getOrDefault(p, 0l) + Math.round(Math.pow(2, i.get())));
                });
                i.incrementAndGet();
            });
        });
        // group players by their results
        final SortedMap<Long, Collection<Player>> grouped = new TreeMap<>();
        players.forEach(p -> {
            grouped.getOrDefault(points.get(p), new HashSet<>()).add(p);
        });
        return Collections.unmodifiableSortedMap(grouped);
    }

}
