package org.drooms.impl.logic.commands;

import org.drooms.api.Collectible;
import org.drooms.api.GameReport;
import org.drooms.impl.DefaultEdge;
import org.drooms.impl.DefaultNode;
import org.drooms.impl.DefaultPlayground;
import org.drooms.impl.logic.CollectibleRelated;
import org.drooms.impl.logic.CommandDistributor;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.events.CollectibleAdditionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddCollectibleCommand implements
        Command<DefaultPlayground, DefaultNode, DefaultEdge>,
        CollectibleRelated {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(AddCollectibleCommand.class);

    private final Collectible toAdd;
    private final DefaultNode whereToAdd;
    private final CollectibleAdditionEvent<DefaultNode> event;

    public AddCollectibleCommand(final Collectible c, final DefaultNode n) {
        this.toAdd = c;
        this.whereToAdd = n;
        this.event = new CollectibleAdditionEvent<DefaultNode>(c, n);
    }

    @Override
    public Collectible getCollectible() {
        return this.toAdd;
    }

    @Override
    public boolean isValid(final CommandDistributor controller) {
        return !controller.hasCollectible(this.toAdd);
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfCollectibleAddition(this.event);
    }

    @Override
    public void report(
            final GameReport<DefaultPlayground, DefaultNode, DefaultEdge> report) {
        report.collectibleAdded(toAdd, whereToAdd);
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
