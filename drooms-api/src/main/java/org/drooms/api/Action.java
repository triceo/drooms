package org.drooms.api;

/**
 * The various possibilities for the worm to act.
 */
public enum Action {

    /**
     * Use a portal. Will turn into STAY, if not on portal at the moment.
     */
    ENTER,
    /**
     * Increases Y, doesn't change X.
     */
    MOVE_UP,
    /**
     * Decreases Y, doesn't change X.
     */
    MOVE_DOWN,
    /**
     * Decreases X, doesn't change Y.
     */
    MOVE_LEFT,
    /**
     * Increases X, doesn't change Y.
     */
    MOVE_RIGHT,
    /**
     * Worm will remain exactly as is.
     */
    NOTHING,
    /**
     * Will make worm's head its tail and vice versa.
     */
    REVERSE;

}
