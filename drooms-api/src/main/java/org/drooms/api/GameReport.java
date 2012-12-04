package org.drooms.api;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a report of the progress of the game.
 *
 * @param <P> Type of playground on which the game is happening.
 * @param <N> Type of node on the playground.
 * @param <E> Type of edge one the playground.
 */
public interface GameReport<P extends Playground<N, E>, N extends Node, E extends Edge<N>> {
    
    public void addCollectible(Collectible c, N where);
    
    public void removeCollectible(Collectible c);

    public void collectCollectible(Collectible c, Player p, int points);
    
    public void crashPlayer(Player p);
    
    public void deactivatePlayer(Player p);
    
    public void movePlayer(Player p, Move m, @SuppressWarnings("unchecked") N... nodes);
    
    public void playerSurvived(Player p, int points);

    public void nextTurn();

    /**
     * Write the report in its current state.
     * 
     * @param s Where to write.
     * @throws IOException When the stream cannot be written.
     */
    public void write(OutputStream s) throws IOException;

}
