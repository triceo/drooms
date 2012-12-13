package org.drooms.impl.logic;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.drools.KnowledgeBaseFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.Channel;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.drools.time.SessionPseudoClock;
import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.api.Strategy;
import org.drooms.impl.logic.events.CollectibleAdditionEvent;
import org.drooms.impl.logic.events.CollectibleRemovalEvent;
import org.drooms.impl.logic.events.CollectibleRewardEvent;
import org.drooms.impl.logic.events.PlayerDeathEvent;
import org.drooms.impl.logic.events.PlayerMoveEvent;
import org.drooms.impl.logic.events.SurvivalRewardEvent;
import org.drooms.impl.logic.facts.CurrentPlayer;
import org.drooms.impl.logic.facts.CurrentTurn;
import org.drooms.impl.logic.facts.Wall;
import org.drooms.impl.logic.facts.Worm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a {@link Player}'s {@link Strategy} in action. This class holds
 * and maintains Drools engine's state for each particular player.
 * 
 * <p>
 * When asked (see {@link #decideNextMove()}), the strategy should make a
 * decision on the next move, based on the current state of the working memory.
 * This decision should be sent over the provided 'decision' channel. If not
 * sent, it will default to STAY. See @{link Move} for the various types of
 * decisions.
 * </p>
 * <p>
 * This class enforces the following requirements on the strategies:
 * </p>
 * 
 * <ul>
 * <li>'gameEvents' entry point must be declared, where the events not directly
 * related to player actions will be sent. These events are
 * {@link CollectibleAdditionEvent}, {@link CollectibleRemovalEvent},
 * {@link CollectibleRewardEvent} and {@link SurvivalRewardEvent}.</li>
 * <li>'playerEvents' entry point must be declared, where the player-caused
 * events will be sent. These events are {@link PlayerMoveEvent} and
 * {@link PlayerDeathEvent}.</li>
 * </ul>
 * 
 * <p>
 * This class provides the following Drools globals, if declared in the
 * strategy:
 * </p>
 * 
 * <ul>
 * <li>'logger' implementation of the {@link Logger} interface, to use for
 * logging from within the rules.</li>
 * <li>'tracker' instance of the {@link PathTracker}, to facilitate path-finding
 * in the rules.</li>
 * </ul>
 * 
 * <p>
 * The working memory will contain instances of the various helper fact types:
 * </p>
 * 
 * <ul>
 * <li>{@link CurrentPlayer}, once. Will change with every turn.</li>
 * <li>{@link CurrentTurn}, once. Will change with every turn.</li>
 * <li>{@link Wall}, many. Will remain constant over the whole game.</li>
 * <li>{@link Worm}, many. Will be added and removed as the worms will move, but
 * never modified.</li>
 * </ul>
 * 
 */
// FIXME rewards should have their own entry point, don't forget to update doc
public class DecisionMaker implements Channel {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DecisionMaker.class);

    // initialize the shared knowledge session config
    private static final KnowledgeSessionConfiguration config;
    static {
        config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        DecisionMaker.config.setOption(ClockTypeOption.get("pseudo"));
    }

    // initialize the shared environment
    private static final Environment environment = KnowledgeBaseFactory
            .newEnvironment();

    private final Player player;
    private final StatefulKnowledgeSession session;
    private final KnowledgeRuntimeLogger sessionAudit;
    private final boolean isDisposed = false;
    private final WorkingMemoryEntryPoint gameEvents, playerEvents;
    private Move latestDecision = null;
    private final FactHandle currentTurn;
    private final FactHandle currentPlayer;

    private final Map<Player, Map<Node, FactHandle>> handles = new HashMap<Player, Map<Node, FactHandle>>();

    // FIXME separate the strategy validation into its own helper class
    public DecisionMaker(final Player p, final PathTracker tracker,
            final File reportFolder) {
        this.player = p;
        this.session = p.getKnowledgeBase().newStatefulKnowledgeSession(
                DecisionMaker.config, DecisionMaker.environment);
        this.sessionAudit = KnowledgeRuntimeLoggerFactory.newFileLogger(
                this.session, reportFolder.getAbsolutePath() + File.separator
                        + "player-" + this.player.getName() + "-session");
        // this is where we listen for decisions
        this.session.registerChannel("decision", this);
        // this is where the path tracker comes in
        try {
            this.session.setGlobal("tracker", tracker);
        } catch (final RuntimeException ex) {
            DecisionMaker.LOGGER.info(
                    "Player {} doesn't use a path tracker. Good luck! :-)",
                    this.player.getName());
        }
        // this is where the logger comes in
        try {
            this.session.setGlobal(
                    "logger",
                    LoggerFactory.getLogger("org.drooms.players."
                            + this.player.getName()));
        } catch (final RuntimeException ex) {
            DecisionMaker.LOGGER.info("Player {} doesn't use a logger.",
                    this.player.getName());
        }
        // this is where we will send events from the game
        this.gameEvents = this.session.getWorkingMemoryEntryPoint("gameEvents");
        if (this.gameEvents == null) {
            throw new IllegalStateException(
                    "Problem in your rule file: 'gameEvents' entry point not declared.");
        }
        this.playerEvents = this.session
                .getWorkingMemoryEntryPoint("playerEvents");
        if (this.playerEvents == null) {
            throw new IllegalStateException(
                    "Problem in your rule file: 'playerEvents' entry point not declared.");
        }
        /*
         * insert playground walls; make sure the playground is always
         * surrounded with walls.
         */
        final Playground playground = tracker.getPlayground();
        for (int x = -1; x <= playground.getWidth(); x++) {
            for (int y = -1; y <= playground.getHeight(); y++) {
                if (!playground.isAvailable(x, y)) {
                    this.session.insert(new Wall(Node.getNode(x, y)));
                }
            }
        }
        // insert info about the game status
        this.currentPlayer = this.session.insert(new CurrentPlayer(p, Node
                .getNode(0, 0)));
        this.currentTurn = this.session.insert(new CurrentTurn(0));
    }

    /**
     * Call on the Drools engine to make the decision on worm's next move,
     * according to the {@link Player}'s {@link Strategy}.
     * 
     * @return The move. STAY will be chosen when the strategy doesn't respond.
     */
    public Move decideNextMove() {
        this.validate();
        DecisionMaker.LOGGER.trace("Player {} advancing time. ",
                new Object[] { this.player.getName() });
        final SessionPseudoClock clock = this.session.getSessionClock();
        clock.advanceTime(1, TimeUnit.MINUTES);
        // decide
        DecisionMaker.LOGGER.trace("Player {} deciding. ",
                new Object[] { this.player.getName() });
        this.latestDecision = null;
        this.session.fireAllRules();
        // increase turn number
        final CurrentTurn turn = (CurrentTurn) this.session
                .getObject(this.currentTurn);
        turn.setNumber(turn.getNumber() + 1);
        this.session.update(this.currentTurn, turn);
        // store the decision
        if (this.latestDecision == null) {
            DecisionMaker.LOGGER.warn(
                    "Player {} didn't make a decision. STAY forced.",
                    this.player.getName());
            return Move.STAY;
        } else {
            DecisionMaker.LOGGER.info("Player {} final decision is {}. ",
                    this.player.getName(), this.latestDecision);
            return this.latestDecision;
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    /**
     * Whether or not this object can still be used for decision making.
     * 
     * @return False when it can be used.
     */
    public boolean isTerminated() {
        return this.isDisposed;
    }

    public void notifyOfCollectibleAddition(final CollectibleAdditionEvent evt) {
        this.gameEvents.insert(evt);
    }

    public void notifyOfCollectibleRemoval(final CollectibleRemovalEvent evt) {
        this.gameEvents.insert(evt);
    }

    public void notifyOfCollectibleReward(final CollectibleRewardEvent evt) {
        this.gameEvents.insert(evt);
    }

    public void notifyOfDeath(final PlayerDeathEvent evt) {
        this.playerEvents.insert(evt);
        final Player p = evt.getPlayer();
        // remove player from the WM
        for (final Map.Entry<Node, FactHandle> entry : this.handles.remove(p)
                .entrySet()) {
            this.session.retract(entry.getValue());
        }
    }

    public void notifyOfPlayerMove(final PlayerMoveEvent evt) {
        final Node newHead = evt.getNodes().getFirst();
        final Player p = evt.getPlayer();
        this.playerEvents.insert(evt);
        // update player positions
        if (!this.handles.containsKey(p)) {
            this.handles.put(p, new HashMap<Node, FactHandle>());
        }
        final Map<Node, FactHandle> playerHandles = this.handles.get(p);
        final Set<Node> untraversedNodes = new HashSet<Node>(
                playerHandles.keySet());
        for (final Node n : evt.getNodes()) {
            if (!playerHandles.containsKey(n)) { // worm occupies a new node
                final FactHandle fh = this.session.insert(new Worm(p, n));
                playerHandles.put(n, fh);
            }
            untraversedNodes.remove(n);
        }
        for (final Node n : untraversedNodes) { // worm no longer
                                                // occupies a node
            final FactHandle fh = playerHandles.remove(n);
            this.session.retract(fh);
        }
        // update current player's head
        if (p == this.getPlayer()) {
            final CurrentPlayer cp = (CurrentPlayer) this.session
                    .getObject(this.currentPlayer);
            cp.setNode(newHead);
            this.session.update(this.currentPlayer, cp);
        }
    }

    public void notifyOfSurvivalReward(final SurvivalRewardEvent evt) {
        this.gameEvents.insert(evt);
    }

    @Override
    public void send(final Object object) {
        this.validate();
        if (object instanceof Move) {
            if (this.latestDecision != null) {
                DecisionMaker.LOGGER.debug(
                        "Player {} has changed the decision from {} to {}.",
                        new Object[] { this.player.getName(),
                                this.latestDecision, object });
            }
            this.latestDecision = (Move) object;
        } else {
            DecisionMaker.LOGGER.warn(
                    "Player {} indicated an invalid move {}.", new Object[] {
                            this.player.getName(), this.latestDecision });
        }
    }

    /**
     * Clean up after this Drools instance. Will terminate the session and leave
     * all the objects up for garbage collection. Only call once and then don't
     * use this object anymore.
     * 
     * @return False if already terminated.
     */
    public boolean terminate() {
        if (this.isDisposed) {
            DecisionMaker.LOGGER.warn("Player {} already terminated.",
                    new Object[] { this.player.getName() });
            return false;
        } else {
            DecisionMaker.LOGGER.info("Terminating player {}.",
                    new Object[] { this.player.getName() });
            this.sessionAudit.close();
            this.session.dispose();
            return true;
        }
    }

    private void validate() {
        if (this.isDisposed) {
            throw new IllegalStateException("Player " + this.player.getName()
                    + " already terminated!");
        }
    }

}
