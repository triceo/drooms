package org.drooms.impl.logic.commands;

import org.drooms.api.Collectible;
import org.drooms.api.GameProgressListener;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.logic.CollectibleRelated;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.RewardRelated;
import org.drooms.impl.logic.events.CollectibleRewardEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectCollectibleCommand implements Command, PlayerRelated,
        CollectibleRelated, RewardRelated {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CollectCollectibleCommand.class);

    private final Collectible toCollect;
    private final Player toReward;
    private final Node node;
    private final CollectibleRewardEvent event;

    public CollectCollectibleCommand(final Collectible c, final Player p,
            final Node n) {
        this.toCollect = c;
        this.toReward = p;
        this.event = new CollectibleRewardEvent(p, c, n);
        this.node = n;
    }

    @Override
    public Collectible getCollectible() {
        return this.toCollect;
    }

    @Override
    public Node getNode() {
        return this.node;
    }

    @Override
    public Player getPlayer() {
        return this.toReward;
    }

    @Override
    public int getPoints() {
        return this.toCollect.getPoints();
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfCollectibleReward(this.event);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.collectibleCollected(this.getCollectible(), this.getPlayer(),
                this.getNode(), this.getPoints());
        CollectCollectibleCommand.LOGGER.info(
                "Collectible {} collected by player {}.", this.toCollect,
                this.toReward);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CollectCollectibleCommand [toCollect=")
                .append(this.toCollect).append(", toReward=")
                .append(this.toReward).append("]");
        return builder.toString();
    }

}
