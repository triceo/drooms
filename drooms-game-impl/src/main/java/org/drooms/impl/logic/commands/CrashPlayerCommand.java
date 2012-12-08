package org.drooms.impl.logic.commands;

import org.drooms.api.GameReport;
import org.drooms.api.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrashPlayerCommand extends DeactivatePlayerCommand {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CrashPlayerCommand.class);

    public CrashPlayerCommand(final Player p) {
        super(p);
    }

    @Override
    public void report(final GameReport report) {
        report.playerCrashed(this.getPlayer());
        CrashPlayerCommand.LOGGER.info(
                "Player {} crashed and has been removed from the game.", this
                        .getPlayer().getName());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CrashPlayerCommand [toDie=").append(this.getPlayer())
                .append("]");
        return builder.toString();
    }

}
