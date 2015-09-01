package org.drooms.impl.logic.commands;

import org.drooms.api.Collectible;
import org.drooms.api.GameProgressListener;
import org.drooms.impl.logic.CollectibleRelated;
import org.drooms.impl.logic.PlayerLogic;
import org.drooms.impl.logic.events.CollectibleRemovalEvent;

public class RemoveCollectibleCommand implements Command, CollectibleRelated {

    private final Collectible toRemove;
    private final CollectibleRemovalEvent event;

    public RemoveCollectibleCommand(final Collectible c) {
        this.toRemove = c;
        this.event = new CollectibleRemovalEvent(c);
    }

    @Override
    public Collectible getCollectible() {
        return this.toRemove;
    }

    @Override
    public void perform(final PlayerLogic logic) {
        logic.notifyOfCollectibleRemoval(this.event);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.collectibleRemoved(this.toRemove);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RemoveCollectibleCommand [toRemove=")
                .append(this.toRemove).append("]");
        return builder.toString();
    }

}
