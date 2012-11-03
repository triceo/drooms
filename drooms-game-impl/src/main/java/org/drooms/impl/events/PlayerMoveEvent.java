package org.drooms.impl.events;

import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;

public class PlayerMoveEvent<N extends Node> implements PlayerEvent {

    private final Player player;
    private final Move move;
    private final N node;

    public PlayerMoveEvent(final Player p, final Move m, final N n) {
        this.player = p;
        this.node = n;
        this.move = m;
    }

    public Move getMove() {
        return this.move;
    }

    public N getNode() {
        return this.node;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

}
