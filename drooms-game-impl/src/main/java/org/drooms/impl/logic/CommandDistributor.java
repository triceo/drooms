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

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommandDistributor.class);

    private final Map<Player, DecisionMaker> players = new LinkedHashMap<>();
    private final Map<Player, PathTracker<DefaultPlayground>> trackers = new LinkedHashMap<>();
    private final GameReport<DefaultPlayground> report;

    public CommandDistributor(final DefaultPlayground playground,
            final List<Player> players,
            final GameReport<DefaultPlayground> report, final File reportFolder) {
        for (final Player player : players) {
            final PathTracker<DefaultPlayground> tracker = new PathTracker<>(
                    playground, player);
            this.trackers.put(player, tracker);
            this.players.put(player, new DecisionMaker(player, tracker,
                    reportFolder));
        }
        this.report = report;
    }

    public Map<Player, Move> execute(
            final List<Command<DefaultPlayground>> commands) {
        CommandDistributor.LOGGER
                .info("First reporting what happens in this turn.");
        this.report.nextTurn();
        for (final Command<DefaultPlayground> command : commands) {
            command.report(this.report);
        }
        CommandDistributor.LOGGER
                .info("Now passing these changes to players.");
        final Map<Player, Deque<Node>> positions = this
                .retrieveNewPlayerPositions(commands);
        final Set<Player> playersToRemove = this
                .retrievePlayersToRemove(commands);
        final Map<Player, Move> moves = new HashMap<Player, Move>();
        for (final Map.Entry<Player, DecisionMaker> entry : this.players
                .entrySet()) {
            final Player player = entry.getKey();
            final DecisionMaker playerLogic = entry.getValue();
            CommandDistributor.LOGGER.debug("Processing player {}.",
                    player.getName());
            for (final Command<DefaultPlayground> command : commands) {
                command.perform(playerLogic);
            }
            if (!playersToRemove.contains(player)) {
                this.trackers.get(player).movePlayers(positions);
                CommandDistributor.LOGGER.debug(
                        "Asking player {} to decide on the next move.",
                        player.getName());
                moves.put(player, playerLogic.decideNextMove());
            }
            CommandDistributor.LOGGER.debug("Player {} processed.",
                    player.getName());
        }
        // purge dead players
        for (final Player p : playersToRemove) {
            final DecisionMaker dm = this.players.remove(p);
            dm.terminate();
            this.trackers.remove(p);
        }
        CommandDistributor.LOGGER.info("Turn processed completely.");
        return Collections.unmodifiableMap(moves);
    }

    public GameReport<DefaultPlayground> getReport() {
        return this.report;
    }

    private Map<Player, Deque<Node>> retrieveNewPlayerPositions(
            final List<Command<DefaultPlayground>> commands) {
        final Map<Player, Deque<Node>> positions = new HashMap<>();
        for (final Command<DefaultPlayground> command : commands) {
            if (command instanceof MovePlayerCommand) {
                final MovePlayerCommand cmd = (MovePlayerCommand) command;
                positions.put(cmd.getPlayer(), cmd.getNodes());
            }
        }
        return Collections.unmodifiableMap(positions);
    }

    private Set<Player> retrievePlayersToRemove(
            final List<Command<DefaultPlayground>> commands) {
        final Set<Player> players = new HashSet<>();
        for (final Command<DefaultPlayground> command : commands) {
            if (command instanceof DeactivatePlayerCommand) {
                // player being removed from the game
                players.add(((PlayerRelated) command).getPlayer());
            }
        }
        return Collections.unmodifiableSet(players);
    }

    public void terminate() {
        for (final Map.Entry<Player, DecisionMaker> entry : this.players
                .entrySet()) {
            entry.getValue().terminate();
        }
    }

}
