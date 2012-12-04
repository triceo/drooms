package org.drooms.impl.logic;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drooms.api.Collectible;
import org.drooms.api.GameReport;
import org.drooms.api.Move;
import org.drooms.api.Player;
import org.drooms.impl.DefaultEdge;
import org.drooms.impl.DefaultNode;
import org.drooms.impl.DefaultPlayground;
import org.drooms.impl.logic.commands.AddCollectibleCommand;
import org.drooms.impl.logic.commands.CollectCollectibleCommand;
import org.drooms.impl.logic.commands.Command;
import org.drooms.impl.logic.commands.CrashPlayerCommand;
import org.drooms.impl.logic.commands.DeactivatePlayerCommand;
import org.drooms.impl.logic.commands.MovePlayerCommand;
import org.drooms.impl.logic.commands.RemoveCollectibleCommand;
import org.drooms.impl.logic.commands.RewardSurvivalCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandDistributor {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommandDistributor.class);

    private static boolean isCommandSupported(
            final Command<DefaultPlayground, DefaultNode, DefaultEdge> command) {
        for (@SuppressWarnings("rawtypes")
        final Class c : CommandDistributor.SUPPORTED_COMMANDS) {
            if (command.getClass() == c) {
                return true;
            }
        }
        return false;
    }

    private final Map<Player, DecisionMaker> players = new LinkedHashMap<>();
    private final Map<Player, PathTracker<DefaultPlayground, DefaultNode, DefaultEdge>> trackers = new LinkedHashMap<>();
    private final GameReport<DefaultPlayground, DefaultNode, DefaultEdge> report;
    private final Set<Collectible> collectibles = new HashSet<>();

    @SuppressWarnings("rawtypes")
    private static final Class[] SUPPORTED_COMMANDS = new Class[] {
            AddCollectibleCommand.class, CollectCollectibleCommand.class,
            CrashPlayerCommand.class, DeactivatePlayerCommand.class,
            MovePlayerCommand.class, RemoveCollectibleCommand.class,
            RewardSurvivalCommand.class };

    public CommandDistributor(final DefaultPlayground playground,
            final List<Player> players,
            final GameReport<DefaultPlayground, DefaultNode, DefaultEdge> report) {
        for (final Player player : players) {
            final PathTracker<DefaultPlayground, DefaultNode, DefaultEdge> tracker = new PathTracker<>(
                    playground, player);
            this.trackers.put(player, tracker);
            this.players.put(player, new DecisionMaker(player, tracker));
        }
        this.report = report;
    }

    public Map<Player, Move> execute(
            final List<Command<DefaultPlayground, DefaultNode, DefaultEdge>> stateChanges) {
        CommandDistributor.LOGGER
                .info("Changing state before the decision making can start.");
        final Map<Player, Deque<DefaultNode>> positions = new HashMap<>();
        for (final Command<DefaultPlayground, DefaultNode, DefaultEdge> change : stateChanges) {
            if (!CommandDistributor.isCommandSupported(change)) {
                CommandDistributor.LOGGER.warn(
                        "Command not supported and will not be executed: {}.",
                        change);
                continue;
            } else if (!change.isValid(this)) {
                CommandDistributor.LOGGER
                        .warn("Command not valid in this context and will not be executed: {}.",
                                change);
                continue;
            }
            // notify all players of the change in state
            for (final DecisionMaker player : this.players.values()) {
                change.perform(player);
            }
            change.report(this.report);
            /*
             * update internal representation of the state so that command
             * validation remains functional.
             */
            if (change instanceof PlayerRelated) {
                // player being removed from the game
                if (change instanceof CrashPlayerCommand
                        || change instanceof DeactivatePlayerCommand) {
                    this.players.remove(((PlayerRelated) change).getPlayer());
                }
            }
            if (change instanceof CollectibleRelated) {
                if (change instanceof AddCollectibleCommand) {
                    this.collectibles.add(((CollectibleRelated) change)
                            .getCollectible());
                } else {
                    this.collectibles.remove(((CollectibleRelated) change)
                            .getCollectible());
                }
            }
            if (change instanceof MovePlayerCommand) { // update paths in the
                                                       // WMs
                final MovePlayerCommand cmd = (MovePlayerCommand) change;
                positions.put(cmd.getPlayer(), cmd.getNodes());
            }
        }
        // make sure that the path-querying information is up-to-date
        CommandDistributor.LOGGER.info("Asking players to decide.");
        // determine the movements of players
        final Map<Player, Move> moves = new HashMap<Player, Move>();
        for (final Map.Entry<Player, DecisionMaker> entry : this.players
                .entrySet()) {
            this.trackers.get(entry.getKey()).movePlayers(positions);
            final Move decision = entry.getValue().decideNextMove();
            final Player player = entry.getKey();
            moves.put(player, decision);
        }
        CommandDistributor.LOGGER.info("All players have decided.");
        return Collections.unmodifiableMap(moves);
    }

    public boolean hasCollectible(final Collectible c) {
        return this.collectibles.contains(c);
    }

    public boolean hasPlayer(final Player p) {
        return this.players.containsKey(p);
    }

}
