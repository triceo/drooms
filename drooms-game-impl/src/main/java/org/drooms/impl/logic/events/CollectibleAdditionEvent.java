package org.drooms.impl.logic.events;

import org.drooms.api.Collectible;
import org.drooms.api.Node;
import org.drooms.impl.logic.CollectibleRelated;

public class CollectibleAdditionEvent implements CollectibleRelated {

    private final Node node;
    private final Collectible collectible;

    public CollectibleAdditionEvent(final Collectible c, final Node n) {
        this.node = n;
        this.collectible = c;
    }

    @Override
    public Collectible getCollectible() {
        return this.collectible;
    }

    @Override
    public Node getNode() {
        return this.node;
    }

}
