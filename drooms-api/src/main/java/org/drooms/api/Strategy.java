package org.drooms.api;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;

import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;

/**
 * Represents a {@link Player}'s strategy for a worm to move around on a
 * {@link Playground}. This strategy should be implemented as a set of Drools
 * business rules.
 * 
 * This is the interface that competitors should implement in order to enter in
 * the game. If your strategy depends on a particular path-finding algorithm,
 * you need to implement {@link CustomPathBasedStrategy}. Otherwise,
 * {@link UnweightedShortestPath} will be used.
 */
public interface Strategy {

    /**
     * Whether or not the Drools session for this strategy should be audited. If
     * enabled, will produce vast amounts of audit data.
     * 
     * @return True if the strategy should be audited.
     */
    public boolean enableAudit();

    /**
     * Provide a means of obtaining a {@link KnowledgeBase}.
     * 
     * @param cls
     *            A classloader to use for this. Implementations should make
     *            sure they use the provided classloader - otherwise the
     *            {@link Game} cannot guarantee that strategies are independent
     *            of each other.
     * @return Means of obtaining a {@link KnowledgeBase} later.
     */
    public KnowledgeBuilder getKnowledgeBuilder(ClassLoader cls);

    /**
     * Provide a name for the strategy.
     * 
     * @return Name of the strategy.
     */
    public String getName();
}
