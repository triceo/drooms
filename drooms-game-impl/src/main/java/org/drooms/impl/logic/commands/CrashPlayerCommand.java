package org.drooms.impl.logic.commands;

import org.drooms.api.GameReport;
import org.drooms.api.Player;
import org.drooms.impl.DefaultEdge;
import org.drooms.impl.DefaultNode;
import org.drooms.impl.DefaultPlayground;
import org.drooms.impl.logic.CommandDistributor;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.events.PlayerDeathEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME investigate code share between CrashPlayerCommand and DeactivatePlayerCommand
public class CrashPlayerCommand implements
        Command<DefaultPlayground, DefaultNode, DefaultEdge>, PlayerRelated {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CrashPlayerCommand.class);

    private final Player toDie;
    private final PlayerDeathEvent event;

    public CrashPlayerCommand(final Player p) {
        this.toDie = p;
        this.event = new PlayerDeathEvent(p);
    }

    @Override
    public Player getPlayer() {
        return this.toDie;
    }

    @Override
    public boolean isValid(final CommandDistributor controller) {
        return controller.hasPlayer(this.toDie);
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfDeath(this.event);
    }

    @Override
    public void report(
            final GameReport<DefaultPlayground, DefaultNode, DefaultEdge> report) {
        report.crashPlayer(this.toDie);
        CrashPlayerCommand.LOGGER.info(
                "Player {} crashed and has been removed from the game.",
                this.toDie.getName());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CrashPlayerCommand [toDie=").append(this.toDie)
                .append("]");
        return builder.toString();
    }

}
