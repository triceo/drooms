package org.drooms.impl.logic.events;

import org.drooms.api.Action;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.logic.PlayerRelated;

import java.util.Collection;

public class PlayerActionEvent implements PlayerRelated {

    private final Player player;
    private final Action action;
    private final Node headNode;
    private final Collection<Node> nodes;

    public PlayerActionEvent(final Player p, final Action a, final Node headNode, final Collection<Node> nodes) {
        this.player = p;
        this.nodes = nodes;
        this.headNode = headNode;
        this.action = a;
    }

    public Action getAction() {
        return this.action;
    }

    public Node getHeadNode() {
        return this.headNode;
    }

    public Collection<Node> getNodes() {
        return this.nodes;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

}
