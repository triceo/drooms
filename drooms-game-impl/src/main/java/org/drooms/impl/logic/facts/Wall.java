package org.drooms.impl.logic.facts;

import org.drooms.impl.DefaultNode;

public class Wall implements Positioned<DefaultNode> {

    private final DefaultNode node;

    public Wall(final DefaultNode node) {
        this.node = node;
    }

    @Override
    public DefaultNode getNode() {
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