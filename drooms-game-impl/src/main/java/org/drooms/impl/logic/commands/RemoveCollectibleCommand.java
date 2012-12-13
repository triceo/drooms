package org.drooms.impl.logic.commands;

import org.drooms.api.Collectible;
import org.drooms.api.GameProgressListener;
import org.drooms.api.Node;
import org.drooms.impl.logic.CollectibleRelated;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.events.CollectibleRemovalEvent;

public class RemoveCollectibleCommand implements Command, CollectibleRelated {

    private final Collectible toRemove;
    private final CollectibleRemovalEvent event;
    private final Node node;

    public RemoveCollectibleCommand(final Collectible c, final Node n) {
        this.toRemove = c;
        this.event = new CollectibleRemovalEvent(c, n);
        this.node = n;
    }

    @Override
    public Collectible getCollectible() {
        return this.toRemove;
    }

    @Override
    public Node getNode() {
        return this.node;
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfCollectibleRemoval(this.event);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.collectibleRemoved(this.toRemove, this.getNode());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RemoveCollectibleCommand [toRemove=")
                .append(this.toRemove).append("]");
        return builder.toString();
    }

}
