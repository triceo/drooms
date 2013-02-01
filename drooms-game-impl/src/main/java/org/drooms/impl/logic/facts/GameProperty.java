package org.drooms.impl.logic.facts;

import org.drooms.api.Move;
import org.drooms.impl.GameController;

/**
 * Represents type of fact to be inserted into the working memory, so that the
 * player is aware of various configuration options being in effect.
 */
public class GameProperty {

    public enum Name {
        /**
         * How many turns will be played.
         */
        MAX_TURNS,
        /**
         * How many turns a worm can {@link Move#STAY} and not be removed from
         * the game.
         */
        MAX_INACTIVE_TURNS,
        /**
         * How many points will a player be awarded for each dead worm. More
         * details on the precise awarding mechanism can be found in
         * {@link GameController}.
         */
        DEAD_WORM_BONUS,
        /**
         * How much time the strategy has to decide on a move. After that, the
         * strategy will be terminated.
         */
        TIMEOUT_IN_SECONDS

    }

    private final Name name;

    private final int value;

    public GameProperty(final Name name, final int value) {
        this.name = name;
        this.value = value;
    }

    public Name getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }

}
