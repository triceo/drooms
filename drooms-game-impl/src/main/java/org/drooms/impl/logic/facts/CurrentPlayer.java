package org.drooms.impl.logic.facts;

import org.drooms.api.Player;
import org.drooms.impl.DefaultNode;

public class CurrentPlayer implements Positioned<DefaultNode> {

    private final Player player;

    private DefaultNode node;

    public CurrentPlayer(final Player p, final DefaultNode node) {
        this.player = p;
        this.node = node;
    }

    public Player get() {
        return this.player;
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

    public void setNode(final DefaultNode node) {
        this.node = node;
    }
}