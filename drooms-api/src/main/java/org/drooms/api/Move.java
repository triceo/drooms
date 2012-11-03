package org.drooms.api;

/**
 * The various possibilities for the worm to move in.
 * 
 */
public enum Move {

    /**
     * Increases Y, doesn't change X.
     */
    UP,

    /**
     * Decreases Y, doesn't change X.
     */
    DOWN,
    /**
     * Decreases X, doesn't change Y.
     */
    LEFT,
    /**
     * Increases X, doesn't change Y.
     */
    RIGHT,
    /**
     * Changes neither X nor Y.
     */
    STAY;

}
