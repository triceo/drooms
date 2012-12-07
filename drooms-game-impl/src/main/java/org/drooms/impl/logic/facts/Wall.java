package org.drooms.impl.logic.facts;

import org.drooms.api.Node;

public class Wall implements Positioned<Node> {

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