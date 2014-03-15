package org.drooms.api;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Represents a connection (a route) between two immediately adjacent {@link Node}s in a {@link Playground}. Worms can
 * move from one {@link Node} to anoter only by using an {@link Edge}. The connection is always
 * bi-directional.
 */
public interface Edge {

    /**
     * Retrieve nodes in this edge.
     * 
     * @return A pair of nodes. First node is always the one on a lower row, or on a left-most place in the same row.
     */
    ImmutablePair<Node, Node> getNodes();

}