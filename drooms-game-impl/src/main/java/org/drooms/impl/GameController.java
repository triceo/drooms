package org.drooms.impl;

import org.drooms.api.*;
import org.drooms.impl.logic.CommandDistributor;
import org.drooms.impl.logic.commands.*;
import org.drooms.impl.util.GameProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Provide a common ground for various types of games. We introduce a couple of
 * concepts and let the implementing classes specify the rules around those
 * concepts. The following concepts will be shared by all games extending this
 * class:
 * 
 * <ul>
 * <li>Each player gets one worm. Properties of these worms come from the game config and will be explained later. List
 * of players comes from the player config.</li>
 * <li>When a worm collides with something, it is terminated. Collisions are determined by classes extending this one.</li>
 * <li>When a worm's past couple decisions were all STAY (see {@link Action}), the worm may be terminated. This is
 * controlled by the classes extending this one.</li>
 * <li>When a turn ends, worms may be rewarded for surviving. How and when, that depends on the classes extending this
 * one.</li>
 * <li>Terminated worms will disappear from the playground in the next turn.</li>
 * <li>In each turn, a collectible item of a certain value may appear in the playground. These collectibles will
 * disappear after a certain amount of turns. Worms who collect them in the meantime will be rewarded. How often, how
 * valuable and how persistent the collectibles are, that depends on the classes extending this one.</li>
 * <li>Upon collecting an item, the worm's length will increase by 1.</li>
 * <li>Game ends either when there are between 0 and 1 worms standing or when a maximum number of turns is reached.</li>
 * <li>At the end of the game, a player whose worm has the most points is declared the winner.</li>
 * </ul>
 * 
 * <p>
 * Some of the decisions can be made by classes extending this one. These are clearly described above. This class
 * depends on properties as defined in {@link GameProperties}.
 * </p>
 * 
 */
public abstract class GameController implements Game {

    private final AtomicBoolean played = new AtomicBoolean(false);

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    private GameProgressListener reporter;

    protected static final SecureRandom RANDOM = new SecureRandom();

    private final Map<Player, Integer> playerPoints = new HashMap<Player, Integer>();

    private final Map<Player, Integer> lengths = new HashMap<Player, Integer>();

    private final Map<Player, Deque<Node>> positions = new HashMap<Player, Deque<Node>>();

    private final Map<Node, Collectible> collectiblesByNode = new HashMap<Node, Collectible>();

    private final Map<Player, SortedMap<Integer, Action>> decisionRecord = new HashMap<Player, SortedMap<Integer, Action>>();

    private GameProperties gameConfig;

    private final Set<GameProgressListener> listeners = new HashSet<GameProgressListener>();

    private void addCollectible(final Collectible c) {
        this.collectiblesByNode.put(c.getAt(), c);
    }

    private void addDecision(final Player p, final Action m, final int turnNumber) {
        if (!this.decisionRecord.containsKey(p)) {
            this.decisionRecord.put(p, new TreeMap<Integer, Action>());
        }
        this.decisionRecord.get(p).put(turnNumber, m);
    }

    @Override
    public boolean addListener(final GameProgressListener listener) {
        return this.listeners.add(listener);
    }

    protected Collectible getCollectible(final Node n) {
        return this.collectiblesByNode.get(n);
    }

    protected List<Action> getDecisionRecord(final Player p) {
        final LinkedList<Action> moves = new LinkedList<Action>();
        for (final SortedMap.Entry<Integer, Action> entry : this.decisionRecord.get(p).entrySet()) {
            moves.add(entry.getKey(), entry.getValue());
        }
        return moves;
    }

    protected int getPlayerLength(final Player p) {
        if (!this.lengths.containsKey(p)) {
            throw new IllegalStateException("Player doesn't have any length assigned: " + p);
        }
        return this.lengths.get(p);
    }

    protected Deque<Node> getPlayerPosition(final Player p) {
        if (!this.positions.containsKey(p)) {
            throw new IllegalStateException("Player doesn't have any position assigned: " + p);
        }
        return this.positions.get(p);
    }

    @Override
    public GameProgressListener getReport() {
        return this.reporter;
    }

    /**
     * Decide which {@link Collectible}s should be considered collected by which
     * worms.
     * 
     * @param players
     *            Players still in the game.
     * @return Which collectible is collected by which player.
     */
    protected abstract Map<Collectible, Player> performCollectibleCollection(final Collection<Player> players);

    /**
     * Decide which new {@link Collectible}s should be distributed.
     * 
     * @param gameConfig
     *            Game config with information about the {@link Collectible} types.
     * @param playground
     *            Playground on which to distribute.
     * @param players
     *            Players still in the game.
     * @param currentTurnNumber
     *            Current turn number.
     * @return Which collectibles should be put where.
     */
    protected abstract Collection<Collectible> performCollectibleDistribution(final GameProperties gameConfig,
            final Playground playground, final Collection<Player> players, final int currentTurnNumber);

    /**
     * Perform collision detection for worms.
     * 
     * @param playground
     *            Playground on which to detect collisions.
     * @param currentPlayers
     *            Players still in the game.
     * @return Which players should be considered crashed.
     */
    protected abstract Set<Player> performCollisionDetection(final Playground playground,
            final Collection<Player> currentPlayers);

    /**
     * Decide which worms should be considered inactive.
     * 
     * @param currentPlayers
     *            Players still in the game.
     * @param currentTurnNumber
     *            Current turn number.
     * @param allowedInactiveTurns
     *            How many turns a player can not move before considered
     *            inactive.
     * @return Which players should be considered inactive.
     */
    protected abstract Set<Player> performInactivityDetection(final Collection<Player> currentPlayers,
            final int currentTurnNumber, final int allowedInactiveTurns);

    /**
     * Decide where the worm should be after it has performed a particular action.
     * 
     * @param player
     *            The worm.
     * @param playground
     *            Playground on which the move is happening.
     * @param decision
     *            The action to perform.
     * @return New positions for the worm, head-first.
     */
    protected abstract Deque<Node> performPlayerAction(final Player player, final Playground playground, final Action decision);

    /**
     * Decide which players should be rewarded for survival in this round.
     * 
     * @param allPlayers
     *            All the players that ever were in the game.
     * @param survivingPlayers
     *            Players that remain in the game.
     * @param removedInThisRound
     *            Number of players removed in this round.
     * @param rewardAmount
     *            How many points to award.
     * @return How much each player should be rewarded. Players not mentioned
     *         are not rewarded.
     */
    protected abstract Map<Player, Integer> performSurvivalRewarding(Collection<Player> allPlayers,
            Collection<Player> survivingPlayers, int removedInThisRound, int rewardAmount);

    @Override
    public Map<Player, Integer> play(final Playground playground, final Collection<Player> players,
            final File reportFolder) {
        if (this.gameConfig == null) {
            throw new IllegalStateException("Game context had not been set!");
        }
        // make sure a game isn't played more than once
        if (this.played.get()) {
            throw new IllegalStateException("This game had already been played.");
        }
        this.played.set(true);
        // prepare the playground
        final int wormLength = this.gameConfig.getStartingWormLength();
        final int allowedInactiveTurns = this.gameConfig.getMaximumInactiveTurns();
        final int allowedTurns = this.gameConfig.getMaximumTurns();
        final int wormSurvivalBonus = this.gameConfig.getDeadWormBonus();
        final int wormTimeout = this.gameConfig.getStrategyTimeoutInSeconds();
        // prepare players and their starting positions
        final List<Node> startingPositions = playground.getStartingPositions();
        final int playersSupported = startingPositions.size();
        final int playersAvailable = players.size();
        if (playersSupported < playersAvailable) {
            throw new IllegalArgumentException("The playground doesn't support " + playersAvailable + " players, only "
                    + playersSupported + "! ");
        }
        int i = 0;
        for (final Player player : players) {
            final Deque<Node> pos = new LinkedList<Node>();
            pos.push(startingPositions.get(i));
            this.setPlayerPosition(player, pos);
            this.setPlayerLength(player, wormLength);
            playerPoints.put(player, 0);
            GameController.LOGGER.info("Player {} assigned position {}.", player.getName(), i);
            i++;
        }
        // prepare situation
        this.reporter = new XmlProgressListener(playground, players, this.gameConfig);
        final CommandDistributor playerControl = new CommandDistributor(playground, players, this.reporter,
                this.gameConfig, reportFolder, wormTimeout);
        for (final GameProgressListener listener : this.listeners) {
            playerControl.addListener(listener);
        }
        Map<Player, Action> decisions = new HashMap<Player, Action>();
        for (final Player p : playerControl.getPlayers()) {
            // initialize players
            decisions.put(p, Action.NOTHING);
        }
        // start the game
        int turnNumber = GameProperties.FIRST_TURN_NUMBER;
        do {
            GameController.LOGGER.info("--- Starting turn no. {}.", turnNumber);
            final int preRemoval = playerControl.getPlayers().size();
            // remove inactive worms
            for (final Player player : this.performInactivityDetection(playerControl.getPlayers(), turnNumber,
                    allowedInactiveTurns)) {
                GameController.LOGGER.info("Player {} will be removed for inactivity.", player.getName());
                playerControl.distributeCommand(new DeactivatePlayerCommand(player));
            }
            // move the worms
            for (final Player p : playerControl.getPlayers()) {
                final Action m = decisions.get(p);
                this.addDecision(p, m, turnNumber);
                final Deque<Node> newPosition = this.performPlayerAction(p, playground, m);
                this.setPlayerPosition(p, newPosition);
                playerControl.distributeCommand(new PlayerActionCommand(p, m, newPosition));
            }
            // resolve worms colliding
            for (final Player player : this.performCollisionDetection(playground, playerControl.getPlayers())) {
                playerControl.distributeCommand(new CrashPlayerCommand(player));
            }
            final int postRemoval = playerControl.getPlayers().size();
            for (final Map.Entry<Player, Integer> entry : this.performSurvivalRewarding(players,
                    playerControl.getPlayers(), preRemoval - postRemoval, wormSurvivalBonus).entrySet()) {
                final Player p = entry.getKey();
                final int amount = entry.getValue();
                this.reward(p, amount);
                playerControl.distributeCommand(new RewardSurvivalCommand(p, amount));
            }
            // expire uncollected collectibles
            final Set<Collectible> removeCollectibles = new HashSet<Collectible>();
            for (final Collectible c : this.collectiblesByNode.values()) {
                if (c.expires() && turnNumber >= c.expiresInTurn()) {
                    removeCollectibles.add(c);
                }
            }
            for (final Collectible c : removeCollectibles) {
                playerControl.distributeCommand(new RemoveCollectibleCommand(c));
                this.removeCollectible(c);
            }
            // add points for collected collectibles
            for (final Map.Entry<Collectible, Player> entry : this.performCollectibleCollection(
                    playerControl.getPlayers()).entrySet()) {
                final Collectible c = entry.getKey();
                final Player p = entry.getValue();
                this.reward(p, c.getPoints());
                playerControl.distributeCommand(new CollectCollectibleCommand(c, p));
                this.removeCollectible(c);
                this.setPlayerLength(p, this.getPlayerLength(p) + 1);
            }
            // distribute new collectibles
            for (final Collectible c : this.performCollectibleDistribution(this.gameConfig, playground,
                    playerControl.getPlayers(), turnNumber)) {
                this.addCollectible(c);
                playerControl.distributeCommand(new AddCollectibleCommand(c));
            }
            // make the move decision
            decisions = playerControl.execute();
            turnNumber++;
            if (turnNumber == allowedTurns) {
                GameController.LOGGER.info("Reached a pre-defined limit of {} turns. Terminating game.", allowedTurns);
                break;
            } else if (playerControl.getPlayers().size() < 2) {
                GameController.LOGGER.info("There are no more players. Terminating game.");
                break;
            }
        } while (true);
        playerControl.terminate(); // clean up all the sessions
        // output player status
        GameController.LOGGER.info("--- Game over.");
        for (final Map.Entry<Player, Integer> entry : this.playerPoints.entrySet()) {
            GameController.LOGGER.info("Player {} earned {} points.", entry.getKey().getName(), entry.getValue());
        }
        return Collections.unmodifiableMap(this.playerPoints);
    }

    private void removeCollectible(final Collectible c) {
        this.collectiblesByNode.remove(c.getAt());
    }

    @Override
    public boolean removeListener(final GameProgressListener listener) {
        return this.listeners.remove(listener);
    }

    private void reward(final Player p, final int points) {
        this.playerPoints.put(p, this.playerPoints.get(p) + points);
    }

    /**
     * 
     */
    @Override
    public void setContext(final InputStream context) {
        try {
            this.gameConfig = GameProperties.read(context);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Failed reading game properties.");
        }
    }

    private void setPlayerLength(final Player p, final int length) {
        this.lengths.put(p, length);
    }

    private void setPlayerPosition(final Player p, final Deque<Node> position) {
        this.positions.put(p, position);
    }

    /**
     * Build the playground from an input stream. Each line in that stream
     * represents one row on the playground. Each '#' in that line represents a
     * wall node, ' ' represents a node where the worm can move, '@' represents
     * a possible starting position for a worm. (Starting positions also can be
     * moved into.) Any other sign, other than a line break, will result in an
     * exception.
     * 
     * @param name
     *            Name for the new playground.
     * @param source
     *            Stream in question.
     * @return Playground constructed from that stream.
     */
    @Override
    public Playground buildPlayground(final String name, final InputStream source) {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(source))) {
            final List<String> lines = reader.lines().collect(Collectors.toList());
            Collections.reverse(lines); // this way, 0,0 is bottom left
            return new DefaultPlayground(name, lines);
        } catch (final Exception ex) {
            throw new IllegalStateException("Cannot read playground " + name, ex);
        }
    }

}
