package org.drooms.impl.logic.commands;

import org.drooms.api.Collectible;
import org.drooms.api.GameProgressListener;
import org.drooms.impl.logic.CollectibleRelated;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.events.CollectibleAdditionEvent;

public class AddCollectibleCommand implements Command, CollectibleRelated {

    private final Collectible toAdd;
    private final CollectibleAdditionEvent event;

    public AddCollectibleCommand(final Collectible c) {
        this.toAdd = c;
        this.event = new CollectibleAdditionEvent(c);
    }

    @Override
    public Collectible getCollectible() {
        return this.toAdd;
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfCollectibleAddition(this.event);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.collectibleAdded(this.toAdd);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AddCollectibleCommand [toAdd=").append(this.toAdd).append("]");
        return builder.toString();
    }

}
