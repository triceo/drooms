package org.drooms.api;

import org.drools.KnowledgeBase;

/**
 * Represents a worm in the {@link Game} on the {@link Playground}.
 */
public class Player {

    private final String name;
    private final KnowledgeBase kbase;

    /**
     * Create a player instance.
     * 
     * @param name
     *            Name of the player.
     * @param knowledgeBase
     *            Rules that implement the player's strategy.
     */
    public Player(final String name, final KnowledgeBase knowledgeBase) {
        if (name == null || knowledgeBase == null) {
            throw new IllegalArgumentException(
                    "Neither of the parameters can be null.");
        }
        this.name = name;
        this.kbase = knowledgeBase;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Player other = (Player) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /**
     * Retrieve the player's strategy.
     * 
     * @return The strategy.
     */
    public KnowledgeBase getKnowledgeBase() {
        return this.kbase;
    }

    /**
     * Retrieve the player's name.
     * 
     * @return The name.
     */
    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Player [name=").append(this.name).append("]");
        return builder.toString();
    }

}
