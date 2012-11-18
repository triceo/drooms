package org.drooms.impl.logic.commands;

import org.drooms.api.Edge;
import org.drooms.api.GameReport;
import org.drooms.api.Node;
import org.drooms.api.Playground;
import org.drooms.impl.logic.CommandDistributor;
import org.drooms.impl.logic.DecisionMaker;

public interface Command<P extends Playground<N, E>, N extends Node, E extends Edge<N>> {

    public boolean isValid(CommandDistributor controller);

    public void perform(DecisionMaker logic);

    public void report(GameReport<P, N, E> report);

}
