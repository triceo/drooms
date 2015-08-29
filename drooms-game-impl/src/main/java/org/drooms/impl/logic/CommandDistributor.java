package org.drooms.impl.logic;

import org.drooms.api.*;
import org.drooms.impl.GameController;
import org.drooms.impl.logic.commands.Command;
import org.drooms.impl.logic.commands.CrashPlayerCommand;
import org.drooms.impl.logic.commands.DeactivatePlayerCommand;
import org.drooms.impl.logic.commands.PlayerActionCommand;
import org.drooms.impl.util.GameProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Receives state changes ({@link Command}s) from the {@link GameController} and
 * distributes them to all the player strategies ({@link DecisionMaker} to
 * process them and make {@link Action} decisions on them.
 */
public class CommandDistributor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandDistributor.class);

    private static Map<Player, Deque<Node>> retrieveNewPlayerPositions(final List<Command> commands) {
        final Map<Player, Deque<Node>> positions = new HashMap<>();
        commands.stream().filter(command -> command instanceof PlayerActionCommand).collect(Collectors.toMap(
                command -> ((PlayerActionCommand) command).getPlayer(), command -> ((PlayerActionCommand) command)
                        .getNodes()));
        return Collections.unmodifiableMap(positions);
    }

    private final Map<Player, DecisionMaker> players = new LinkedHashMap<>();
    private final Map<Player, PathTracker> trackers = new LinkedHashMap<>();

    private final List<GameProgressListener> listeners = new LinkedList<GameProgressListener>();

    private final int playerTimeoutInSeconds;

    private final ExecutorService e = Executors.newFixedThreadPool(1);
    private final List<Command> commands = new LinkedList<>();

    /**
     * Initialize the class.
     * 
     * @param playground
     *            The playground on which the game is happening.
     * @param players
     *            The players taking part in the game.
     * @param report
     *            The game listener.
     * @param properties
     *            Configuration of the game.
     * @param reportFolder
     *            Where to report to.
     * @param playerTimeoutInSeconds
     *            How much time the player strategies should be given to make
     *            move decisions.
     */
    public CommandDistributor(final Playground playground, final Collection<Player> players,
            final GameProgressListener report, final GameProperties properties, final File reportFolder,
            final int playerTimeoutInSeconds) {
        players.forEach(player -> {
            final PathTracker tracker = new PathTracker(playground, player);
            this.trackers.put(player, tracker);
            this.players.put(player, new DecisionMaker(player, tracker, properties, reportFolder));
        });
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
     * @return Strategy decisions.
     */
    public Map<Player, Action> execute() {
        // hint GC to potentially not interrupt decision making later
        System.gc();
        CommandDistributor.LOGGER.info("Starting processing next turn.");
        this.listeners.forEach(listener -> listener.nextTurn());
        this.commands.forEach(command -> {
            CommandDistributor.LOGGER.info("Will process command: {}", command);
            this.listeners.forEach(listener -> command.report(listener));
        });
        CommandDistributor.LOGGER.info("Now passing these changes to players.");
        final Map<Player, Deque<Node>> positions = CommandDistributor.retrieveNewPlayerPositions(commands);
        final Map<Player, Action> moves = new HashMap<Player, Action>();
        this.players.forEach((player, decisionMaker) -> {
            CommandDistributor.LOGGER.debug("Processing player {}.", player.getName());
            this.trackers.get(player).updatePlayerPositions(positions);
            // send commands to the player's strategy
            this.commands.forEach(command -> command.perform(decisionMaker));
            // begin the time-box for a player strategy to make decisions
            CommandDistributor.LOGGER.debug("Starting time-box for player {}.", player.getName());
            final Future<Action> move = this.e.submit(() -> decisionMaker.decideNextMove());
            try {
                moves.put(player, move.get(this.playerTimeoutInSeconds, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException e) {
                CommandDistributor.LOGGER.warn("Player {} error during decision-making, STAY forced.",
                        player.getName(), e);
                moves.put(player, Action.NOTHING);
            } catch (final TimeoutException e) {
                CommandDistributor.LOGGER.info("Player {} didn't reach a decision in time, STAY forced.",
                        player.getName());
                moves.put(player, Action.NOTHING);
            } finally {
                move.cancel(true);
                decisionMaker.halt(); // otherwise other players' are slowed down
            }
            // end the time-box for a player strategy
            CommandDistributor.LOGGER.debug("Player {} processed.", player.getName());
        });
        commands.clear();
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
        this.players.forEach((player, decisionMaker) -> decisionMaker.terminate());
        this.e.shutdownNow();
    }

    /**
     * Get the players in the game (i.e. not disqualified, nor dead).
     * 
     * @return Unmodifiable collection of current players.
     */
    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(players.keySet());
    }

    /**
     * Distribute the command to players.
     * 
     * @param command
     *            Command to distribute.
     */
    public void distributeCommand(Command command) {
        if (command instanceof DeactivatePlayerCommand) {
            removePlayer(((DeactivatePlayerCommand) command).getPlayer());
        }
        if (command instanceof CrashPlayerCommand) {
            removePlayer(((CrashPlayerCommand) command).getPlayer());
        }

        commands.add(command);
    }

    private void removePlayer(Player player) {
        CommandDistributor.LOGGER.debug("Removing player {}.", player.getName());
        final DecisionMaker dm = this.players.remove(player);
        dm.terminate();
        this.trackers.remove(player);
    }
}
