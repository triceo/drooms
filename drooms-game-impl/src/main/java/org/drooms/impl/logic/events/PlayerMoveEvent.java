package org.drooms.impl.logic.events;

import java.util.Deque;

import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.logic.PlayerRelated;

public class PlayerMoveEvent implements PlayerRelated {

    private final Player player;
    private final Move move;
    private final Deque<Node> nodes;

    public PlayerMoveEvent(final Player p, final Move m, final Deque<Node> nodes) {
        this.player = p;
        this.nodes = nodes;
        this.move = m;
    }

    public Move getMove() {
        return this.move;
    }

    public Deque<Node> getNodes() {
        return this.nodes;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

}
