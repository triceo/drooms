package org.drooms.impl.logic.facts;

import org.drooms.api.Node;

/**
 * Represents type of fact to be inserted into the working memory, so that the
 * strategy has information about where the walls are and where it can move.
 */
public class Wall implements Positioned {

    private final Node node;

    public Wall(final Node node) {
        this.node = node;
    }

    @Override
    public Node getNode() {
        return this.node;
    }

    @Override
    public int getX() {
        return this.node.getX();
    }

    @Override
    public int getY() {
        return this.node.getY();
    }
}