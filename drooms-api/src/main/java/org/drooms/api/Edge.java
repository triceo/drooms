package org.drooms.api;

/**
 * Represents a connection (a route) between two immediately adjacent {@link Node}s in a {@link Playground}. Worms can
 * move from one {@link Node} to another only by using an {@link Edge}. The connection is always bi-directional.
 */
public interface Edge {

    Node getFirstNode();

    Node getSecondNode();

}
