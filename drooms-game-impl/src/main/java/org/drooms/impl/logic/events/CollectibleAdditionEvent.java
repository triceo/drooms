package org.drooms.impl.logic.events;

import org.drooms.api.Collectible;
import org.drooms.api.Node;
import org.drooms.impl.logic.CollectibleRelated;

public class CollectibleAdditionEvent<N extends Node> implements
        CollectibleRelated {

    private final N node;
    private final Collectible collectible;

    public CollectibleAdditionEvent(final Collectible c, final N n) {
        this.node = n;
        this.collectible = c;
    }

    @Override
    public Collectible getCollectible() {
        return this.collectible;
    }

    public N getNode() {
        return this.node;
    }

}
