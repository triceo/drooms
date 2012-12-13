package org.drooms.impl.logic.facts;

import org.drooms.api.Node;
import org.drooms.api.Player;

/**
 * Represents type of fact to be inserted into the working memory, so that the
 * strategy has information about the position of worms.
 */
public class Worm implements Positioned {

    private final Player player;

    private final Node node;

    public Worm(final Player p, final Node node) {
        this.player = p;
        this.node = node;
    }

    @Override
    public Node getNode() {
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