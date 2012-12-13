package org.drooms.impl.logic.commands;

import org.drooms.api.Collectible;
import org.drooms.api.GameProgressListener;
import org.drooms.api.Node;
import org.drooms.impl.logic.CollectibleRelated;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.events.CollectibleAdditionEvent;

public class AddCollectibleCommand implements Command, CollectibleRelated {

    private final Collectible toAdd;
    private final Node whereToAdd;
    private final CollectibleAdditionEvent event;

    public AddCollectibleCommand(final Collectible c, final Node n) {
        this.toAdd = c;
        this.whereToAdd = n;
        this.event = new CollectibleAdditionEvent(c, n);
    }

    @Override
    public Collectible getCollectible() {
        return this.toAdd;
    }

    @Override
    public Node getNode() {
        return this.whereToAdd;
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfCollectibleAddition(this.event);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.collectibleAdded(this.toAdd, this.whereToAdd);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AddCollectibleCommand [toAdd=").append(this.toAdd)
                .append(", whereToAdd=").append(this.whereToAdd).append("]");
        return builder.toString();
    }

}
