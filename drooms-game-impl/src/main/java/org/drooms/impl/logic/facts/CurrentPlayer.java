package org.drooms.impl.logic.facts;

import org.drooms.api.Node;
import org.drooms.api.Player;

/**
 * Represents type of fact to be inserted into the working memory, so that the
 * strategy has information about who and where the current player is.
 * 
 * FIXME this probably doesn't need to implement Positioned, since that
 * information can be easily inferred from events.
 */
public class CurrentPlayer implements Positioned {

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

    // TODO strategies shouldn't be allowed to call this
    public void setNode(final Node node) {
        this.node = node;
    }
}