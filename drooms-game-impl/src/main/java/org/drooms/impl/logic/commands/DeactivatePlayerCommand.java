package org.drooms.impl.logic.commands;

import org.drooms.api.GameProgressListener;
import org.drooms.api.Player;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.events.PlayerDeathEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeactivatePlayerCommand implements Command, PlayerRelated {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DeactivatePlayerCommand.class);

    private final Player toDie;
    private final PlayerDeathEvent event;

    public DeactivatePlayerCommand(final Player p) {
        this.toDie = p;
        this.event = new PlayerDeathEvent(p);
    }

    protected PlayerDeathEvent getEvent() {
        return this.event;
    }

    @Override
    public Player getPlayer() {
        return this.toDie;
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfDeath(this.getEvent());
    }

    @Override
    public void report(final GameProgressListener report) {
        report.playerDeactivated(this.toDie);
        DeactivatePlayerCommand.LOGGER.info(
                "Player {} has been removed from the game due to inactivity.",
                this.toDie.getName());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("DeactivatePlayerCommand [toDie=").append(this.toDie)
                .append("]");
        return builder.toString();
    }

}
