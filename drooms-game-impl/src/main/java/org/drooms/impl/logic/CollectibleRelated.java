package org.drooms.impl.logic;

import org.drooms.api.Collectible;

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

}
