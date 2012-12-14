package org.drooms.impl.logic;

import org.drooms.api.Player;

/**
 * States that a certain event or a command is related to a {@link Player}'s
 * worm.
 */
public interface PlayerRelated {

    /**
     * To which {@link Player}'s worm is this related.
     * 
     * @return Player in question.
     */
    public Player getPlayer();

}
