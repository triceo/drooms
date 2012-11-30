package org.drooms.impl.logic.facts;

public class Wall implements Positioned {

    private final int x, y;

    public Wall(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

}