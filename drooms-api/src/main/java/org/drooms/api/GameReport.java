package org.drooms.api;

import java.io.IOException;
import java.io.Writer;

/**
 * Represents a report of the progress of the game.
 *
 * @param <P> Type of playground on which the game is happening.
 * @param <N> Type of node on the playground.
 * @param <E> Type of edge one the playground.
 */
public interface GameReport<P extends Playground<N, E>, N extends Node, E extends Edge<N>> {
    
    public void collectibleAdded(Collectible c, N where);
    
    public void collectibleRemoved(Collectible c);

    public void collectibleCollected(Collectible c, Player p, int points);
    
    public void playerCrashed(Player p);
    
    public void playerDeactivated(Player p);
    
    public void playerMoved(Player p, Move m, @SuppressWarnings("unchecked") N... nodes);
    
    public void playerSurvived(Player p, int points);

    public void nextTurn();

    /**
     * Write the report in its current state.
     * 
     * @param w Where to write.
     * @throws IOException When the stream cannot be written.
     */
    public void write(Writer w) throws IOException;

}
