package org.drooms.impl.logic.commands;

import org.drooms.api.Collectible;
import org.drooms.api.GameProgressListener;
import org.drooms.api.Player;
import org.drooms.impl.logic.*;
import org.drooms.impl.logic.events.CollectibleRewardEvent;

public class CollectCollectibleCommand implements Command, PlayerRelated,
        CollectibleRelated, RewardRelated {

    private final Collectible toCollect;
    private final Player toReward;
    private final CollectibleRewardEvent event;

    public CollectCollectibleCommand(final Collectible c, final Player p) {
        this.toCollect = c;
        this.toReward = p;
        this.event = new CollectibleRewardEvent(p, c);
    }

    @Override
    public Collectible getCollectible() {
        return this.toCollect;
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
    public void perform(final PlayerLogic logic) {
        logic.notifyOfCollectibleReward(this.event);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.collectibleCollected(this.getCollectible(), this.getPlayer(), this.getPoints());
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
