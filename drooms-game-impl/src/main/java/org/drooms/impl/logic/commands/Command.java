package org.drooms.impl.logic.commands;

import org.drooms.api.GameReport;
import org.drooms.impl.logic.DecisionMaker;

public interface Command {

    public void perform(DecisionMaker logic);

    public void report(GameReport report);

}
