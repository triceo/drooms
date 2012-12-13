package org.drooms.impl.logic.commands;

import org.drooms.api.GameProgressListener;
import org.drooms.api.Player;

public class CrashPlayerCommand extends DeactivatePlayerCommand {

    public CrashPlayerCommand(final Player p) {
        super(p);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.playerCrashed(this.getPlayer());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CrashPlayerCommand [toDie=").append(this.getPlayer())
                .append("]");
        return builder.toString();
    }

}
