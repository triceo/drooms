package org.drooms.impl.logic;

import org.drooms.api.Collectible;
import org.drooms.api.Node;

/**
 * States that a certain event or a command is related to a {@link Collectible}.
 */
public interface CollectibleRelated {

    /**
     * The collectible that this relates to.
     * 
     * @return Collectible in question.
     */
    public Collectible getCollectible();

    /**
     * The node where that collectible is placed. Will go away post-1.0, use {@link Collectible#getAt()}.
     * 
     * @return Node in question.
     */
    @Deprecated
    public Node getNode();

}
