package org.drooms.impl.logic.events;

import org.drooms.api.Collectible;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.logic.CollectibleRelated;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.RewardRelated;

public class CollectibleRewardEvent implements RewardRelated, PlayerRelated,
        CollectibleRelated {

    private final Player player;
    private final Collectible collectible;
    private final Node node;

    public CollectibleRewardEvent(final Player p, final Collectible c,
            final Node n) {
        this.player = p;
        this.collectible = c;
        this.node = n;
    }

    @Override
    public Collectible getCollectible() {
        return this.collectible;
    }

    @Override
    public Node getNode() {
        return this.node;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public int getPoints() {
        return this.collectible.getPoints();
    }

}
