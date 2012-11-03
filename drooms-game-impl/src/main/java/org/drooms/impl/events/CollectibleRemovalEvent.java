package org.drooms.impl.events;

import org.drooms.api.Collectible;

public class CollectibleRemovalEvent implements CollectibleEvent {

    private final Collectible collectible;

    public CollectibleRemovalEvent(final Collectible c) {
        this.collectible = c;
    }

    @Override
    public Collectible getCollectible() {
        return this.collectible;
    }

}
