package org.drooms.impl.logic.commands;

import org.drooms.api.GameProgressListener;
import org.drooms.impl.GameController;
import org.drooms.impl.logic.CommandDistributor;
import org.drooms.impl.logic.PlayerLogic;

/**
 * Represents a game state change to be sent from {@link GameController} to
 * {@link CommandDistributor} and further passed to the strategies.
 */
public interface Command {

    /**
     * Perform the state change on the strategy.
     *
     * @param logic
     *            Player's strategy in action.
     */
    public void perform(PlayerLogic logic);

    /**
     * Report the state change to a listener.
     * 
     * @param report
     *            The listener.
     */
    public void report(GameProgressListener report);

}
