package org.drooms.impl.logic.events;

import java.util.Deque;

import org.drooms.api.Action;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.logic.PlayerRelated;

public class PlayerActionEvent implements PlayerRelated {

    private final Player player;
    private final Action action;
    private final Deque<Node> nodes;

    public PlayerActionEvent(final Player p, final Action a, final Deque<Node> nodes) {
        this.player = p;
        this.nodes = nodes;
        this.action = a;
    }

    public Action getAction() {
        return this.action;
    }

    public Deque<Node> getNodes() {
        return this.nodes;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

}
