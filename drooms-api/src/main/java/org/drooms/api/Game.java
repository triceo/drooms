package org.drooms.api;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a certain type of game, with its own rules and constraints.
 */
public interface Game {

    /**
     * Retrieve the main report of this game, detailing the progress of the
     * game.
     * 
     * @return The report. Null when game not played before.
     */
    public GameProgressListener getReport();

    /**
     * Initialize the game and play it through. Will throw an exception in case
     * of a repeated call of this method on the same class instance. May throw
     * {@link IllegalStateException} if {@link #setContext(Object)} wasn't
     * called first.
     * 
     * @param playground
     *            The playground on which this game will be played out.
     * @param players
     *            A list of players to participate in the game.
     * @param reportFolder
     *            Where to output data, if necessary.
     * @return Points gained by each player.
     */
    public Map<Player, Integer> play(Playground playground, Collection<Player> players, File reportFolder);

    /**
     * Sets the context for this game. The context should provide properties
     * that the game should use to decide various situations.
     * 
     * @param context
     *            This is a simple Object, since we don't want to enforce
     *            anything on the game implementations. Neither we know what to
     *            enforce.
     */
    public void setContext(Object context);

}
