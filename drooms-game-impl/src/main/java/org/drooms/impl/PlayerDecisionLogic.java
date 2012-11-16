package org.drooms.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.drools.KnowledgeBaseFactory;
import org.drools.runtime.Channel;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.drools.time.SessionPseudoClock;
import org.drooms.api.Collectible;
import org.drooms.api.Move;
import org.drooms.api.Player;
import org.drooms.impl.events.CollectibleAdditionEvent;
import org.drooms.impl.events.CollectibleRemovalEvent;
import org.drooms.impl.events.CollectibleRewardEvent;
import org.drooms.impl.events.PlayerDeathEvent;
import org.drooms.impl.events.PlayerLengthChangeEvent;
import org.drooms.impl.events.PlayerMoveEvent;
import org.drooms.impl.events.SurvivalRewardEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerDecisionLogic implements Channel {

    public class CurrentPlayer implements Positioned {

        private final Player player;

        private int x, y;

        public CurrentPlayer(final Player p, final int x, final int y) {
            this.player = p;
            this.x = x;
            this.y = y;
        }

        public Player get() {
            return this.player;
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        public void setX(final int x) {
            this.x = x;
        }

        public void setY(final int y) {
            this.y = y;
        }
    }

    public class CurrentTurn {

        private int number;

        public CurrentTurn(final int number) {
            this.number = number;
        }

        public int getNumber() {
            return this.number;
        }

        public void setNumber(final int number) {
            this.number = number;
        }

    }

    public interface Positioned {

        public int getX();

        public int getY();

    }

    public class Wall implements Positioned {

        private final int x, y;

        public Wall(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

    }

    public class Worm implements Positioned {

        private final Player player;

        private final int x, y;

        public Worm(final Player p, final int x, final int y) {
            this.player = p;
            this.x = x;
            this.y = y;
        }

        public Player getPlayer() {
            return this.player;
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PlayerDecisionLogic.class);

    // initialize the shared knowledge session config
    private static final KnowledgeSessionConfiguration config;
    static {
        config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        PlayerDecisionLogic.config.setOption(ClockTypeOption.get("pseudo"));
    }

    // initialize the shared environment
    private static final Environment environment = KnowledgeBaseFactory
            .newEnvironment();

    private final Player player;
    private final StatefulKnowledgeSession session;
    private final boolean isDisposed = false;
    private final WorkingMemoryEntryPoint gameEvents, playerEvents;
    private Move latestDecision = null;
    private final FactHandle currentTurn;
    private final FactHandle currentPlayer;

    public PlayerDecisionLogic(final Player p,
            final DefaultPlayground playground) {
        this.player = p;
        this.session = p.getKnowledgeBase().newStatefulKnowledgeSession(
                PlayerDecisionLogic.config, PlayerDecisionLogic.environment);
        // this is where we listen for decisions
        this.session.registerChannel("decision", this);
        // this is where the logger comes in
        try {
            this.session.setGlobal("logger",
                    LoggerFactory.getLogger("org.drooms.players." + this.player.getName()));
        } catch (final RuntimeException ex) {
            PlayerDecisionLogic.LOGGER.info("Player {} doesn't use a logger.",
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
        // insert playground walls; make sure the playground is always surrounded with walls
        for (int x = -1; x <= playground.getWidth(); x++) {
            for (int y = -1; y <= playground.getHeight(); y++) {
                if (!playground.isAvailable(x, y)) {
                    this.session.insert(new Wall(x, y));
                }
            }
        }
        // insert info about the game status
        this.currentPlayer = this.session.insert(new CurrentPlayer(p, 0, 0));
        this.currentTurn = this.session.insert(new CurrentTurn(0));
    }

    public Move decideNextMove() {
        this.validate();
        PlayerDecisionLogic.LOGGER.trace("Player {} advancing time. ",
                new Object[] { this.player.getName() });
        final SessionPseudoClock clock = this.session.getSessionClock();
        clock.advanceTime(1, TimeUnit.MINUTES);
        // decide
        PlayerDecisionLogic.LOGGER.trace("Player {} deciding. ",
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
            PlayerDecisionLogic.LOGGER.warn("Player {} didn't make a decision. STAY forced.", this.player.getName());
            return Move.STAY;
        } else {
            PlayerDecisionLogic.LOGGER.info("Player {} final decision is {}. ", this.player.getName(), this.latestDecision);
            return this.latestDecision;
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isTerminated() {
        return this.isDisposed;
    }

    public void notifyOfCollectibleAddition(final Collectible c,
            final DefaultNode node) {
        this.gameEvents.insert(new CollectibleAdditionEvent<DefaultNode>(c,
                node));
    }

    public void notifyOfCollectibleRemoval(final Collectible c) {
        this.gameEvents.insert(new CollectibleRemovalEvent(c));
    }

    public void notifyOfCollectibleReward(final Collectible c, final Player p,
            final int points) {
        this.gameEvents.insert(new CollectibleRewardEvent(p, c));
    }

    public void notifyOfCollision(final Player p1, final Player p2) {
        this.notifyOfDeath(p1);
        this.notifyOfDeath(p2);
    }

    public void notifyOfDeath(final Player p) {
        this.playerEvents.insert(new PlayerDeathEvent(p));
        // remove player from the WM
        for (Map.Entry<DefaultNode, FactHandle> entry: handles.remove(p).entrySet()) {
            this.session.retract(entry.getValue());
        }
    }

    public void notifyOfPlayerLengthChange(final Player p, final int length) {
        this.playerEvents.insert(new PlayerLengthChangeEvent(p, length));
    }
    
    private final Map<Player, Map<DefaultNode, FactHandle>> handles = new HashMap<Player, Map<DefaultNode, FactHandle>>();

    public void notifyOfPlayerMove(final Player p, final Move m,
            final DefaultNode newHead, final Collection<DefaultNode> newPositions) {
        this.playerEvents
                .insert(new PlayerMoveEvent<DefaultNode>(p, m, newHead));
        // update player positions
        if (!handles.containsKey(p)) {
            handles.put(p, new HashMap<DefaultNode, FactHandle>());
        }
        Map<DefaultNode, FactHandle> playerHandles = handles.get(p);
        Set<DefaultNode> untraversedNodes = new HashSet<DefaultNode>(playerHandles.keySet());
        for (DefaultNode n: newPositions) {
            if (!playerHandles.containsKey(n)) { // worm occupies a new node
                FactHandle fh = this.session.insert(new Worm(p, n.getX(), n.getY()));
                playerHandles.put(n, fh);
            }
            untraversedNodes.remove(n);
        }
        for (DefaultNode n: untraversedNodes) { // worm no longer occupies a node
            FactHandle fh = playerHandles.remove(n);
            this.session.retract(fh);
        }
        // update current player's head
        if (p == this.getPlayer()) {
            final CurrentPlayer cp = (CurrentPlayer) this.session
                    .getObject(this.currentPlayer);
            cp.setX(newHead.getX());
            cp.setY(newHead.getY());
            this.session.update(this.currentPlayer, cp);
        }
    }

    public void notifyOfSurvivalReward(final Player p, final int points) {
        this.gameEvents.insert(new SurvivalRewardEvent(p, points));
    }

    @Override
    public void send(final Object object) {
        this.validate();
        if (object instanceof Move) {
            if (this.latestDecision != null) {
                PlayerDecisionLogic.LOGGER.debug(
                        "Player {} has changed the decision from {} to {}.",
                        new Object[] { this.player.getName(),
                                this.latestDecision, object });
            }
            this.latestDecision = (Move) object;
        } else {
            PlayerDecisionLogic.LOGGER.warn(
                    "Player {} indicated an invalid move {}.", new Object[] {
                            this.player.getName(), this.latestDecision });
        }
    }

    public boolean terminate() {
        if (this.isDisposed) {
            PlayerDecisionLogic.LOGGER.warn("Player {} already terminated.",
                    new Object[] { this.player.getName() });
            return false;
        } else {
            PlayerDecisionLogic.LOGGER.info("Terminating player {}.",
                    new Object[] { this.player.getName() });
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
