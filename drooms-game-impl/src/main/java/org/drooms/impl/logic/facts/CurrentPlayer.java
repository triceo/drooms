package org.drooms.impl.logic.facts;

import org.drooms.api.Player;

/**
 * Represents type of fact to be inserted into the working memory, so that the
 * strategy has information about who the current player is.
 */
public class CurrentPlayer {

    private final Player player;

    public CurrentPlayer(final Player p) {
        this.player = p;
    }

    public Player getPlayer() {
        return this.player;
    }

}