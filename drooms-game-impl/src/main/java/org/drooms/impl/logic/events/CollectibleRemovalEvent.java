package org.drooms.impl.logic.events;

import org.drooms.api.Collectible;
import org.drooms.api.Node;
import org.drooms.impl.logic.CollectibleRelated;

public class CollectibleRemovalEvent implements CollectibleRelated {

    private final Collectible collectible;

    public CollectibleRemovalEvent(final Collectible c) {
        this.collectible = c;
    }

    @Override
    public Collectible getCollectible() {
        return this.collectible;
    }

    @Override
    public Node getNode() {
        return this.collectible.getAt();
    }

}
