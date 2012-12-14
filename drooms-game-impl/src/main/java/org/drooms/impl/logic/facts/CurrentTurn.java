package org.drooms.impl.logic.facts;

/**
 * Represents type of fact to be inserted into the working memory, so that the
 * strategy has information about the current turn.
 */
public class CurrentTurn {

    private final int number;

    public CurrentTurn(final int number) {
        this.number = number;
    }

    public int getNumber() {
        return this.number;
    }

}