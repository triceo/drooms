package org.drooms.impl.logic;

import java.io.File;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.drooms.api.GameReport;
import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.DefaultPlayground;
import org.drooms.impl.logic.commands.Command;
import org.drooms.impl.logic.commands.DeactivatePlayerCommand;
import org.drooms.impl.logic.commands.MovePlayerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandDistributor {

    private static class DecisionMakerUnit implements Callable<Move> {

        private final DecisionMaker playerLogic;
        private final List<Command> commands;

        public DecisionMakerUnit(final DecisionMaker m,
                final List<Command> commands) {
            this.playerLogic = m;
            this.commands = commands;
        }

        @Override
        public Move call() throws Exception {
            for (final Command command : this.commands) {
                command.perform(this.playerLogic);
            }
            return this.playerLogic.decideNextMove();
        }

    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommandDistributor.class);

    private static Map<Player, Deque<Node>> retrieveNewPlayerPositions(
            final List<Command> commands) {
        final Map<Player, Deque<Node>> positions = new HashMap<>();
        for (final Command command : commands) {
            if (command instanceof MovePlayerCommand) {
                final MovePlayerCommand cmd = (MovePlayerCommand) command;
                positions.put(cmd.getPlayer(), cmd.getNodes());
            }
        }
        return Collections.unmodifiableMap(positions);
    }

    private static Set<Player> retrievePlayersToRemove(
            final List<Command> commands) {
        final Set<Player> players = new HashSet<>();
        for (final Command command : commands) {
            if (command instanceof DeactivatePlayerCommand) {
                // player being removed from the game
                players.add(((PlayerRelated) command).getPlayer());
            }
        }
        return Collections.unmodifiableSet(players);
    }

    private final Map<Player, DecisionMaker> players = new LinkedHashMap<>();
    private final Map<Player, PathTracker<DefaultPlayground>> trackers = new LinkedHashMap<>();

    private final GameReport report;

    private final int playerTimeoutInSeconds;

    private final ExecutorService e = Executors.newFixedThreadPool(1);

    public CommandDistributor(final DefaultPlayground playground,
            final List<Player> players, final GameReport report,
            final File reportFolder, final int playerTimeoutInSeconds) {
        for (final Player player : players) {
            final PathTracker<DefaultPlayground> tracker = new PathTracker<>(
                    playground, player);
            this.trackers.put(player, tracker);
            this.players.put(player, new DecisionMaker(player, tracker,
                    reportFolder));
        }
        this.report = report;
        this.playerTimeoutInSeconds = playerTimeoutInSeconds;
    }

    public Map<Player, Move> execute(final List<Command> commands) {
        CommandDistributor.LOGGER
                .info("First reporting what happens in this turn.");
        this.report.nextTurn();
        for (final Command command : commands) {
            command.report(this.report);
        }
        CommandDistributor.LOGGER.info("Now passing these changes to players.");
        final Map<Player, Deque<Node>> positions = CommandDistributor
                .retrieveNewPlayerPositions(commands);
        final Set<Player> playersToRemove = CommandDistributor
                .retrievePlayersToRemove(commands);
        final Map<Player, Move> moves = new HashMap<Player, Move>();
        for (final Map.Entry<Player, DecisionMaker> entry : this.players
                .entrySet()) {
            final Player player = entry.getKey();
            if (playersToRemove.contains(player)) {
                continue;
            }
            final DecisionMaker playerLogic = entry.getValue();
            this.trackers.get(player).movePlayers(positions);
            CommandDistributor.LOGGER.debug("Processing player {}.",
                    player.getName());
            final DecisionMakerUnit dmu = new DecisionMakerUnit(playerLogic,
                    commands);
            // begin the time-box for a player strategy
            final Future<Move> move = this.e.submit(dmu);
            try {
                moves.put(player,
                        move.get(this.playerTimeoutInSeconds, TimeUnit.SECONDS));
            } catch (final Exception e) {
                CommandDistributor.LOGGER
                        .warn("Player {}, didn't reach a decision in time, STAY forced.",
                                player.getName(), e);
                move.cancel(true);
                moves.put(player, Move.STAY);
            }
            // end the time-box for a player strategy
            CommandDistributor.LOGGER.debug("Player {} processed.",
                    player.getName());
        }
        // purge dead players
        for (final Player p : playersToRemove) {
            CommandDistributor.LOGGER.debug("Removing player {}.", p.getName());
            final DecisionMaker dm = this.players.remove(p);
            dm.terminate();
            this.trackers.remove(p);
        }
        CommandDistributor.LOGGER.info("Turn processed completely.");
        return Collections.unmodifiableMap(moves);
    }

    public GameReport getReport() {
        return this.report;
    }

    public void terminate() {
        for (final Map.Entry<Player, DecisionMaker> entry : this.players
                .entrySet()) {
            entry.getValue().terminate();
        }
        this.e.shutdownNow();
    }

}
