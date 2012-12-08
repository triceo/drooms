package org.drooms.impl.logic.commands;

import org.drooms.api.GameReport;
import org.drooms.api.Playground;
import org.drooms.impl.logic.DecisionMaker;

public interface Command<P extends Playground> {

    public void perform(DecisionMaker logic);

    public void report(GameReport<P> report);

}
