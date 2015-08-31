package org.drooms.impl.logic;

import org.drooms.api.Player;

/**
 * States that a certain event or a command is related to a {@link Player} reward expressed with points.
 */
public interface RewardRelated {

    /**
     * How many points the {@link Player} should be rewarded.
     * 
     * @return A number greater than 0.
     */
    public int getPoints();

}
