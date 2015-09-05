package org.drooms.api;

/**
 * A single indivisible unit of real estate on the {@link Playground}.
 */
public interface Node {

    Type getType();

    enum Type {
        REGULAR,
        WALL,
        STARTING_POSITION,
        PORTAL;

    }

    int getX();

    int getY();
}
