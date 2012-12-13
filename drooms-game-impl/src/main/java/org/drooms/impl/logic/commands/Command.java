package org.drooms.impl.logic.commands;

import org.drooms.api.GameProgressListener;
import org.drooms.impl.logic.DecisionMaker;

public interface Command {

    public void perform(DecisionMaker logic);

    public void report(GameProgressListener report);

}
