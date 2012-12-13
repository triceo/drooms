package org.drooms.impl.logic.commands;

import java.util.Deque;

import org.drooms.api.GameProgressListener;
import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.events.PlayerMoveEvent;

public class MovePlayerCommand implements Command, PlayerRelated {

    private final Player toMove;
    private final Move whichMove;
    private final Deque<Node> nodes;
    private final PlayerMoveEvent event;

    public MovePlayerCommand(final Player p, final Move m,
            final Deque<Node> nodes) {
        this.toMove = p;
        this.whichMove = m;
        this.nodes = nodes;
        this.event = new PlayerMoveEvent(p, m, nodes);
    }

    public Deque<Node> getNodes() {
        return this.nodes;
    }

    @Override
    public Player getPlayer() {
        return this.toMove;
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfPlayerMove(this.event);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.playerMoved(this.toMove, this.whichMove,
                this.nodes.toArray(new Node[] {}));
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MovePlayerCommand [toMove=").append(this.toMove)
                .append(", whichMove=").append(this.whichMove)
                .append(", nodes=").append(this.nodes).append("]");
        return builder.toString();
    }

}
