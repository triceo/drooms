package org.drooms.impl.events;

import org.drooms.api.Collectible;
import org.drooms.api.Player;

public class CollectibleRewardEvent implements RewardEvent, PlayerEvent,
        CollectibleEvent {

    private final Player player;
    private final Collectible collectible;

    public CollectibleRewardEvent(final Player p, final Collectible c) {
        this.player = p;
        this.collectible = c;
    }

    @Override
    public Collectible getCollectible() {
        return this.collectible;
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
