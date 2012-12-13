package org.drooms.api;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * Represents a report of the progress of the game.
 * 
 * @param <P>
 *            Type of playground on which the game is happening.
 */
// FIXME rename to some listener
public interface GameReport {

    public void collectibleAdded(Collectible c, Node where);

    public void collectibleCollected(Collectible c, Player p, Node where,
            int points);

    public void collectibleRemoved(Collectible c, Node where);

    public File getTargetFolder();

    public void nextTurn();

    public void playerCrashed(Player p);

    public void playerDeactivated(Player p);

    public void playerMoved(Player p, Move m, Node... nodes);

    public void playerSurvived(Player p, int points);

    /**
     * Write the report in its current state.
     * 
     * @param w
     *            Where to write.
     * @throws IOException
     *             When the stream cannot be written.
     */
    public void write(Writer w) throws IOException;

}
