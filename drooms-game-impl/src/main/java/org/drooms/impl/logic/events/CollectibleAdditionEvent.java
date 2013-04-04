package org.drooms.impl.logic.events;

import org.drooms.api.Collectible;
import org.drooms.impl.logic.CollectibleRelated;

public class CollectibleAdditionEvent implements CollectibleRelated {

    private final Collectible collectible;

    public CollectibleAdditionEvent(final Collectible c) {
        this.collectible = c;
    }

    @Override
    public Collectible getCollectible() {
        return this.collectible;
    }

}
