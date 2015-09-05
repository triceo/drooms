package org.drooms.impl.logic.commands;

import org.drooms.api.Action;
import org.drooms.api.GameProgressListener;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.PlayerPosition;
import org.drooms.impl.logic.PlayerLogic;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.events.PlayerActionEvent;

import java.util.Collection;

public class PlayerActionCommand implements Command, PlayerRelated {

    private final Player actor;
    private final Action action;
    private final Collection<Node> nodes;
    private final PlayerActionEvent event;

    public PlayerActionCommand(final Action a, final PlayerPosition position) {
        this.actor = position.getPlayer();
        this.action = a;
        this.nodes = position.getNodes();
        this.event = new PlayerActionEvent(this.actor, a, position.getHeadNode(), nodes);
    }

    public Collection<Node> getNodes() {
        return this.nodes;
    }

    @Override
    public Player getPlayer() {
        return this.actor;
    }

    @Override
    public void perform(final PlayerLogic logic) {
        logic.notifyOfPlayerMove(this.event);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.playerPerformedAction(this.actor, this.action,
                this.nodes.toArray(new Node[]{}));
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MovePlayerCommand [actor=").append(this.actor)
                .append(", action=").append(this.action)
                .append(", nodes=").append(this.nodes).append("]");
        return builder.toString();
    }

}
