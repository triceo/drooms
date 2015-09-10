package org.drooms.api;

import java.io.IOException;
import java.io.Writer;

/**
 * Tracks progress of the game.
 * 
 */
public interface GameProgressListener {

    void collectibleAdded(Collectible c);

    void collectibleCollected(Collectible c, Player p, int points);

    void collectibleRemoved(Collectible c);

    void nextTurn();

    void playerCrashed(Player p);

    void playerDeactivated(Player p);

    void playerPerformedAction(Player p, Action m, Node... nodes);

    void playerSurvived(Player p, int points);

    /**
     * Write a report of the current state.
     * 
     * @param w
     *            Where to write.
     * @throws IOException
     *             When it cannot be written.
     */
    void write(Writer w) throws IOException;

}
