package org.drooms.api;

import edu.uci.ics.jung.graph.Graph;

import java.util.List;

/**
 * Represents a playing field for the worms as a graph of {@link Node}s and {@link Edge}s.
 */
public interface Playground {

    /**
     * Return the playing field as a graph.
     * 
     * @return An unmodifiable graph representing the playing field.
     */
    Graph<Node, Edge> getGraph();

    /**
     * Return the height of the playing field.
     * 
     * @return Of all the nodes in the field, this is the maximum vertical
     *         co-ordinate.
     */
    int getHeight();

    /**
     * Return the playground name.
     * 
     * @return String representing the playground.
     */
    String getName();

    /**
     * Retrieve a node at the particular position.
     * 
     * @param x
     *            Horizontal co-ordinate.
     * @param y
     *            Vertical co-ordinate.
     * @return A node if {@link #isAvailable(int, int)}, a wall otherwise.
     */
    Node getNodeAt(int x, int y);

    /**
     * Retrieve the other end of a portal.
     * 
     * @param portal
     *            The known end of a portal.
     * @return The other end, if a known portal.
     * @throws IllegalArgumentException
     *             when not portal or not a known portal.
     */
    Node getOtherEndOfPortal(Node portal);

    /**
     * Returns {@link Node}s at which {@link Player}s are allowed to start out
     * from.
     * 
     * @return An unmodifiable list of starting positions.
     */
    List<Node> getStartingPositions();

    /**
     * Return the width of the playing field.
     * 
     * @return Of all the nodes in the field, this is the maximum horizontal
     *         co-ordinate.
     */
    int getWidth();

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
    boolean isAvailable(int x, int y);

}
