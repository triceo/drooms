package org.drooms.impl.logic.facts;

import org.drooms.api.Node;
import org.drooms.api.Player;

public class CurrentPlayer implements Positioned<Node> {

    private final Player player;

    private Node node;

    public CurrentPlayer(final Player p, final Node node) {
        this.player = p;
        this.node = node;
    }

    public Player get() {
        return this.player;
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

    public void setNode(final Node node) {
        this.node = node;
    }
}