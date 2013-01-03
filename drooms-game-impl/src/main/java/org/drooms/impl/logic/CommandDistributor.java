package org.drooms.impl.logic;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.drooms.api.GameProgressListener;
import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.GameController;
import org.drooms.impl.logic.commands.Command;
import org.drooms.impl.logic.commands.DeactivatePlayerCommand;
import org.drooms.impl.logic.commands.MovePlayerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives state changes ({@link Command}s) from the {@link GameController} and
 * distributes them to all the player strategies ({@link DecisionMaker} to
 * process them and make {@link Move} decisions on them.
 */
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
    private final Map<Player, PathTracker> trackers = new LinkedHashMap<>();

    private final List<GameProgressListener> listeners = new LinkedList<GameProgressListener>();

    private final int playerTimeoutInSeconds;

    private final ExecutorService e = Executors.newFixedThreadPool(1);

    /**
     * Initialize the class.
     * 
     * @param playground
     *            The playground on which the game is happening.
     * @param players
     *            The players taking part in the game.
     * @param report
     *            The game listener.
     * @param reportFolder
     *            Where to report to.
     * @param playerTimeoutInSeconds
     *            How much time the player strategies should be given to make
     *            move decisions.
     */
    public CommandDistributor(final Playground playground,
            final Collection<Player> players,
            final GameProgressListener report, final File reportFolder,
            final int playerTimeoutInSeconds) {
        for (final Player player : players) {
            final PathTracker tracker = new PathTracker(playground, player);
            this.trackers.put(player, tracker);
            this.players.put(player, new DecisionMaker(player, tracker,
                    reportFolder));
        }
        this.listeners.add(report);
        this.playerTimeoutInSeconds = playerTimeoutInSeconds;
    }

    /**
     * Add another listener.
     * 
     * @param listener
     * @return True if added, false if already added.
     */
    public boolean addListener(final GameProgressListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Execute the commands.
     * 
     * @param commands
     *            A collection of state changes, to be handed over to strategies
     *            in this exact order.
     * @return Strategy decisions.
     */
    public Map<Player, Move> execute(final List<Command> commands) {
        CommandDistributor.LOGGER
                .info("First reporting what happens in this turn.");
        for (final GameProgressListener listener : this.listeners) {
            listener.nextTurn();
        }
        for (final Command command : commands) {
            CommandDistributor.LOGGER.info("Will process command: {}", command);
            for (final GameProgressListener listener : this.listeners) {
                command.report(listener);
            }
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
            } catch (InterruptedException | ExecutionException e) {
                CommandDistributor.LOGGER
                        .warn("Player {} error during decision-making, STAY forced.",
                                player.getName(), e);
                moves.put(player, Move.STAY);
            } catch (final TimeoutException e) {
                CommandDistributor.LOGGER
                        .warn("Player {}, didn't reach a decision in time, STAY forced.",
                                player.getName());
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

    public GameProgressListener getReport() {
        return this.listeners.get(0);
    }

    /**
     * Clean up when the game is over. This instance shouldn't be used anymore
     * after this method is called. Not calling this method after the game may
     * result in the JVM not terminating, since the executors will still be
     * active.
     */
    public void terminate() {
        for (final Map.Entry<Player, DecisionMaker> entry : this.players
                .entrySet()) {
            entry.getValue().terminate();
        }
        this.e.shutdownNow();
    }

}
