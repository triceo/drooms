package org.drooms.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    private final DefaultPlayground playground;
    private final Player player;
    private final StatefulKnowledgeSession session;
    private final boolean isDisposed = false;
    private final WorkingMemoryEntryPoint gameEvents, playerEvents;
    private Move latestDecision = null;
    private final FactHandle currentTurn;
    private final FactHandle currentPlayer;

    private final Map<Player, FactHandle[][]> playerPositions = new HashMap<Player, FactHandle[][]>();

    public PlayerDecisionLogic(final Player p,
            final DefaultPlayground playground) {
        this.playground = playground;
        this.player = p;
        this.session = p.getKnowledgeBase().newStatefulKnowledgeSession(
                PlayerDecisionLogic.config, PlayerDecisionLogic.environment);
        // this is where we listen for decisions
        this.session.registerChannel("decision", this);
        // this is where the logger comes in
        try {
            this.session.setGlobal("logger",
                    LoggerFactory.getLogger(this.player.getName() + "Player"));
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
        // insert playground walls
        for (int x = 0; x < playground.getWidth(); x++) {
            for (int y = 0; y < playground.getHeight(); y++) {
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
        final Move decision = (this.latestDecision == null ? Move.STAY
                : this.latestDecision);
        PlayerDecisionLogic.LOGGER.info("Player {} final decision is {}. ",
                new Object[] { this.player.getName(), decision });
        return decision;
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
        for (final FactHandle[] handles : this.playerPositions.remove(p)) {
            for (final FactHandle handle : handles) {
                if (handle == null) {
                    continue;
                }
                this.session.retract(handle);
            }
        }
    }

    public void notifyOfPlayerLengthChange(final Player p, final int length) {
        this.playerEvents.insert(new PlayerLengthChangeEvent(p, length));
    }

    public void notifyOfPlayerMove(final Player p, final Move m,
            final DefaultNode newHead) {
        this.playerEvents
                .insert(new PlayerMoveEvent<DefaultNode>(p, m, newHead));
        if (p == this.getPlayer()) {
            // update player head
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

    public boolean updatePlayerPosition(final Player p,
            final Collection<DefaultNode> positions) {
        if (!positions.contains(p)) {
            PlayerDecisionLogic.LOGGER.debug("Adding positions for player {}.",
                    p.getName());
            final FactHandle[][] handles = new FactHandle[this.playground
                    .getWidth()][this.playground.getHeight()];
            for (final DefaultNode n : positions) {
                final int x = n.getX();
                final int y = n.getY();
                handles[x][y] = this.session.insert(new Worm(p, x, y));
            }
            this.playerPositions.put(p, handles);
            return false;
        } else {
            PlayerDecisionLogic.LOGGER.debug(
                    "Updating position for player {}.", p.getName());
            final FactHandle[][] handles = this.playerPositions.get(p);
            final Collection<FactHandle> touchedHandles = new HashSet<FactHandle>();
            // add new nodes
            for (final DefaultNode n : positions) {
                final int x = n.getX();
                final int y = n.getY();
                if (handles[x][y] == null) {
                    handles[x][y] = this.session.insert(new Worm(p, x, y));
                }
                touchedHandles.add(handles[x][y]);
            }
            // remove old nodes
            for (final FactHandle[] handles2 : handles) {
                for (final FactHandle handle : handles2) {
                    if (touchedHandles.contains(handle)) {
                        continue;
                    }
                    final Worm w = (Worm) this.session.getObject(handle);
                    this.session.retract(handle);
                    handles[w.getX()][w.getY()] = null;
                }
            }
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
