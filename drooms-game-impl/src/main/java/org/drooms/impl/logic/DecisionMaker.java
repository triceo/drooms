package org.drooms.impl.logic;

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
import org.drooms.api.Player;
import org.drooms.impl.DefaultEdge;
import org.drooms.impl.DefaultNode;
import org.drooms.impl.DefaultPlayground;
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
    private final KnowledgeRuntimeLogger logger;
    private final boolean isDisposed = false;
    private final WorkingMemoryEntryPoint gameEvents, playerEvents;
    private Move latestDecision = null;
    private final FactHandle currentTurn;
    private final FactHandle currentPlayer;

    private final Map<Player, Map<DefaultNode, FactHandle>> handles = new HashMap<Player, Map<DefaultNode, FactHandle>>();

    public DecisionMaker(
            final Player p,
            final PathTracker<DefaultPlayground, DefaultNode, DefaultEdge> tracker) {
        this.player = p;
        this.session = p.getKnowledgeBase().newStatefulKnowledgeSession(
                DecisionMaker.config, DecisionMaker.environment);
        this.logger = KnowledgeRuntimeLoggerFactory.newFileLogger(this.session,
                "player-" + this.player.getName() + "-session");
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
        final DefaultPlayground playground = tracker.getPlayground();
        for (int x = -1; x <= playground.getWidth(); x++) {
            for (int y = -1; y <= playground.getHeight(); y++) {
                if (!playground.isAvailable(x, y)) {
                    this.session.insert(new Wall(DefaultNode.getNode(x, y)));
                }
            }
        }
        // insert info about the game status
        this.currentPlayer = this.session.insert(new CurrentPlayer(p,
                DefaultNode.getNode(0, 0)));
        this.currentTurn = this.session.insert(new CurrentTurn(0));
    }

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

    public boolean isTerminated() {
        return this.isDisposed;
    }

    public void notifyOfCollectibleAddition(
            final CollectibleAdditionEvent<DefaultNode> evt) {
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
        for (final Map.Entry<DefaultNode, FactHandle> entry : this.handles
                .remove(p).entrySet()) {
            this.session.retract(entry.getValue());
        }
    }

    public void notifyOfPlayerMove(final PlayerMoveEvent<DefaultNode> evt) {
        final DefaultNode newHead = evt.getNodes().getFirst();
        final Player p = evt.getPlayer();
        this.playerEvents.insert(evt);
        // update player positions
        if (!this.handles.containsKey(p)) {
            this.handles.put(p, new HashMap<DefaultNode, FactHandle>());
        }
        final Map<DefaultNode, FactHandle> playerHandles = this.handles.get(p);
        final Set<DefaultNode> untraversedNodes = new HashSet<DefaultNode>(
                playerHandles.keySet());
        for (final DefaultNode n : evt.getNodes()) {
            if (!playerHandles.containsKey(n)) { // worm occupies a new node
                final FactHandle fh = this.session.insert(new Worm(p, n));
                playerHandles.put(n, fh);
            }
            untraversedNodes.remove(n);
        }
        for (final DefaultNode n : untraversedNodes) { // worm no longer
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

    public boolean terminate() {
        if (this.isDisposed) {
            DecisionMaker.LOGGER.warn("Player {} already terminated.",
                    new Object[] { this.player.getName() });
            return false;
        } else {
            DecisionMaker.LOGGER.info("Terminating player {}.",
                    new Object[] { this.player.getName() });
            this.session.dispose();
            this.logger.close();
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
