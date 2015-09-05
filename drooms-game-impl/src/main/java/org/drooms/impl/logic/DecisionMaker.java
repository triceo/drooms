package org.drooms.impl.logic;

import org.drools.core.time.SessionPseudoClock;
import org.drooms.api.Action;
import org.drooms.api.Node;
import org.drooms.api.Node.Type;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.logic.events.*;
import org.drooms.impl.logic.facts.*;
import org.drooms.impl.util.GameProperties;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a {@link Player}'s Strategy in action. This class holds
 * and maintains Drools engine's state for each particular player.
 * 
 * <p>
 * When submitted to an {@link java.util.concurrent.Executor}, the strategy should make a decision on the next move,
 * based on the current state of the working memory. This decision should be sent over the provided 'decision'
 * channel. If not sent, it will default to STAY. See {@link Action} for the various types of decisions.
 * </p>
 * <p>
 * This class enforces the following requirements on the strategies:
 * </p>
 * 
 * <ul>
 * <li>'gameEvents' entry point must be declared, where the events not directly related to player actions will be sent.
 * These events are {@link CollectibleAdditionEvent} and {@link CollectibleRemovalEvent}.</li>
 * <li>'playerEvents' entry point must be declared, where the player-caused events will be sent. These events are
 * {@link PlayerActionEvent} and {@link PlayerDeathEvent}.</li>
 * <li>'rewardEvents' entry point must be declared, where the reward events will be sent. These events are
 * {@link CollectibleRewardEvent} and {@link SurvivalRewardEvent}.</li>
 * </ul>
 * 
 * <p>
 * This class provides the following Drools globals, if declared in the strategy:
 * </p>
 * 
 * <ul>
 * <li>'logger' implementation of the {@link Logger} interface, to use for logging from within the rules.</li>
 * <li>'tracker' instance of the {@link PathTracker}, to facilitate path-finding in the rules.</li>
 * </ul>
 * 
 * <p>
 * Your strategies can be validated for all these - check {@link org.drooms.impl.util.DroomsStrategyValidator} and
 * feel free to use it in your unit testing.
 * </p>
 *
 * <p>
 * The working memory will contain instances of the various helper fact types:
 * </p>
 * 
 * <ul>
 * <li>{@link GameProperty}, many. Will never change or be removed.</li>
 * <li>{@link CurrentPlayer}, once. Will never change or be removed.</li>
 * <li>{@link CurrentTurn}, once. Will change with every turn.</li>
 * <li>{@link Wall}, many. Will remain constant over the whole game.</li>
 * <li>{@link Worm}, many. Will be added and removed as the worms will move, but never modified.</li>
 * </ul>
 * 
 */
class DecisionMaker implements PlayerLogic, Channel, Callable<Action> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionMaker.class);

    private static void setGlobal(final KieSession session, final String global, final Object value) {
        try {
            session.setGlobal(global, value);
        } catch (final RuntimeException ex) {
            // do nothing, since the user has already been notified
        }
    }

    private final FactHandle currentTurn;
    private final EntryPoint gameEvents, playerEvents, rewardEvents;
    private final Map<Player, Map<Node, FactHandle>> handles = new HashMap<>();
    private final boolean isDisposed = false;
    private final Player player;
    private final PathTracker tracker;
    private final KieSession session;
    private final KieRuntimeLogger sessionAudit;

    private Action latestDecision = null;
    private Node currentHead = null;

    public DecisionMaker(final Playground playground, final Player p, final GameProperties properties, final File
            reportFolder) {
        this.player = p;
        final KieSessionConfiguration config = KieServices.Factory.get().newKieSessionConfiguration();
        config.setOption(ClockTypeOption.get("pseudo"));
        this.session = p.constructKieBase().newKieSession(config, null);
        if (reportFolder != null) {
            Path reportFile = Paths.get(reportFolder.getPath(), p.getName());
            this.sessionAudit = KieServices.Factory.get().getLoggers().newFileLogger(session, reportFile.toString());
            DecisionMaker.LOGGER.info("Auditing the Drools session is enabled.");
        } else {
            this.sessionAudit = null;
            DecisionMaker.LOGGER.info("Auditing the Drools session is disabled.");
        }
        // this is where we listen for decisions
        this.session.registerChannel("decision", this);
        // this is where we will send events from the game
        this.rewardEvents = this.session.getEntryPoint("rewardEvents");
        this.gameEvents = this.session.getEntryPoint("gameEvents");
        this.playerEvents = this.session.getEntryPoint("playerEvents");
        // configure the globals for the session
        this.tracker = new PathTracker(playground, p);
        DecisionMaker.setGlobal(this.session, "tracker", tracker);
        DecisionMaker.setGlobal(this.session, "logger",
                LoggerFactory.getLogger("org.drooms.players." + p.getName()));
        // insert playground walls
        for (int x = -1; x <= playground.getWidth(); x++) {
            for (int y = -1; y <= playground.getHeight(); y++) {
                Node n = playground.getNodeAt(x, y);
                if (n.getType() == Type.WALL) {
                    this.session.insert(new Wall(n));
                }
            }
        }
        // insert info about the game configuration
        this.session.insert(new GameProperty(GameProperty.Name.MAX_TURNS, properties.getMaximumTurns()));
        this.session
                .insert(new GameProperty(GameProperty.Name.MAX_INACTIVE_TURNS, properties.getMaximumInactiveTurns()));
        this.session.insert(new GameProperty(GameProperty.Name.DEAD_WORM_BONUS, properties.getDeadWormBonus()));
        this.session.insert(new GameProperty(GameProperty.Name.TIMEOUT_IN_SECONDS, properties
                .getStrategyTimeoutInSeconds()));
        // insert info about the game status
        this.currentTurn = this.session.insert(new CurrentTurn(GameProperties.FIRST_TURN_NUMBER - 1));
        this.session.insert(new CurrentPlayer(p));
    }

    /**
     * Stop the decision-making process, no matter where it currently is.
     */
    public void halt() {
        this.session.halt();
    }

    @Override
    public void notifyOfCollectibleAddition(final CollectibleAdditionEvent evt) {
        this.gameEvents.insert(evt);
    }

    @Override
    public void notifyOfCollectibleRemoval(final CollectibleRemovalEvent evt) {
        this.gameEvents.insert(evt);
    }

    @Override
    public void notifyOfCollectibleReward(final CollectibleRewardEvent evt) {
        this.rewardEvents.insert(evt);
    }

    @Override
    public void notifyOfDeath(final PlayerDeathEvent evt) {
        this.playerEvents.insert(evt);
        // remove player from the WM
        this.handles.remove(evt.getPlayer()).forEach((node, handle) -> this.session.delete(handle));
    }

    @Override
    public void notifyOfPlayerMove(final PlayerActionEvent evt) {
        final Player player = evt.getPlayer();
        this.playerEvents.insert(evt);
        // update player positions
        if (!this.handles.containsKey(player)) {
            this.handles.put(player, new HashMap<>());
        }
        final Map<Node, FactHandle> handles = this.handles.get(player);
        // worm no longer occupies certain nodes
        handles.keySet().stream().filter(n -> !evt.getNodes().contains(n)).collect(Collectors.toSet()).forEach(n ->
                handles.remove(n));
        // worm occupies a new node
        evt.getNodes().stream().filter(n -> !handles.containsKey(n)).forEach(n -> handles.put(n, this.session.insert
                (new Worm(player, n))));
        // update head node
        if (player == this.player) {
            this.currentHead = evt.getHeadNode();
        }
    }

    @Override
    public void notifyOfSurvivalReward(final SurvivalRewardEvent evt) {
        this.rewardEvents.insert(evt);
    }

    @Override
    public void send(final Object object) {
        this.validate();
        if (object instanceof Action) {
            if (this.latestDecision != null) {
                DecisionMaker.LOGGER.debug("Player {} has changed the decision from {} to {}.", new Object[]{
                        this.player.getName(), this.latestDecision, object});
            }
            this.latestDecision = (Action) object;
        } else {
            DecisionMaker.LOGGER.warn("Player {} indicated an invalid move {}.", new Object[]{this.player.getName(),
                    this.latestDecision});
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
            DecisionMaker.LOGGER.warn("Player {} already terminated.", new Object[]{this.player.getName()});
            return false;
        } else {
            DecisionMaker.LOGGER.info("Terminating player {}.", new Object[]{this.player.getName()});
            if (this.sessionAudit != null) {
                this.sessionAudit.close();
            }
            this.halt();
            this.session.dispose();
            return true;
        }
    }

    /**
     * Signifies that this tracker has been notified of all events and that the immediately following action is
     * {@link #call()}.
     */
    public void commit() {
        this.validate();
        DecisionMaker.LOGGER.trace("Player {} updating path tracker. ", new Object[]{this.player.getName()});
        Map<Player, Collection<Node>> positions = handles.keySet().stream().collect(Collectors.toMap(Function.identity
                (), player -> handles.get(player).keySet()));
        this.tracker.updatePlayerPositions(positions, this.currentHead);
        DecisionMaker.LOGGER.trace("Player {} advancing time. ", new Object[]{this.player.getName()});
        final SessionPseudoClock clock = this.session.getSessionClock();
        clock.advanceTime(1, TimeUnit.MINUTES);
        // increase turn number
        final CurrentTurn turn = (CurrentTurn) this.session.getObject(this.currentTurn);
        this.session.update(this.currentTurn, new CurrentTurn(turn.getNumber() + 1));
    }

    private void validate() {
        if (this.isDisposed) {
            throw new IllegalStateException("Player " + this.player.getName() + " already terminated!");
        }
    }

    /**
     * Call on the Drools engine to make the decision on worm's next move,
     * according to the {@link Player}'s strategy.
     *
     * @return The move. STAY will be chosen when the strategy doesn't respond.
     */
    @Override
    public Action call() {
        DecisionMaker.LOGGER.trace("Player {} deciding. ", new Object[]{this.player.getName()});
        this.latestDecision = null;
        this.session.fireAllRules();
        if (this.latestDecision == null) {
            DecisionMaker.LOGGER.info("Player {} didn't make a decision. STAY forced.", this.player.getName());
            return Action.NOTHING;
        } else {
            DecisionMaker.LOGGER.info("Player {} final decision is {}. ", this.player.getName(), this.latestDecision);
            return this.latestDecision;
        }
    }
}
