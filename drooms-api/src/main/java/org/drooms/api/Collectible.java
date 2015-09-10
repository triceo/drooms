package org.drooms.api;

/**
 * Represents an item that occasionally pops up on the {@link Playground}. When
 * a worm's head reaches this item, the {@link Player} in question will be
 * rewarded by a specific amount of points.
 * 
 * No two instances ever {@link #equals(Object)}.
 */
public class Collectible {

    private final int expiresInTurn, points;
    private final Node at;

    
    public Node getAt() {
        return at;
    }

    /**
     * Construct an item that never expires.
     * 
     * @param n
     *            Where the collectible is located.
     * @param points
     *            How many points to award. Must be greater than 0.
     */
    public Collectible(final Node n, final int points) {
        if (points <= 0) {
            throw new IllegalArgumentException(
                    "Collectible must have a positive amount of points.");
        }
        this.expiresInTurn = -1;
        this.points = points;
        this.at = n;
    }

    /**
     * Construct the item.
     * 
     * @param n
     *            Where the collectible is located.
     * @param points
     *            How many points to award. Must be greater than 0.
     * @param expiresInTurn
     *            At which turn the item expires. Must be greater than 0.
     */
    public Collectible(final Node n, final int points, final int expiresInTurn) {
        if (expiresInTurn <= 0) {
            throw new IllegalArgumentException(
                    "Expiration must be a positive number.");
        } else if (points <= 0) {
            throw new IllegalArgumentException(
                    "Collectible must have a positive amount of points.");
        }
        this.expiresInTurn = expiresInTurn;
        this.points = points;
        this.at = n;
    }

    /**
     * Whether or not this item will disappear from the {@link Playground} at
     * some point in time.
     * 
     * @return True if the item will disappear, false if it stays indefinitely.
     */
    public boolean expires() {
        return this.expiresInTurn >= 0;
    }

    /**
     * The number of the turn at the beginning of which this item will disappear
     * from the {@link Playground}.
     * 
     * @return A number greater than or equal to when {@link #expires()} is true, -1 otherwise.
     */
    public int expiresInTurn() {
        return this.expiresInTurn;
    }

    /**
     * Number of points to award to {@link Player} in case a worm successfully
     * reaches the item.
     * 
     * @return A positive number.
     */
    public int getPoints() {
        return this.points;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Collectible [expiresInTurn=")
                .append(this.expiresInTurn).append(", points=")
                .append(this.points).append("]");
        return builder.toString();
    }

}
