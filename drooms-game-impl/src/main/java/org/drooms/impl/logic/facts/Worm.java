package org.drooms.impl.logic.facts;

import org.drooms.api.Player;
import org.drooms.impl.DefaultNode;

public class Worm implements Positioned<DefaultNode> {

    private final Player player;

    private final DefaultNode node;

    public Worm(final Player p, final DefaultNode node) {
        this.player = p;
        this.node = node;
    }

    @Override
    public DefaultNode getNode() {
        return this.node;
    }

    public Player getPlayer() {
        return this.player;
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