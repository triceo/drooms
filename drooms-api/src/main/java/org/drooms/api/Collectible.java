package org.drooms.api;

/**
 * Represents an item that occasionally pops up on the {@link Playground}. When
 * a worm's head reaches this item, the {@link Player} in question will be
 * rewarded by a specific amount of points.
 */
// FIXME this doesn't need to be an interface.
public interface Collectible {

    /**
     * Whether or not this item will disappear from the {@link Playground} at
     * some point in time.
     * 
     * @return True if the item will disappear, false if it stays indefinitely.
     */
    public boolean expires();

    /**
     * The number of the turn at the beginning of which this item will disappear
     * from the {@link Playground}.
     * 
     * @return A number >= 0 when {@link #expires()} is true, -1 otherwise.
     */
    public int expiresInTurn();

    /**
     * Number of points to award to {@link Player} in case a worm successfully
     * reaches the item.
     * 
     * @return A positive number.
     */
    public int getPoints();

}
