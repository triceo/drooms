package org.drooms.impl.logic.events;

import java.util.Deque;

import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.logic.PlayerRelated;

public class PlayerMoveEvent<N extends Node> implements PlayerRelated {

    private final Player player;
    private final Move move;
    private final Deque<N> nodes;

    public PlayerMoveEvent(final Player p, final Move m, final Deque<N> nodes) {
        this.player = p;
        this.nodes = nodes;
        this.move = m;
    }

    public Move getMove() {
        return this.move;
    }

    public Deque<N> getNodes() {
        return this.nodes;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

}
