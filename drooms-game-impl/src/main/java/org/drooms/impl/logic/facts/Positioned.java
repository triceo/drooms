package org.drooms.impl.logic.facts;

import org.drooms.api.Node;

/**
 * States that a certain fact type is related to a position of something.
 */
public interface Positioned {

    /**
     * The position.
     * 
     * @return
     */
    public Node getNode();

    /**
     * The horizontal co-ordinate of that position.
     * 
     * @return
     */
    public int getX();

    /**
     * The vertical co-ordinate of that position.
     * 
     * @return
     */
    public int getY();

}