package org.drooms.impl.logic.commands;

import java.util.Deque;

import org.drooms.api.GameReport;
import org.drooms.api.Move;
import org.drooms.api.Player;
import org.drooms.impl.DefaultEdge;
import org.drooms.impl.DefaultNode;
import org.drooms.impl.DefaultPlayground;
import org.drooms.impl.logic.CommandDistributor;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.events.PlayerMoveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovePlayerCommand implements
        Command<DefaultPlayground, DefaultNode, DefaultEdge>, PlayerRelated {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MovePlayerCommand.class);

    private final Player toMove;
    private final Move whichMove;
    private final Deque<DefaultNode> nodes;
    private final PlayerMoveEvent<DefaultNode> event;

    public MovePlayerCommand(final Player p, final Move m,
            final Deque<DefaultNode> nodes) {
        this.toMove = p;
        this.whichMove = m;
        this.nodes = nodes;
        this.event = new PlayerMoveEvent<DefaultNode>(p, m, nodes);
    }

    @Override
    public Player getPlayer() {
        return this.toMove;
    }
    
    public Deque<DefaultNode> getNodes() {
        return this.nodes;
    }

    @Override
    public boolean isValid(final CommandDistributor controller) {
        // FIXME add node validation
        return controller.hasPlayer(this.toMove);
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfPlayerMove(this.event);
    }

    @Override
    public void report(
            final GameReport<DefaultPlayground, DefaultNode, DefaultEdge> report) {
        MovePlayerCommand.LOGGER.info(
                "Player {}'s move is {}, new position is {}.", new Object[] {
                        this.toMove.getName(), this.whichMove, this.nodes });
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
