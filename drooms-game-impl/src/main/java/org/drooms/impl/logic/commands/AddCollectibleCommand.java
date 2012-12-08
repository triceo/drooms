package org.drooms.impl.logic.commands;

import org.drooms.api.Collectible;
import org.drooms.api.GameReport;
import org.drooms.api.Node;
import org.drooms.impl.DefaultPlayground;
import org.drooms.impl.logic.CollectibleRelated;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.events.CollectibleAdditionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddCollectibleCommand implements Command<DefaultPlayground>,
        CollectibleRelated {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(AddCollectibleCommand.class);

    private final Collectible toAdd;
    private final Node whereToAdd;
    private final CollectibleAdditionEvent<Node> event;

    public AddCollectibleCommand(final Collectible c, final Node n) {
        this.toAdd = c;
        this.whereToAdd = n;
        this.event = new CollectibleAdditionEvent<Node>(c, n);
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
    public void report(final GameReport<DefaultPlayground> report) {
        report.collectibleAdded(this.toAdd, this.whereToAdd);
        AddCollectibleCommand.LOGGER.info("Collectible {} added at [{},{}].",
                new Object[] { this.toAdd, this.whereToAdd.getX(),
                        this.whereToAdd.getY() });
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AddCollectibleCommand [toAdd=").append(this.toAdd)
                .append(", whereToAdd=").append(this.whereToAdd).append("]");
        return builder.toString();
    }

}
