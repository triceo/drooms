package org.drooms.api;

import java.util.List;

import edu.uci.ics.jung.graph.Graph;

/**
 * Represents a playing field for the worms as a graph of {@link Node}s and
 * {@link Edge}s.
 */
public interface Playground {

    /**
     * Return the playing field as a graph.
     * 
     * @return An unmodifiable graph representing the playing field.
     */
    public Graph<Node, Edge> getGraph();

    /**
     * Return the height of the playing field.
     * 
     * @return Of all the nodes in the field, this is the maximum vertical
     *         co-ordinate.
     */
    public int getHeight();

    /**
     * Returns {@link Node}s at which {@link Player}s are allowed to start out
     * from.
     * 
     * @return An unmodifiable list of starting positions.
     */
    public List<Node> getStartingPositions();

    /**
     * Return the width of the playing field.
     * 
     * @return Of all the nodes in the field, this is the maximum horizontal
     *         co-ordinate.
     */
    public int getWidth();

    /**
     * Whether or not a {@link Node} is available for a {@link Player}'s worm to
     * move into without crashing into a wall.
     * 
     * @param x
     *            Horizontal co-ordinate.
     * @param y
     *            Vertical co-ordinate.
     * @return True if available.
     */
    public boolean isAvailable(int x, int y);

}
