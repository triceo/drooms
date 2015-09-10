package org.drooms.api;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a certain type of game, with its own rules and constraints.
 */
public interface Game {

    /**
     * Build the playground from an input stream. Specific instructions on how
     * to structure this file will come from implementations of this class.
     * 
     * @param name
     *            Name for the new playground.
     * @param source
     *            Stream in question.
     * @return Playground constructed from that stream.
     */
    Playground buildPlayground(final String name, final InputStream source);

    /**
     * Add a custom listener to the game. Will be used next time {@link #play(Playground, Collection, File)} is called.
     * 
     * @param listener
     *            Listener in question.
     * @return True if added.
     */
    boolean addListener(GameProgressListener listener);

    /**
     * Retrieve the main report of this game, detailing the progress of the
     * game.
     * 
     * @return The report. Null when game not played before.
     */
    GameProgressListener getReport();

    /**
     * Initialize the game and play it through. Will throw an exception in case of a repeated call of this method on the
     * same class instance. May throw {@link IllegalStateException} if {@link #setContext(InputStream)} wasn't called
     * first.
     * 
     * @param playground
     *            The playground on which this game will be played out.
     * @param players
     *            A list of players to participate in the game.
     * @param reportFolder
     *            Where to output data, if necessary.
     * @return Points gained by each player.
     */
    Map<Player, Integer> play(Playground playground, Collection<Player> players, File reportFolder);

    /**
     * Remove a previously {@link #addListener(GameProgressListener)}ed listener. This listener will not for any
     * subsequent calls to {@link #play(Playground, Collection, File)}.
     * 
     * @param listener
     *            Listener in question.
     * @return True if removed.
     */
    boolean removeListener(GameProgressListener listener);

    /**
     * Sets the context for this game. The context should provide properties
     * that the game should use to decide various situations.
     * 
     * @param context
     *            Where do we load the game properties from.
     */
    void setContext(InputStream context);

}
