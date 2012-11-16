package org.drooms.impl;

import java.util.Collection;
import java.util.HashMap;
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

    public class CurrentPlayer {

        private final Player player;

        public CurrentPlayer(final Player p) {
            this.player = p;
        }

        public Player get() {
            return this.player;
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

    public class PlayerPosition {

        private final Player player;

        private Collection<DefaultNode> position;

        public PlayerPosition(final Player p,
                final Collection<DefaultNode> position) {
            this.player = p;
            this.position = position;
        }

        public Player getPlayer() {
            return this.player;
        }

        public Collection<DefaultNode> getPosition() {
            return this.position;
        }

        public void setPosition(final Collection<DefaultNode> position) {
            this.position = position;
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

    private final Map<Player, FactHandle> playerPositions = new HashMap<Player, FactHandle>();

    public PlayerDecisionLogic(final Player p) {
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
        // FIXME somehow insert playing field into WM
        this.session.insert(new CurrentPlayer(p)); // make sure everyone knows
                                                   // the current player
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
        final FactHandle fh = this.playerPositions.remove(p);
        this.session.retract(fh);
    }

    public void notifyOfPlayerLengthChange(final Player p, final int length) {
        this.playerEvents.insert(new PlayerLengthChangeEvent(p, length));
    }

    public void notifyOfPlayerMove(final Player p, final Move m,
            final DefaultNode newHead) {
        this.playerEvents
                .insert(new PlayerMoveEvent<DefaultNode>(p, m, newHead));
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
            PlayerDecisionLogic.LOGGER.debug("Adding position for player {}.",
                    p.getName());
            final PlayerPosition pos = new PlayerPosition(p, positions);
            final FactHandle fh = this.session.insert(pos);
            this.playerPositions.put(p, fh);
            return false;
        } else {
            PlayerDecisionLogic.LOGGER.debug(
                    "Updating position for player {}.", p.getName());
            final FactHandle fh = this.playerPositions.get(p);
            final PlayerPosition pos = (PlayerPosition) this.session
                    .getObject(fh);
            pos.setPosition(positions);
            this.session.update(fh, pos);
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
