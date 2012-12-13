package org.drooms.api;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;

/**
 * Represents a {@link Player}'s strategy for a worm to move around on a {@link
 * Playground.} This strategy should be implemented as a set of Drools business
 * rules.
 * 
 * This is the interface that competitors should implement in order to enter in
 * the game.
 */
public interface Strategy {

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
