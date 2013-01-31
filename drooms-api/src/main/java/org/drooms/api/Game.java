package org.drooms.api;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

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
     * of a repeated call of this method on the same class instance.
     * 
     * @param playground
     *            The playground on which this game will be played out.
     * @param config
     *            Game configuration.
     * @param players
     *            A list of players to participate in the game.
     * @param reportFolder
     *            Where to output data, if necessary.
     * @return Points gained by each player.
     */
    public Map<Player, Integer> play(Playground playground, Properties config, Collection<Player> players, File reportFolder);

}
