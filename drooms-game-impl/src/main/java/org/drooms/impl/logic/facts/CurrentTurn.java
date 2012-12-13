package org.drooms.impl.logic.facts;

/**
 * Represents type of fact to be inserted into the working memory, so that the
 * strategy has information about the current turn.
 */
public class CurrentTurn {

    private int number;

    public CurrentTurn(final int number) {
        this.number = number;
    }

    public int getNumber() {
        return this.number;
    }

    // TODO strategies shouldn't be allowed to call this
    public void setNumber(final int number) {
        this.number = number;
    }

}